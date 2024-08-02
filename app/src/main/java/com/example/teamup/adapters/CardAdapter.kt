package com.example.teamup.adapters

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.R
import com.example.teamup.databinding.CreateBoardBinding
import com.example.teamup.databinding.ItemCardBinding
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.ChangeCardPositionRequest
import com.example.teamup.dataclasses.CreateSubtaskRequest
import com.example.teamup.dataclasses.MoveCardRequest
import com.example.teamup.network.CardApi
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.SubtaskApi
import okhttp3.internal.toImmutableList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections

class CardAdapter(
    private val context: Context,
    val cardList: ArrayList<Card>,
    private val optionClickListener: (MenuItem, Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int = cardList.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentCard = cardList[position]
        with(holder.binding) {
            tvCardTitle.text = currentCard.title
            tvCardDescription.text = currentCard.description ?: ""

            if (!currentCard.image.isNullOrEmpty()) {
                Glide.with(context)
                    .load(currentCard.image)
                    .centerCrop()
                    .into(cardIv)
            } else {
                cardIv.visibility = View.GONE
            }

            val profileImageViews = listOf(imageView4, imageView3, imageView2, imageView1)
            val profiles = currentCard.assignedUsers.map { it.user.profile }

            profileImageViews.forEachIndexed { index, imageView ->
                if (index < profiles.size) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(profiles[index])
                        .placeholder(R.drawable.user)
                        .into(imageView)
                } else {
                    imageView.visibility = View.GONE
                }
            }

            val subtasks = currentCard.subtasks.toMutableList()
            val listAdapter = ListAdapter(context, subtasks) { subtaskPosition ->
                val subtask = subtasks[subtaskPosition]
                subtasks.removeAt(subtaskPosition)
                removeSubtask(subtask.id)
            }
            holder.binding.recyclerViewList.layoutManager = LinearLayoutManager(context)
            holder.binding.recyclerViewList.adapter = listAdapter

            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    listAdapter.removeItem(position)
                    removeSubtask(subtasks[position].id)
                }
            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(recyclerViewList)

            btnAddListAtEnd.setOnClickListener {
                addList(currentCard.id, listAdapter)
            }

            btnToggle.setOnClickListener {
                holder.isRecyclerViewVisible = !holder.isRecyclerViewVisible

                val newHeight = if (holder.isRecyclerViewVisible) {
                    RecyclerView.LayoutParams.WRAP_CONTENT
                } else {
                    0
                }

                val layoutParams = recyclerViewList.layoutParams
                layoutParams.height = newHeight
                recyclerViewList.layoutParams = layoutParams

                val newBtnHeight = if (holder.isRecyclerViewVisible) {
                    RecyclerView.LayoutParams.WRAP_CONTENT
                } else {
                    0
                }

                val btnLayoutParams = btnAddListAtEnd.layoutParams
                btnLayoutParams.height = newBtnHeight
                btnAddListAtEnd.layoutParams = btnLayoutParams

                btnToggle.setImageResource(
                    if (holder.isRecyclerViewVisible) R.drawable.baseline_arrow_drop_down_24
                    else R.drawable.baseline_arrow_drop_up_24
                )
            }


            dropdownMenu.setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.menuInflater.inflate(R.menu.card_options, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    optionClickListener(menuItem, currentCard)
                    true
                }
                popupMenu.show()
            }
        }
    }

    inner class CardViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        var isRecyclerViewVisible: Boolean = false
    }

    fun removeItem(position: Int) {
        cardList.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun addList(cardId: Int, listAdapter: ListAdapter) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogBinding = CreateBoardBinding.inflate(inflater)
        builder.setView(dialogBinding.root)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val listName = dialogBinding.inputName2Board.text.toString()
            if (listName.isNotEmpty()) {
                val createSubtaskRequest = CreateSubtaskRequest(listName, cardId)
                createSubtask(createSubtaskRequest, listAdapter)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun createSubtask(request: CreateSubtaskRequest, listAdapter: ListAdapter) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AuthPrefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val subtaskApi = RetrofitInstance.getRetrofitInstance().create(SubtaskApi::class.java)
        val call: Call<Void> = subtaskApi.createSubtask(authToken, request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Subtask added successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Error", "Failed to add subtask")
            }
        })
    }

    private fun removeSubtask(subtaskId: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AuthPrefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val subtaskApi = RetrofitInstance.getRetrofitInstance().create(SubtaskApi::class.java)
        val call: Call<Void> = subtaskApi.deleteSubtask(authToken, subtaskId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Subtask removed successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Error", "Failed to remove subtask")
            }
        })
    }
}