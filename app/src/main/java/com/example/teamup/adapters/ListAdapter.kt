package com.example.teamup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Lists

class ListAdapter : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private val listList: MutableList<Lists> = mutableListOf()

    fun submitList(newList: List<Lists>) {
        listList.clear()
        listList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listItem = listList[position]
        holder.bind(listItem)
    }

    override fun getItemCount(): Int = listList.size

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.et_task_list_name)

        fun bind(listItem: Lists) {
            titleTextView.text = listItem.title
        }
    }
}