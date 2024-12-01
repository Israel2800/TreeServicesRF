package com.amaurypm.videogamesrf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            delay(3000L)
            _isReady.value = true
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            // Add your data fetching logic here (e.g., call an API or load data from a repository)
            // Example:
            delay(2000L)  // Simulate network request
            _isReady.value = true  // Assuming data is ready
        }
    }
}