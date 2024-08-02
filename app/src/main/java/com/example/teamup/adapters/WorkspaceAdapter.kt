package com.example.teamup.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.R
import com.example.teamup.databinding.ItemWorkspaceBinding
import com.example.teamup.dataclasses.WorkspaceX

class WorkspaceAdapter(
    private val context: Context,
    private val itemClickListener: (Int) -> Unit,
    private val optionClickListener: (Int, MenuItem) -> Unit
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
            binding.apply {
                workspaceNameTextView.text = workspace.title
                root.setOnClickListener {
                    itemClickListener.invoke(workspace.id)
                }

                if (!workspace.icon.isNullOrEmpty()) {
                    workspaceIconIv.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(workspace.icon)
                        .centerCrop()
                        .into(workspaceIconIv)
                } else {
                    workspaceIconIv.visibility = View.GONE
                }

                val profileImageViews = listOf(imageView4, imageView3, imageView2, imageView1)
                val profiles = workspace.members.map { it.user.profile }

                profileImageViews.forEachIndexed { index, imageView ->
                    if (index < profiles.size) {
                        imageView.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(profiles[index])
                            .placeholder(R.drawable.user)
                            .into(imageView)
                    } else {
                        imageView.visibility = View.INVISIBLE
                    }
                }

                dropdownMenu.setOnClickListener {
                    showPopupMenu(it, workspace.id)
                }
            }
        }

        private fun showPopupMenu(view: View, workspaceId: Int) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.workspace_options)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                optionClickListener.invoke(workspaceId, menuItem)
                true
            }
            popupMenu.show()
        }
    }
}