package com.test.cct

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SocketListener(socketMessageListener: SocketHandler.SocketMessageListener?): WebSocketListener() {

        private val TAG = "SamsungSocketDrop"
        var listeners = mutableListOf<SocketHandler.SocketMessageListener>()
        init{
            socketMessageListener?.let {
                listeners.add(socketMessageListener)
            }
        }

        fun setSocketListener(socketMessageListener: SocketHandler.SocketMessageListener) {
            listeners.add(socketMessageListener)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            for (listener in listeners) {
                listener.setStatus(true)
            }
            webSocket.send("Android Device Connected")
            Log.d(TAG, "onOpen:")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            for (listener in listeners) {
                listener.onMessage(Pair(false, text))
            }

            Log.d(TAG, "onMessage: $text")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d(TAG, "onClosing: $code $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            for (listener in listeners) {
                listener.setStatus(false)
            }
            Log.d(TAG, "onClosed: $code $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(TAG, "onFailure: ${t.message} $response")
            for (listener in listeners) {
                listener.onMessage(Pair(false, "onFailure: ${t.message} $response"))
                listener.setStatus(false)
            }
            super.onFailure(webSocket, t, response)
        }
    }
