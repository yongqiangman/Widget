/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.widget.app

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yqman.wdiget.recyclerView.BaseRecyclerViewAdapter

class SimpleAdapter: BaseRecyclerViewAdapter<Item>() {

    var data : List<Item>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getChildrenItemCount(): Int {
        return data?.size?:0
    }

    override fun onCreateChildrenViewHolder(parent: ViewGroup, viewType: Int): android.support.v7.widget.RecyclerView.ViewHolder {
        Log.d("SimpleAdapter", "createType $viewType")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple, parent, false)
        return NormalViewHolder(view)
    }

    override fun onBindChildrenViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("SimpleAdapter", "bind $position")
        val value = data?.get(position)
        if (holder is NormalViewHolder) {
            holder.textView.text = value?.name
            holder.itemView.setOnClickListener {
                if (value != null) {
                    mItemClickListener?.onItemClick(value)
                }
            }
        }
    }
}

class Item(val name: String)

class NormalViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.textView)
}
