package com.test.cct

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), SocketHandler.SocketMessageListener {

    private val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<Pair<Boolean, String>>()
    val messages: LiveData<Pair<Boolean, String>> = _messages

    fun addMessage(message: Pair<Boolean, String>) = viewModelScope.launch(Dispatchers.Main) {
        if (_socketStatus.value == true) {
            _messages.value = message
        }
    }

    override fun setStatus(status: Boolean){
        viewModelScope.launch(Dispatchers.Main) {
            _socketStatus.value = status
        }
    }

    override fun onMessage(message: Pair<Boolean, String>) {
        addMessage(message)
    }
}