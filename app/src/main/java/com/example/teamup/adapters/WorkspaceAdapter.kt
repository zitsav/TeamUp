package com.example.teamup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.GetAllWorkspaceResponseItem

class WorkspaceAdapter(private val itemClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder>() {

    private val workspaceList = mutableListOf<GetAllWorkspaceResponseItem>()

    fun submitList(workspaces: List<GetAllWorkspaceResponseItem>) {
        workspaceList.clear()
        workspaceList.addAll(workspaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_workspace, parent, false)
        return WorkspaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        val workspace = workspaceList[position]
        holder.bind(workspace)
    }

    override fun getItemCount(): Int = workspaceList.size

    inner class WorkspaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workspaceNameTextView: TextView = itemView.findViewById(R.id.workspaceNameTextView)

        fun bind(workspace: GetAllWorkspaceResponseItem) {
            workspaceNameTextView.text = workspace.title
            itemView.setOnClickListener {
                itemClickListener.invoke(workspace.id)
            }
        }
    }
}