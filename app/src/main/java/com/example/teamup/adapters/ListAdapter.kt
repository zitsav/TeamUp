package com.example.teamup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Lists

class ListAdapter(
    private val context: Context,
    private val listItems: ArrayList<Lists>,
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentListItem = listItems[position]
        holder.listTitleTextView.text = currentListItem.title
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listTitleTextView: TextView = itemView.findViewById(R.id.et_task_list_name)
    }
}