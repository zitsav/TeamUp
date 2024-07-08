package com.example.teamup.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.databinding.ListItemBinding
import com.example.teamup.dataclasses.EditSubtaskRequest
import com.example.teamup.dataclasses.Subtask
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.SubtaskApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListAdapter(
    private val context: Context,
    private val listItems: MutableList<Subtask>,
    private val onItemSwiped: (Int) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentListItem = listItems[position]
        holder.binding.etTaskListName.text = currentListItem.title

        if (currentListItem.isDone){
            holder.binding.ibDoneListName.visibility = View.VISIBLE
            holder.binding.ibDoneListName.setOnClickListener{
                editSubtask(currentListItem.id, false)
            }
        }
        else{
            holder.binding.ibDoneListName.visibility = View.INVISIBLE
            holder.binding.checkbox.setOnClickListener{
                editSubtask(currentListItem.id, true)
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    inner class ListViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun removeItem(position: Int) {
        listItems.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun editSubtask(subtaskId: Int, isDone: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AuthPrefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val subtaskApi = RetrofitInstance.getRetrofitInstance().create(SubtaskApi::class.java)
        val call: Call<Void> = subtaskApi.editSubtask(authToken, subtaskId, EditSubtaskRequest(isDone = isDone))

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Subtask status changed successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Error", "Failed to change subtask status")
            }
        })
    }
}