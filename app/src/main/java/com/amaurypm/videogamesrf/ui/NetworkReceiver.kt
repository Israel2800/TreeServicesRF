package com.amaurypm.videogamesrf.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast

class NetworkReceiver(private val onNetworkAvailable: () -> Unit) : BroadcastReceiver() {

    // Variable para saber si la conexión fue previamente perdida
    private var wasDisconnected = false

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkInfo = connectivityManager.getNetworkInfo(activeNetwork)

        if (networkInfo != null && networkInfo.isConnected) {
            if (wasDisconnected) {
                // Solo mostrar el Toast cuando la conexión se haya recuperado después de haber estado perdida
                Toast.makeText(context, "Connection restored", Toast.LENGTH_SHORT).show()
                onNetworkAvailable() // Recargar los datos cuando la conexión se haya restablecido
            }
            // Cambiar el estado a conectado
            wasDisconnected = false
        } else {
            if (!wasDisconnected) {
                // Solo mostrar el Toast la primera vez cuando la conexión se pierde
                Toast.makeText(context, "Connection lost", Toast.LENGTH_SHORT).show()
            }
            // Cambiar el estado a desconectado
            wasDisconnected = true
        }
    }
}
