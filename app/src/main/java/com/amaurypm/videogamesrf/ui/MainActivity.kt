package com.amaurypm.videogamesrf.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.databinding.ActivityMainBinding
import com.amaurypm.videogamesrf.ui.fragments.GamesListFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkReceiver

    private lateinit var firebaseAuth: FirebaseAuth
    private var user: FirebaseUser? = null
    private var userId: String? = null

    private val viewModel by viewModels<MainViewModel>()

    /*private lateinit var repository: GameRepository
    private lateinit var retrofit: Retrofit*/

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pasamos la orientación en portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        firebaseAuth = FirebaseAuth.getInstance()

        user = firebaseAuth.currentUser
        userId = user?.uid

        binding.tvUsuario.text = user?.email

        // Colocamos un SplashScreen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f,
                    0.0f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }

                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }

        // Revisamos si el correo no está verificado
        if(user?.isEmailVerified != true){
            // No se ha verificado su correo
            binding.tvCorreoNoVerificado.visibility = View.VISIBLE
            binding.btnReenviarVerificacion.visibility = View.VISIBLE

            binding.btnReenviarVerificacion.setOnClickListener {
                user?.sendEmailVerification()?.addOnSuccessListener {
                    message(getString(R.string.verification_email_sent))
                }?.addOnFailureListener {
                    message(getString(R.string.verification_email_failed))
                }
            }
        }

        //Mostramos el fragment inicial GamesListFragment
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GamesListFragment())
                .commit()
        }

        binding.btnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            message(getString(R.string.session_closed))
            finish()
        }

        networkReceiver = NetworkReceiver {
            // Llamar al ViewModel para recargar la data
            reloadData()
        }

    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }

    private fun reloadData() {
        // Llamar al método del MainViewModel para refrescar la data
        viewModel.refreshData()
    }

}