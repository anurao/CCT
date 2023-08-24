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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.test.cct.ActionBroadcastReceiver
import com.test.cct.R

/**
 * A BroadcastReceiver that handles the Action Intent from the Custom Tab and shows the Url
 * in a Toast.
 */
class ActionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val url = intent.dataString
        if (url != null) {
            val toastText = getToastText(context, intent.getIntExtra(KEY_ACTION_SOURCE, -1), url)
            //Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    private fun getToastText(context: Context, actionId: Int, url: String): String {
        return when (actionId) {
            ACTION_ACTION_BUTTON -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("cctapp://linktofile")
                intent.putExtra("update", "update")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                context.getString(R.string.action_button_toast_text, url)
            }
            ACTION_MENU_ITEM -> {
                val menu_intent = Intent(Intent.ACTION_VIEW)
                menu_intent.data = Uri.parse("cctapp://linktofile")
                menu_intent.putExtra("update", "clear")
                menu_intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(menu_intent)
                context.getString(R.string.menu_item_toast_text, url)
            }
            ACTION_TOOLBAR -> {
                val shareIntent = Intent(Intent.ACTION_VIEW)
                shareIntent.data = Uri.parse("cctapp://linktofile")
                shareIntent.putExtra("update", "clear")
                shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(shareIntent)
                //CustomTabActivityHelper.getInstance().updateToolbar(false, context);
                context.getString(R.string.toolbar_toast_text, url)
            }
            ACTION_TOOLBAR_TEST -> {
                val anIntent = Intent(Intent.ACTION_VIEW)
                anIntent.data = Uri.parse("cctapp://linktofile")
                anIntent.putExtra("update", "update")
                anIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(anIntent)
                //CustomTabActivityHelper.getInstance().updateToolbar(false, context);
                context.getString(R.string.toolbar_toast_text, url)
            }
            else -> context.getString(R.string.unknown_toast_text, url)
        }
    }

    companion object {
        const val KEY_ACTION_SOURCE = "org.chromium.customtabsdemos.ACTION_SOURCE"
        const val ACTION_ACTION_BUTTON = 1
        const val ACTION_MENU_ITEM = 2
        const val ACTION_TOOLBAR = 3
        const val ACTION_TOOLBAR_TEST = 4
    }
}