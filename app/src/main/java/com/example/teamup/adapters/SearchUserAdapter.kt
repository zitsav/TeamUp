package com.example.teamup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.R
import com.example.teamup.dataclasses.User

class SearchUserAdapter(
    private val context: Context,
    private var userList: ArrayList<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false)
        return SearchUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    fun updateUsers(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }

    inner class SearchUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)

        fun bind(user: User) {
            tvName.text = user.name
            tvEmail.text = user.email
            Glide.with(context)
                .load(user.profile ?: R.drawable.user)
                .into(ivProfile)
            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}