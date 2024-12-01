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
                if(authResult.isSuccessful){
                    message("Usuario creado exitosamente")


                    // Enviar correo para verificar la dirección de email
                    firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        message("El correo de verificación ha sido enviado")
                    }?.addOnFailureListener {
                        message("No se pudo enviar el correo de verificación")
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
                .setTitle("Restablecer contraseña")
                .setMessage("Ingrese su correo electrónico para recibir el enlace de restablecimiento")
                .setView(resetMail)
                .setPositiveButton("Enviar"){ _, _ ->
                    val mail = resetMail.text.toString()
                    if(mail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                        //Mandamos el enlace de restablecimiento
                        firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener {
                            message("El correo para restablecer la contraseña ha sido enviado")
                        }.addOnFailureListener {
                            message("El enlace no se ha podido enviar")
                        }
                    }else{
                        message("Favor de ingresar una dirección de correo válida")
                    }
                }
                .setNegativeButton("Cancelar"){ dialog, _ ->
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

    private fun validateFields(): Boolean{
        email = binding.tietEmail.text.toString().trim()  //Elimina los espacios en blanco
        contrasenia = binding.tietContrasenia.text.toString().trim()

        //Verifica que el campo de correo no esté vacío
        if(email.isEmpty()){
            binding.tietEmail.error = "Se requiere el correo"
            binding.tietEmail.requestFocus()
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.tietEmail.error = "El correo no tiene un formato válido"
            binding.tietEmail.requestFocus()
            return false
        }

        //Verifica que el campo de la contraseña no esté vacía y tenga al menos 6 caracteres
        if(contrasenia.isEmpty()){
            binding.tietContrasenia.error = "Se requiere una contraseña"
            binding.tietContrasenia.requestFocus()
            return false
        }else if(contrasenia.length < 6){
            binding.tietContrasenia.error = "La contraseña debe tener al menos 6 caracteres"
            binding.tietContrasenia.requestFocus()
            return false
        }
        return true
    }


    private fun handleErrors(task: Task<AuthResult>){
        var errorCode = ""

        try{
            errorCode = (task.exception as FirebaseAuthException).errorCode
        }catch(e: Exception){
            e.printStackTrace()
        }

        when(errorCode){
            "ERROR_INVALID_EMAIL" -> {
                Toast.makeText(this, "Error: El correo electrónico no tiene un formato correcto", Toast.LENGTH_SHORT).show()
                binding.tietEmail.error = "Error: El correo electrónico no tiene un formato correcto"
                binding.tietEmail.requestFocus()
            }
            "ERROR_WRONG_PASSWORD" -> {
                Toast.makeText(this, "Error: La contraseña no es válida", Toast.LENGTH_SHORT).show()
                binding.tietContrasenia.error = "La contraseña no es válida"
                binding.tietContrasenia.requestFocus()
                binding.tietContrasenia.setText("")

            }
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                //An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.
                Toast.makeText(this, "Error: Una cuenta ya existe con el mismo correo, pero con diferentes datos de ingreso", Toast.LENGTH_SHORT).show()
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                Toast.makeText(this, "Error: el correo electrónico ya está en uso con otra cuenta.", Toast.LENGTH_LONG).show()
                binding.tietEmail.error = ("Error: el correo electrónico ya está en uso con otra cuenta.")
                binding.tietEmail.requestFocus()
            }
            "ERROR_USER_TOKEN_EXPIRED" -> {
                Toast.makeText(this, "Error: La sesión ha expirado. Favor de ingresar nuevamente.", Toast.LENGTH_LONG).show()
            }
            "ERROR_USER_NOT_FOUND" -> {
                Toast.makeText(this, "Error: No existe el usuario con la información proporcionada.", Toast.LENGTH_LONG).show()
            }
            "ERROR_WEAK_PASSWORD" -> {
                Toast.makeText(this, "La contraseña porporcionada es inválida", Toast.LENGTH_LONG).show()
                binding.tietContrasenia.error = "La contraseña debe de tener por lo menos 6 caracteres"
                binding.tietContrasenia.requestFocus()
            }
            "NO_NETWORK" -> {
                Toast.makeText(this, "Red no disponible o se interrumpió la conexión", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Error. No se pudo autenticar exitosamente.", Toast.LENGTH_SHORT).show()
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
                message("Autenticación exitosa")
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
        // Llamar al método del MainViewModel para refrescar la data
        viewModel.refreshData()
    }

}