package com.example.teamup.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.R
import com.example.teamup.databinding.ItemMemberBinding

class MemberAdapter(
    private val context: Context,
    private val profiles: List<String>
) : RecyclerView.Adapter<MemberAdapter.ProfileImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileImageViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProfileImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileImageViewHolder, position: Int) {
        val profileUrl = profiles[position]
        Glide.with(context)
            .load(profileUrl)
            .placeholder(R.drawable.user)
            .into(holder.binding.imageView)

        Log.d("MemberAdapter", "Loading image at position: $position with URL: $profileUrl")
    }

    override fun getItemCount(): Int {
        // Limiting to the first 4 items
        return if (profiles.size > 4) 4 else profiles.size
    }

    inner class ProfileImageViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)
}