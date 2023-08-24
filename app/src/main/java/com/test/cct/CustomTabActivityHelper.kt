// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.test.cct

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.browser.customtabs.*
import com.test.cct.ActionBroadcastReceiver
import java.lang.ref.WeakReference
import java.util.*

/**
 * This is a helper class to manage the connection to the Custom Tabs Service.
 */
object CustomTabActivityHelper : ServiceConnectionCallback, SocketHandler.SocketMessageListener {
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mClient: CustomTabsClient? = null
    private var mConnection: CustomTabsServiceConnection?=null
    private var mConnectionCallback: ConnectionCallback? = null
    private var remoteViews: RemoteViews? = null
    private var pendingIntent: PendingIntent? = null
    private var toolbaraction = ""
    lateinit var clickableIDs: IntArray
    private var timer: Timer? = null
    lateinit var context: WeakReference<Context>
    var count:Int = 0

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     * @param activity the activity that is connected to the service.
     */
    fun unbindCustomTabsService(activity: Activity) {
        if (mConnection == null) return
        activity.unbindService(mConnection!!)
        mClient = null
        mCustomTabsSession = null
        mConnection = null
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    val session: CustomTabsSession?
        get() {
            if (mClient == null) {
                mCustomTabsSession = null
            } else if (mCustomTabsSession == null) {
                mCustomTabsSession = mClient!!.newSession(customTabsCallback)
            }
            return mCustomTabsSession
        }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     */
    fun setConnectionCallback(connectionCallback: ConnectionCallback?) {
        mConnectionCallback = connectionCallback
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     * @param activity the activity to be binded to the service.
     */
    fun bindCustomTabsService(activity: Activity) {
        if (mClient != null) return
        context = WeakReference(activity.baseContext)
        val packageName = CustomTabsHelper.getPackageNameToUse(activity.applicationContext) ?: return
        mConnection = ServiceConnection(this)
        mConnection?.let { CustomTabsClient.bindCustomTabsService(activity!!, packageName, it) }

    }

    /**
     * @see {@link CustomTabsSession.mayLaunchUrl
     * @return true if call to mayLaunchUrl was accepted.
     */
    fun mayLaunchUrl(uri: Uri?, extras: Bundle?, otherLikelyBundles: List<Bundle?>?): Boolean {
        if (mClient == null) return false
        val session = session ?: return false
        return session.mayLaunchUrl(uri, extras, otherLikelyBundles)
    }

    override fun onServiceConnected(client: CustomTabsClient) {
        mClient = client
        mClient!!.warmup(0L)
        if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsConnected()
    }

    override fun onServiceDisconnected() {
        mClient = null
        mCustomTabsSession = null
        if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsDisconnected()
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        fun onCustomTabsConnected()

        /**
         * Called when the service is disconnected.
         */
        fun onCustomTabsDisconnected()
    }

    private fun getRemoteView(context: Context, show: Boolean, count: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.bottom_bar)
        if (show) {
            val digIcon = R.drawable.ic_baseline_directions_boat_24
            remoteViews.setImageViewResource(R.id.dig_action, digIcon)
            val flagIcon = R.drawable.ic_baseline_directions_bus_24
            remoteViews.setImageViewResource(R.id.flag_action, flagIcon)
            remoteViews.setTextViewText(R.id.barker, Integer.toString(count))
        } else {
            remoteViews.setViewVisibility(R.id.bar_parent, View.GONE)
            remoteViews.setViewVisibility(R.id.dig_action, View.GONE)
            remoteViews.setViewVisibility(R.id.flag_action, View.GONE)
            remoteViews.setViewVisibility(R.id.barker, View.GONE)
            //            int digIcon = R.drawable.ic_baseline_directions_boat_24;
//            int flagIcon = R.drawable.ic_baseline_directions_bus_24;
//            remoteViews.setImageViewResource(R.id.dig_action, flagIcon);
//            remoteViews.setImageViewResource(R.id.flag_action, digIcon);
        }
        return remoteViews
    }

    /**
     * @return The PendingIntent that will be triggered when the user clicks on the Views listed by
     */
    private fun getPendingIntent(context: Context, acttionID: Int): PendingIntent {
        val digIntent = Intent(context, ActionBroadcastReceiver::class.java)
        digIntent.putExtra(ActionBroadcastReceiver.KEY_ACTION_SOURCE, acttionID)
        return PendingIntent.getBroadcast(context, 0, digIntent, PendingIntent.FLAG_IMMUTABLE)
    }

     fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                SocketHandler.sendMessage("Message in bkgnd : " + count)
                count++
            }
        }, 5, 20000)
    }

    private fun stopTimer() {
        if (timer != null) {
            count = 0
            timer!!.cancel()
        }
        timer = null
    }

    private fun updateBarker(update: String, context: Context, count: Int) {
        if (mCustomTabsSession == null) return
        toolbaraction = update
        if (update.equals("update", ignoreCase = true)) {
            clickableIDs = intArrayOf(R.id.dig_layout)
            pendingIntent = getPendingIntent(context, ActionBroadcastReceiver.ACTION_TOOLBAR)
            remoteViews = getRemoteView(context, true, count)
            mCustomTabsSession?.setSecondaryToolbarViews(remoteViews, clickableIDs, pendingIntent)
        } else if (update.equals("clear", ignoreCase = true)) {
            // mCustomTabsSession.setSecondaryToolbarViews(null, null, null);
            //mCustomTabsSession.setSecondaryToolbarViews(getRemoteView(context, false), null, null);
            clickableIDs = intArrayOf()
            pendingIntent = null
            // getPendingIntent(context, ActionBroadcastReceiver.ACTION_TOOLBAR);
            remoteViews = getRemoteView(context, false, count)
            mCustomTabsSession?.setSecondaryToolbarViews(remoteViews, clickableIDs, pendingIntent)
        }
    }

    fun setListener() {
        SocketHandler.addListener(this)
    }

    var customTabsCallback: CustomTabsCallback = object : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            Log.i("CCT", "onNavigationEvent: Code = $navigationEvent")
            when (navigationEvent) {
                NAVIGATION_STARTED -> {}
                NAVIGATION_FINISHED -> {}
                NAVIGATION_FAILED -> {}
                NAVIGATION_ABORTED -> {}
                TAB_HIDDEN -> {
                    stopTimer()
                }
                TAB_SHOWN -> {
                    setListener()
                    startTimer()
                }
            }
        }
    }
    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity The host activity.
     * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
     * @param uri the Uri to be opened.
     * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
     */
    fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri) {
        val packageName = CustomTabsHelper.getPackageNameToUse(activity)
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(activity, uri)
    }

    override fun onMessage(message: Pair<Boolean, String>) {
        if(this::context.isInitialized) {
            context.get()?.let {
                Log.i("SamsungSocketDrop", "onMessage Update Tab: " +message.second)
                updateBarker("update", it, count)
            }
        }
    }

    override fun setStatus(connected: Boolean) {
        //not needed
    }
}