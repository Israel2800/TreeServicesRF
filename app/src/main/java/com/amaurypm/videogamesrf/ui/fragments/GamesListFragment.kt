package com.amaurypm.videogamesrf.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.application.VideoGamesRFApp
import com.amaurypm.videogamesrf.data.TreeServiceRepository
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDto
import com.amaurypm.videogamesrf.databinding.FragmentGamesListBinding
import com.amaurypm.videogamesrf.ui.adapters.TreeServicesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GamesListFragment : Fragment() {

    private var _binding: FragmentGamesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: TreeServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGamesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Obteniendo la instancia al repositorio
        repository = (requireActivity().application as VideoGamesRFApp).repository

        //val call: Call<MutableList<GameDto>> = repository.getGames("cm/games/games_list.php")

        //Para apiary
        val call: Call<MutableList<TreeServiceDto>> = repository.getTreeServicesApi()

        call.enqueue(object: Callback<MutableList<TreeServiceDto>>{
            override fun onResponse(
                p0: Call<MutableList<TreeServiceDto>>,
                response: Response<MutableList<TreeServiceDto>>
            ) {
                binding.pbLoading.visibility = View.GONE

                response.body()?.let{ treeServices ->

                    //Le pasamos los juegos al recycler view y lo instanciamos
                    binding.rvGames.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        //layoutManager = GridLayoutManager(requireContext(), 3)
                        adapter = TreeServicesAdapter(treeServices){ treeService ->
                            //Aquí realizamos la acción para ir a ver los detalles del juego
                            treeService.id?.let{ id ->
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, GameDetailFragment.newInstance(id))
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    }

                }
            }

            override fun onFailure(p0: Call<MutableList<TreeServiceDto>>, p1: Throwable) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_conexion),
                    Toast.LENGTH_SHORT
                ).show()
                binding.pbLoading.visibility = View.GONE
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}