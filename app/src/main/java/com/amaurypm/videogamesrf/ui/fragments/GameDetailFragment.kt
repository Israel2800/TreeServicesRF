package com.amaurypm.videogamesrf.ui.fragments

import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.application.VideoGamesRFApp
import com.amaurypm.videogamesrf.data.TreeServiceRepository
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDetailDto
import com.amaurypm.videogamesrf.databinding.FragmentGameDetailBinding
import com.amaurypm.videogamesrf.utils.Constants
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val GAME_ID = "game_id"

class GameDetailFragment : Fragment() {

    private var gameId: String? = null
    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: TreeServiceRepository

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
    ): View? {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as VideoGamesRFApp).repository

        gameId?.let { id ->
            val call: Call<TreeServiceDetailDto> = repository.getTreeServiceDetailApiary(id)

            call.enqueue(object : Callback<TreeServiceDetailDto> {
                override fun onResponse(
                    p0: Call<TreeServiceDetailDto>,
                    response: Response<TreeServiceDetailDto>
                ) {
                    binding.apply {
                        pbLoading.visibility = View.GONE

                        tvTitle.text = response.body()?.title
                        Glide.with(requireActivity())
                            .load(response.body()?.image)
                            .into(ivImage)

                        tvLongDesc.text = getString(R.string.description, response.body()?.longDesc)
                        tvAdditionalDetail1.text = getString(R.string.details, response.body()?.additionalDetail1)
                        tvAdditionalDetail2.text = getString(R.string.additional, response.body()?.additionalDetail2)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            tvLongDesc.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                        }

                        // Obtener el ID del video de YouTube desde la API
                        val urlId = response.body()?.url_id
                        Log.d("GameDetailFragment", "Video URL: $urlId")

                        if (!urlId.isNullOrEmpty()) {
                            val videoId = urlId // ID del video obtenido de la API

                            try {
                                binding.ytPlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        try {
                                            // Prepara el video sin reproducirlo
                                            youTubePlayer.cueVideo(videoId, 0f)

                                            // Configuración de un setOlickListener para el YouTubePlayerView
                                            binding.ytPlayerView.setOnClickListener {
                                                // Inicia la reproducción al dar clic
                                                youTubePlayer.play()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("YouTubePlayer", "Error al preparar el video: ${e.message}")
                                        }
                                    }

                                    override fun onStateChange(
                                        youTubePlayer: YouTubePlayer,
                                        state: PlayerConstants.PlayerState
                                    ) {
                                        try {
                                            // Si el video termina, regresa al inicio pero no se reproduce automáticamente
                                            if (state == PlayerConstants.PlayerState.ENDED) {
                                                youTubePlayer.seekTo(0f)
                                                youTubePlayer.pause() // Pausa para esperar otro click e iniciar de nuevo
                                            }
                                        } catch (e: Exception) {
                                            Log.e("YouTubePlayer", "Error durante el cambio de estado: ${e.message}")
                                        }
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e("YouTubePlayer", "Error al inicializar el reproductor de YouTube: ${e.message}")
                            }
                        }

                    }
                }

                override fun onFailure(p0: Call<TreeServiceDetailDto>, p1: Throwable) {
                    Log.e("GameDetailFragment", "Error al obtener los detalles: ${p1.message}")
                }
            })
        }

        lifecycle.addObserver(binding.ytPlayerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.ytPlayerView.release()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(gameId: String) =
            GameDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(GAME_ID, gameId)
                }
            }
    }
}
