package com.amaurypm.videogamesrf.ui

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Propiedad para firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var networkReceiver: NetworkReceiver

    private val viewModel by viewModels<MainViewModel>()


    // Propiedades para el usuario y contraseña
    private var email = ""
    private var contrasenia = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Instanciamos el objeto firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Revisamos si ya tenemos a un usuario autenticado y lo pasamos al MainActivity
        if(firebaseAuth.currentUser != null)
            actionLoginSuccesful()

        // Botón para ingresar
        binding.btnLogin.setOnClickListener {
            if(!validateFields()) return@setOnClickListener

            // Mostramos el progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Autenticamos al usuario
            authenticateUser(email, contrasenia)
        }

        // Botón para registrarse
        binding.btnRegistrarse.setOnClickListener {
            if(!validateFields()) return@setOnClickListener

            // Mostramos el progress bar
            binding.progressBar.visibility = View.VISIBLE

            firebaseAuth.createUserWithEmailAndPassword(email, contrasenia).addOnCompleteListener { authResult ->
                if(authResult.isSuccessful) {
                    message(getString(R.string.user_created_success))

                    // Enviar correo para verificar la dirección de email
                    firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        message(getString(R.string.email_verification_sent))
                    }?.addOnFailureListener {
                        message(getString(R.string.email_verification_failed))
                    }
                    actionLoginSuccesful()
                } else {
                    binding.progressBar.visibility = View.GONE
                    handleErrors(authResult)
                }
            }

        }

        // Texto para recuperar contraseña
        binding.tvRestablecerPassword.setOnClickListener {
            val resetMail = EditText(this)
            resetMail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_password_title))
                .setMessage(getString(R.string.reset_password_message))
                .setView(resetMail)
                .setPositiveButton(getString(R.string.send_button)) { _, _ ->
                    val mail = resetMail.text.toString()
                    if(mail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                        //Mandamos el enlace de restablecimiento
                        firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener {
                            message(getString(R.string.reset_email_sent))
                        }.addOnFailureListener {
                            message(getString(R.string.reset_email_failed))
                        }
                    } else {
                        message(getString(R.string.invalid_email))
                    }
                }
                .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }


        networkReceiver = NetworkReceiver {
            // Llamar al ViewModel para recargar la data
            reloadData()
        }

    }

    private fun validateFields(): Boolean {
        email = binding.tietEmail.text.toString().trim()  // Elimina los espacios en blanco
        contrasenia = binding.tietContrasenia.text.toString().trim()

        // Verifica que el campo de correo no esté vacío
        if (email.isEmpty()) {
            binding.tietEmail.error = getString(R.string.email_required)
            binding.tietEmail.requestFocus()
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tietEmail.error = getString(R.string.invalid_email_format)
            binding.tietEmail.requestFocus()
            return false
        }

        // Verifica que el campo de la contraseña no esté vacía y tenga al menos 6 caracteres
        if (contrasenia.isEmpty()) {
            binding.tietContrasenia.error = getString(R.string.password_required)
            binding.tietContrasenia.requestFocus()
            return false
        } else if (contrasenia.length < 6) {
            binding.tietContrasenia.error = getString(R.string.password_length)
            binding.tietContrasenia.requestFocus()
            return false
        }
        return true
    }

    private fun handleErrors(task: Task<AuthResult>) {
        var errorCode = ""

        try {
            errorCode = (task.exception as FirebaseAuthException).errorCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> {
                Toast.makeText(this, getString(R.string.invalid_email_format), Toast.LENGTH_SHORT).show()
                binding.tietEmail.error = getString(R.string.invalid_email_format)
                binding.tietEmail.requestFocus()
            }
            "ERROR_WRONG_PASSWORD" -> {
                Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show()
                binding.tietContrasenia.error = getString(R.string.invalid_password)
                binding.tietContrasenia.requestFocus()
                binding.tietContrasenia.setText("")
            }
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                Toast.makeText(this, getString(R.string.account_exists_with_different_credentials), Toast.LENGTH_SHORT).show()
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                Toast.makeText(this, getString(R.string.email_already_in_use), Toast.LENGTH_LONG).show()
                binding.tietEmail.error = getString(R.string.email_already_in_use)
                binding.tietEmail.requestFocus()
            }
            "ERROR_USER_TOKEN_EXPIRED" -> {
                Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_LONG).show()
            }
            "ERROR_USER_NOT_FOUND" -> {
                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_LONG).show()
            }
            "ERROR_WEAK_PASSWORD" -> {
                Toast.makeText(this, getString(R.string.weak_password), Toast.LENGTH_LONG).show()
                binding.tietContrasenia.error = getString(R.string.weak_password)
                binding.tietContrasenia.requestFocus()
            }
            "NO_NETWORK" -> {
                Toast.makeText(this, getString(R.string.network_unavailable), Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun actionLoginSuccesful(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun authenticateUser(usr: String, psw: String){
        firebaseAuth.signInWithEmailAndPassword(usr, psw).addOnCompleteListener {  authResult ->
            // Verificamos si fue exitosa la autenticación
            if(authResult.isSuccessful){
                message(getString(R.string.authentication_success))
                actionLoginSuccesful()
            } else {
                binding.progressBar.visibility = View.GONE
                handleErrors(authResult)
            }
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
        // Llamar al método del MainViewModel para refrescar
        viewModel.refreshData()
    }

}