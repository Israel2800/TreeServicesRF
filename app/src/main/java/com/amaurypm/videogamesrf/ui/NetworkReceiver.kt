package com.amaurypm.videogamesrf.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.amaurypm.videogamesrf.R

class NetworkReceiver(private val onNetworkAvailable: () -> Unit) : BroadcastReceiver() {

    private var wasDisconnected = false

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (isConnected) {
            if (wasDisconnected) {
                Toast.makeText(context, R.string.connection_restored, Toast.LENGTH_SHORT).show()
                onNetworkAvailable()
            }
            wasDisconnected = false
        } else {
            if (!wasDisconnected) {
                Toast.makeText(context, R.string.no_connection, Toast.LENGTH_SHORT).show()
            }
            wasDisconnected = true
        }

    }
}