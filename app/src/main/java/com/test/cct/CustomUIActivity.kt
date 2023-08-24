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

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import android.app.PendingIntent
import android.content.Context
import com.test.cct.ActionBroadcastReceiver
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.RemoteViews
import java.lang.NumberFormatException

/**
 * Opens Chrome Custom Tabs with a customized UI.
 */
class CustomUIActivity : AppCompatActivity(), View.OnClickListener {
    private var mUrlEditText: EditText? = null
    private var mCustomTabColorEditText: EditText? = null
    private var mCustomTabSecondaryColorEditText: EditText? = null
    private var mShowActionButtonCheckbox: CheckBox? = null
    private var mAddMenusCheckbox: CheckBox? = null
    private var mShowTitleCheckBox: CheckBox? = null
    private var mCustomBackButtonCheckBox: CheckBox? = null
    private var mAutoHideAppBarCheckbox: CheckBox? = null
    private var mAddDefaultShareCheckbox: CheckBox? = null
    private var mToolbarItemCheckbox: CheckBox? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_ui)
        findViewById<View>(R.id.start_custom_tab).setOnClickListener(this)
        mUrlEditText = findViewById(R.id.url)
        mCustomTabColorEditText = findViewById(R.id.custom_toolbar_color)
        mCustomTabSecondaryColorEditText = findViewById(R.id.custom_toolbar_secondary_color)
        mShowActionButtonCheckbox = findViewById(R.id.custom_show_action_button)
        mAddMenusCheckbox = findViewById(R.id.custom_add_menus)
        mShowTitleCheckBox = findViewById(R.id.show_title)
        mCustomBackButtonCheckBox = findViewById(R.id.custom_back_button)
        mAutoHideAppBarCheckbox = findViewById(R.id.auto_hide_checkbox)
        mAddDefaultShareCheckbox = findViewById(R.id.add_default_share)
        mToolbarItemCheckbox = findViewById(R.id.add_toolbar_item)
    }

    override fun onStart() {
        super.onStart()
        CustomTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        //mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    override fun onClick(v: View) {
        val viewId = v.id
        when (viewId) {
            R.id.start_custom_tab -> openCustomTab()
            else -> {}
        }
    }

    private fun getColor(editText: EditText?): Int {
        return try {
            Color.parseColor(editText!!.text.toString())
        } catch (ex: NumberFormatException) {
            Log.i(TAG, "Unable to parse Color: " + editText!!.text)
            Color.LTGRAY
        }
    }

    private fun openCustomTab() {
        val url = mUrlEditText!!.text.toString()
        val color = getColor(mCustomTabColorEditText)
        val secondaryColor = getColor(mCustomTabSecondaryColorEditText)
        val intentBuilder = CustomTabsIntent.Builder(
            CustomTabActivityHelper.session
        )
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(color)
            .setSecondaryToolbarColor(secondaryColor)
            .build()
        intentBuilder.setDefaultColorSchemeParams(defaultColors)
        if (mShowActionButtonCheckbox!!.isChecked) {
            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
            //UI thread to keep the example short.
            val actionLabel = getString(R.string.label_action)
            val icon = BitmapFactory.decodeResource(
                resources,
                android.R.drawable.ic_menu_share
            )
            val pendingIntent = createPendingIntent(ActionBroadcastReceiver.ACTION_ACTION_BUTTON)
            intentBuilder.setActionButton(icon, actionLabel, pendingIntent)
        }
        if (mAddMenusCheckbox!!.isChecked) {
            val menuItemTitle = getString(R.string.menu_item_title)
            val menuItemPendingIntent =
                createPendingIntent(ActionBroadcastReceiver.ACTION_MENU_ITEM)
            intentBuilder.addMenuItem(menuItemTitle, menuItemPendingIntent)
        }
        val shareState =
            if (mAddDefaultShareCheckbox!!.isChecked) CustomTabsIntent.SHARE_STATE_ON else CustomTabsIntent.SHARE_STATE_OFF
        intentBuilder.setShareState(shareState)
        if (mToolbarItemCheckbox!!.isChecked) {
            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
            //UI thread to keep the example short.
            val actionLabel = getString(R.string.label_action)
            val icon = BitmapFactory.decodeResource(
                resources,
                android.R.drawable.ic_menu_share
            )
            val pendingIntent = createPendingIntent(ActionBroadcastReceiver.ACTION_TOOLBAR_TEST)
            //intentBuilder.addToolbarItem(TOOLBAR_ITEM_ID, icon, actionLabel, pendingIntent);
            val clickableIDs = intArrayOf(R.id.dig_layout)
            intentBuilder.setSecondaryToolbarViews(getRemoteView(this), clickableIDs, pendingIntent)
        }
        intentBuilder.setShowTitle(mShowTitleCheckBox!!.isChecked)
        intentBuilder.setUrlBarHidingEnabled(mAutoHideAppBarCheckbox!!.isChecked)
        if (mCustomBackButtonCheckBox!!.isChecked) {
            intentBuilder.setCloseButtonIcon(toBitmap(getDrawable(R.drawable.ic_arrow_back)))
        }
        intentBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        intentBuilder.setExitAnimations(
            this, android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        val intent = intentBuilder.build()
        CustomTabActivityHelper.openCustomTab(
            this, intent, Uri.parse(url))
    }

    private fun createPendingIntent(actionSourceId: Int): PendingIntent {
        val actionIntent = Intent(
            this.applicationContext, ActionBroadcastReceiver::class.java
        )
        actionIntent.putExtra(ActionBroadcastReceiver.KEY_ACTION_SOURCE, actionSourceId)
        return PendingIntent.getBroadcast(
            applicationContext, actionSourceId, actionIntent, PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Return a Bitmap representation of the Drawable. Based on Android KTX.
     */
    private fun toBitmap(drawable: Drawable?): Bitmap {
        val width = drawable!!.intrinsicWidth
        val height = drawable.intrinsicHeight
        val oldBounds = Rect(drawable.bounds)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(Canvas(bitmap))
        drawable.bounds = oldBounds
        return bitmap
    }

    private fun getRemoteView(context: Context): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.bottom_bar)
        remoteViews.setViewVisibility(R.id.bar_parent, View.GONE)
        val digIcon = R.drawable.ic_baseline_directions_boat_24
        remoteViews.setImageViewResource(R.id.dig_action, digIcon)
        val flagIcon = R.drawable.ic_baseline_directions_bus_24
        remoteViews.setImageViewResource(R.id.flag_action, flagIcon)
        return remoteViews
    }

    /**
     * @return The PendingIntent that will be triggered when the user clicks on the Views listed by
     */
    private fun getPendingIntent(context: Context): PendingIntent {
        val digIntent = Intent(context, ActionBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, digIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private const val TAG = "CustChromeTabActivity"
        private const val TOOLBAR_ITEM_ID = 1
        private const val EXTRA_CUSTOM_TABS_KEEP_ALIVE =
            "android.support.customtabs.extra.KEEP_ALIVE"
    }
}