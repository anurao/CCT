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
import android.os.Bundle
import com.test.cct.R
import com.test.cct.DemoListActivity.ActivityDesc
import com.test.cct.DemoListActivity.ActivityListAdapter
import com.test.cct.CustomUIActivity
import com.test.cct.SocketActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.test.cct.CustomTabActivityHelper
import android.app.Activity
import android.content.Context
import android.widget.TextView
import android.view.LayoutInflater
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import java.util.ArrayList

class DemoListActivity : AppCompatActivity() {
    private var enable: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        if (intent.scheme != null && intent.scheme.equals("cctapp", ignoreCase = true)) {
            if (intent.hasExtra("update")) enable = intent.getStringExtra("update")
        }
        val activityDescList: MutableList<ActivityDesc> = ArrayList()
        val listAdapter = ActivityListAdapter(this, activityDescList)
        var activityDesc = createActivityDesc(
            R.string.title_activity_customized_chrome_tab,
            R.string.description_activity_customized_chrome_tab,
            CustomUIActivity::class.java
        )
        activityDescList.add(activityDesc)
        activityDesc = createActivityDesc(
            R.string.socket_activity,
            R.string.socket_activity_desc,
            SocketActivity::class.java
        )
        activityDescList.add(activityDesc)
        val recyclerView = findViewById<RecyclerView>(android.R.id.list)
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    public override fun onResume() {
        super.onResume()
    }

    private fun createActivityDesc(
        titleId: Int, descriptionId: Int,
        activity: Class<out Activity?>
    ): ActivityDesc {
        val activityDesc = ActivityDesc()
        activityDesc.mTitle = getString(titleId)
        activityDesc.mDescription = getString(descriptionId)
        activityDesc.mActivity = activity
        return activityDesc
    }

    private class ActivityDesc {
        var mTitle: String? = null
        var mDescription: String? = null
        var mActivity: Class<out Activity?>? = null
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /* package */
        var mTitleTextView: TextView

        /* package */
        var mDescriptionTextView: TextView

        /* package */
        var mPosition = 0

        init {
            mTitleTextView = itemView.findViewById(R.id.title)
            mDescriptionTextView = itemView.findViewById(R.id.description)
        }
    }

    private class ActivityListAdapter(
        private val context: Context,
        private val mActivityDescs: List<ActivityDesc>
    ) : RecyclerView.Adapter<ViewHolder>(), View.OnClickListener {
        private val mLayoutInflater: LayoutInflater

        init {
            mLayoutInflater = LayoutInflater.from(context)
        }

        override fun onClick(v: View) {
            val position = (v.tag as ViewHolder).mPosition
            val activityDesc = mActivityDescs[position]
            val intent = Intent(context, activityDesc.mActivity)
            context.startActivity(intent)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
            val v = mLayoutInflater.inflate(R.layout.item_example_description, viewGroup, false)
            val viewHolder = ViewHolder(v)
            v.setOnClickListener(this)
            v.tag = viewHolder
            return viewHolder
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val activityDesc = mActivityDescs[position]
            val title = activityDesc.mTitle
            val description = activityDesc.mDescription
            viewHolder.mTitleTextView.text = title
            viewHolder.mDescriptionTextView.text = description
            viewHolder.mPosition = position
        }

        override fun getItemCount(): Int {
            return mActivityDescs.size
        }
    }
}