package com.example.teamup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.databinding.ItemWorkspaceBinding
import com.example.teamup.dataclasses.WorkspaceX

class WorkspaceAdapter(
    private val context: Context,
    private val itemClickListener: (Int) -> Unit
) : RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder>() {

    private val workspaceList = mutableListOf<WorkspaceX>()

    fun submitList(workspaces: List<WorkspaceX>) {
        workspaceList.clear()
        workspaceList.addAll(workspaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = ItemWorkspaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkspaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        val workspace = workspaceList[position]
        holder.bind(workspace)
    }

    override fun getItemCount(): Int = workspaceList.size

    inner class WorkspaceViewHolder(private val binding: ItemWorkspaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workspace: WorkspaceX) {
            binding.workspaceNameTextView.text = workspace.title
            binding.root.setOnClickListener {
                itemClickListener.invoke(workspace.id)
            }

            val profiles = workspace.members.take(4).mapNotNull { it.user.profile }

            val memberAdapter = MemberAdapter(context, profiles)
            binding.membersRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = memberAdapter
            }


            if (!workspace.icon.isNullOrEmpty()) {
                binding.workspaceIconIv.visibility = View.VISIBLE
                Glide.with(context)
                    .load(workspace.icon)
                    .centerCrop()
                    .into(binding.workspaceIconIv)
            } else {
                binding.workspaceIconIv.visibility = View.GONE
            }
        }
    }
}