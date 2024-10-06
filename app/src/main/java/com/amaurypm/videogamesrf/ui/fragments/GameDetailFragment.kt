package com.amaurypm.videogamesrf.ui.fragments

import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.application.VideoGamesRFApp
import com.amaurypm.videogamesrf.data.TreeServiceRepository
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDetailDto
import com.amaurypm.videogamesrf.databinding.FragmentGameDetailBinding
import com.amaurypm.videogamesrf.utils.Constants
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val GAME_ID = "game_id"

class GameDetailFragment : Fragment() {

    private var gameId: String? = null

    private var _binding: FragmentGameDetailBinding? = null
    private val binding get()  = _binding!!

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

    //Se manda llamar ya cuando el fragment es visible en pantalla
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Obteniendo la instancia al repositorio
        repository = (requireActivity().application as VideoGamesRFApp).repository

        gameId?.let{ id ->
            //Hago la llamada al endpoint para consumir los detalles del juego

            //val call: Call<GameDetailDto> = repository.getGameDetail(id)

            //Para apiary
            val call: Call<TreeServiceDetailDto> = repository.getTreeServiceDetailApiary(id)

            call.enqueue(object: Callback<TreeServiceDetailDto>{
                override fun onResponse(p0: Call<TreeServiceDetailDto>, response: Response<TreeServiceDetailDto>) {

                    binding.apply {
                        pbLoading.visibility = View.GONE

                        //Aquí utilizamos la respuesta exitosa y asignamos los valores a las vistas
                        tvTitle.text = response.body()?.title

                        Glide.with(requireActivity())
                            .load(response.body()?.image)
                            .into(ivImage)

                        /*Picasso.get()
                            .load(response.body()?.image)
                            .into(ivImage)*/

                        tvLongDesc.text = getString(R.string.description, response.body()?.longDesc)
                        tvAdditionalDetail1.text = getString(R.string.details, response.body()?.additionalDetail1)
                        tvAdditionalDetail2.text = getString(R.string.additional, response.body()?.additionalDetail2)

                        //Para justificar el texto de un textview
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) //Q corresponde a Android 10
                            tvLongDesc.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                    }



                }

                override fun onFailure(p0: Call<TreeServiceDetailDto>, p1: Throwable) {
                    //Manejo del error de conexión
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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