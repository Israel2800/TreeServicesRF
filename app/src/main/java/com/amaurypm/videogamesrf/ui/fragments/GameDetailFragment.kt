package com.amaurypm.videogamesrf.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.application.VideoGamesRFApp
import com.amaurypm.videogamesrf.data.TreeServiceRepository
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDetailDto
import com.amaurypm.videogamesrf.databinding.FragmentGameDetailBinding
import com.amaurypm.videogamesrf.ui.NetworkReceiver
import com.amaurypm.videogamesrf.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val GAME_ID = "game_id"

class GameDetailFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private var gameId: String? = null
    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: TreeServiceRepository
    private lateinit var map: GoogleMap
    private var fineLocationPermissionGranted = false
    private lateinit var networkReceiver: NetworkReceiver

    // private lateinit var viewModel: MainViewModel


    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            actionPermissionGranted()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission Required")
                    .setMessage("Location permission is needed to display the user's position on the map.")
                    .setPositiveButton("Understood") { _, _ -> updateOrRequestPermissions() }
                    .setNegativeButton("Exit") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission was permanently denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Registrar el NetworkReceiver
        networkReceiver = NetworkReceiver {
            reloadFragment()
        }
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(networkReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        // Desregistrar el NetworkReceiver para evitar fugas de memoria
        requireContext().unregisterReceiver(networkReceiver)
    }

    private fun reloadFragment() {
        // Recargar datos desde el método de la API
        fetchGameDetails()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            gameId = args.getString(GAME_ID)
            Log.d(Constants.LOGTAG, getString(R.string.id_received, gameId))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = (requireActivity().application as VideoGamesRFApp).repository
        fetchGameDetails()
        initializeMap()
        lifecycle.addObserver(binding.ytPlayerView)
        updateOrRequestPermissions()
    }

    private fun fetchGameDetails() {
        binding.pbLoading.visibility = View.VISIBLE
        gameId?.let { id ->
            repository.getTreeServiceDetailApiary(id).enqueue(object : Callback<TreeServiceDetailDto> {
                override fun onResponse(
                    call: Call<TreeServiceDetailDto>,
                    response: Response<TreeServiceDetailDto>
                ) {
                    binding.pbLoading.visibility = View.GONE
                    if (response.isSuccessful) {
                        val detailData = response.body()
                        detailData?.let { detail ->
                            updateUI(detail)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al cargar los datos. Por favor intenta de nuevo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TreeServiceDetailDto>, t: Throwable) {
                    binding.pbLoading.visibility = View.GONE
                    /*
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar los datos: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    */
                }
            })
        }
    }

    private fun updateUI(detail: TreeServiceDetailDto) {
        binding.apply {
            tvTitle.text = detail.title
            Glide.with(requireActivity()).load(detail.image).into(ivImage)
            tvLongDesc.text = getString(R.string.description, detail.longDesc)
            tvAdditionalDetail1.text = getString(R.string.details, detail.additionalDetail1)
            tvAdditionalDetail2.text = getString(R.string.additional, detail.additionalDetail2)

            detail.latitude?.let { lat ->
                detail.longitude?.let { lng ->
                    updateMapWithCoordinates(lat, lng)
                }
            }

            if (!detail.url_id.isNullOrEmpty()) {
                initializeYouTubePlayer(detail.url_id!!)
            }
        }
    }


    private fun updateMapWithCoordinates(latitude: Double?, longitude: Double?) {
        if (latitude != null && longitude != null) {
            map.clear()
            val coordinate = LatLng(latitude, longitude)
            val marker = MarkerOptions()
                .position(coordinate)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.school))

            map.addMarker(marker)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 18f), 4000, null)
        } else {
            Toast.makeText(
                requireContext(),
                "No se encontraron coordenadas para esta ubicación.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initializeYouTubePlayer(videoId: String) {
        binding.ytPlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0f)
                binding.ytPlayerView.setOnClickListener { youTubePlayer.play() }
            }
        })
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //createMarker() // Ya no es necesario al estar implementando updateMapWithCoordinates
    }


    @SuppressLint("MissingPermission")
    private fun actionPermissionGranted() {
        map.isMyLocationEnabled = true
        val locationManager = requireContext().getSystemService(LocationManager::class.java)
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000,
            10F,
            this
        )
    }

    private fun updateOrRequestPermissions() {
        fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationPermissionGranted) {
            permissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Verificar si el mapa ya está listo antes de permitir la ubicación
            if (::map.isInitialized) {
                actionPermissionGranted()
            } else {
                // Si el mapa no está listo, esperamos hasta que se inicialice.
                initializeMap()
            }
        }
    }


    override fun onLocationChanged(location: Location) {
        map.clear()
        val coordinates = LatLng(location.latitude, location.longitude)
        val marker = MarkerOptions().position(coordinates).icon(BitmapDescriptorFactory.fromResource(R.drawable.person))
        map.addMarker(marker)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 18f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycle.removeObserver(binding.ytPlayerView)
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(gameId: String) = GameDetailFragment().apply {
            arguments = Bundle().apply { putString(GAME_ID, gameId) }
        }
    }
}
