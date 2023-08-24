package com.test.cct

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class SocketFragment : Fragment() {

    companion object {
        fun newInstance() = SocketFragment()
    }

    private lateinit var viewModel: MainViewModel
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var shoulsStartService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        SocketHandler.setListener(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_socket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageET = view.findViewById<EditText>(R.id.messageET)
        val sendMessageButton  = view.findViewById<ImageButton>(R.id.sendButton)
        val connectButton = view.findViewById<Button>(R.id.connectButton)
        val disconnectButton = view.findViewById<Button>(R.id.disconnectButton)
        val statusTV = view.findViewById<TextView>(R.id.statusTV)
        val messageTV = view.findViewById<TextView>(R.id.messageTV)
        val deeplinkButton = view.findViewById<Button>(R.id.deeplinkButton)
        val startService = view.findViewById<CheckBox>(R.id.service_checkbx)

        viewModel.socketStatus.observe(viewLifecycleOwner) {
            statusTV.text = if (it) "Connected" else "Disconnected"
        }

        var text = ""
        viewModel.messages.observe(viewLifecycleOwner) {
            text += "${if (it.first) "You: " else "Other: "} ${it.second}\n"
            messageTV.text = text
        }

        connectButton.setOnClickListener {
            //webSocketHandler.setSocketUrl("wss://socketsbay.com/wss/v2/1/demo/cct")
            SocketHandler.setSocketUrl("wss://echo.websocket.events")
            SocketHandler.connect()
            if(shoulsStartService) startService()
        }

        disconnectButton.setOnClickListener {
            SocketHandler.disconnect()
            stopService()
        }

        deeplinkButton.setOnClickListener {
            openActivity()
        }
        sendMessageButton.setOnClickListener {
            SocketHandler.sendMessage(messageET.text.toString())
            viewModel.addMessage(Pair(true, messageET.text.toString()))
            messageET.text.clear()
            messageET.onEditorAction(EditorInfo.IME_ACTION_DONE);
        }

        startService.setOnCheckedChangeListener { buttonView, isChecked ->
            shoulsStartService = isChecked
        }
    }

    fun startService() {
        val serviceIntent = Intent(activity, ForgroundSocketService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android")
        ContextCompat.startForegroundService(context!!, serviceIntent)
    }

    fun stopService() {
        val serviceIntent = Intent(activity, ForgroundSocketService::class.java)
        activity?.stopService(serviceIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SocketHandler.disconnect()
    }

    private fun openActivity() {
        val intent = Intent(this.activity,  CustomUIActivity::class.java)
        activity?.startActivity(intent)
    }
}