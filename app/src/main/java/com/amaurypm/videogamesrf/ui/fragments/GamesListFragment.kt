package com.amaurypm.videogamesrf.ui.fragments

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.application.VideoGamesRFApp
import com.amaurypm.videogamesrf.data.TreeServiceRepository
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDto
import com.amaurypm.videogamesrf.databinding.FragmentGamesListBinding
import com.amaurypm.videogamesrf.ui.NetworkReceiver
import com.amaurypm.videogamesrf.ui.adapters.TreeServicesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GamesListFragment : Fragment() {

    private var _binding: FragmentGamesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TreeServiceRepository
    private lateinit var networkReceiver: NetworkReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGamesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity().application as VideoGamesRFApp).repository

        setupRecyclerView()
        loadData()

        // Configurar el receptor de red
        networkReceiver = NetworkReceiver { onConnectionRestored() }
        requireContext().registerReceiver(
            networkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    private fun setupRecyclerView() {
        binding.rvGames.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadData() {
        binding.pbLoading.visibility = View.VISIBLE

        val call: Call<MutableList<TreeServiceDto>> = repository.getTreeServicesApi()
        call.enqueue(object : Callback<MutableList<TreeServiceDto>> {
            override fun onResponse(
                call: Call<MutableList<TreeServiceDto>>,
                response: Response<MutableList<TreeServiceDto>>
            ) {
                binding.pbLoading.visibility = View.GONE
                response.body()?.let { treeServices ->
                    binding.rvGames.adapter = TreeServicesAdapter(treeServices) { treeService ->
                        treeService.id?.let { id ->
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, GameDetailFragment.newInstance(id))
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MutableList<TreeServiceDto>>, t: Throwable) {
                binding.pbLoading.visibility = View.GONE
            }
        })
    }

    private fun onConnectionRestored() {
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(networkReceiver)
        _binding = null
    }
}
