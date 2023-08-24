package com.test.cct

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

object SocketHandler {
    private val TAG = "SamsungSocketDrop"
    private lateinit var webSocket: okhttp3.WebSocket
    private var socketUrl = ""
    private var socketMessageListener:SocketMessageListener? = null
    private var shouldReconnect = true
    private var client: OkHttpClient = OkHttpClient()
    private lateinit var socketListener: SocketListener

    fun setListener(listener: SocketMessageListener) {
        this.socketMessageListener = listener
    }

    fun addListener(listener: SocketMessageListener) {
        if(this::socketListener.isInitialized) socketListener.setSocketListener(listener)
    }

    fun setSocketUrl(socketUrl: String) {
        this.socketUrl = socketUrl
    }

    private fun initWebSocket() {
        Log.e(TAG, "initWebSocket() socketurl = $socketUrl")
        client = OkHttpClient()
        val request = Request.Builder().url(url = socketUrl).build()
        socketListener = SocketListener(socketMessageListener)
        webSocket = client.newWebSocket(request, socketListener)
        //this must me done else memory leak will be caused
    }

    fun connect() {
        Log.e(TAG, "connect()")
        shouldReconnect = true
        initWebSocket()
    }

    fun reconnect() {
        Log.e(TAG, "reconnect()")
        initWebSocket()
    }

    //send
    fun sendMessage(message: String) {
        Log.e(TAG, "sendMessage($message)")
        if (::webSocket.isInitialized) {
            Log.e(TAG, "sendingMessage($message)")
            webSocket.send(message)
        }
    }

    fun disconnect() {
        if (::webSocket.isInitialized) webSocket.close(1000, "Close Manually.")
        shouldReconnect = false
        client.dispatcher.executorService.shutdown()
    }

    interface SocketMessageListener {
        fun onMessage(message: Pair<Boolean, String>)
        fun setStatus(connected:Boolean)
    }
}