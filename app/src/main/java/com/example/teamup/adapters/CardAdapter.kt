package com.example.teamup.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.RetrofitInstance
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.dataclasses.CreateListRequest
import com.example.teamup.interfaces.CardApi
import com.example.teamup.interfaces.ListApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardAdapter(
    private val context: Context,
    private val cardList: ArrayList<Card>,
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitleTextView: TextView = itemView.findViewById(R.id.tv_card_title)
        val listRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_list)
        val addListTextView: TextView = itemView.findViewById(R.id.tv_add_task_list)
    }

    private fun addList(cardId: Int, listAdapter: ListAdapter) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.create_board, null)
        builder.setView(dialogView)

        val listNameEditText: EditText = dialogView.findViewById(R.id.inputName2Board)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val listName = listNameEditText.text.toString()
            if (listName.isNotEmpty()) {
                val createListRequest = CreateListRequest(cardId, listName)
                createList(createListRequest, listAdapter)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun createList(request: CreateListRequest, listAdapter: ListAdapter) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "AuthPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val listApi = RetrofitInstance.getRetrofitInstance().create(ListApi::class.java)
        val call: Call<Void> = listApi.createList(authToken, request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    (context as? AppCompatActivity)?.recreate()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
            }
        })
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentCard = cardList[position]
        holder.cardTitleTextView.text = currentCard.title

        val listAdapter = ListAdapter(context, ArrayList(currentCard.lists))
        holder.listRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.listRecyclerView.adapter = listAdapter

        holder.addListTextView.setOnClickListener {
            addList(currentCard.id, listAdapter)
        }
    }
}