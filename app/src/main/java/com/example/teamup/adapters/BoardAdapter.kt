package com.example.teamup.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
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
import com.example.teamup.interfaces.BoardApi
import com.example.teamup.interfaces.CardApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardAdapter(
    private val context: Context,
    private val boardList: ArrayList<Board>,
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    interface OnClickListener {
        fun onClick(board: Board)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        return BoardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val currentBoard = boardList[position]
        holder.titleTextView.text = currentBoard.title

        val cardAdapter = CardAdapter(context, ArrayList(currentBoard.cards))
        holder.cardRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.cardRecyclerView.adapter = cardAdapter

        holder.addCardTextView.setOnClickListener {
            showAddCardDialog(currentBoard.id, cardAdapter)
        }
    }

    override fun getItemCount(): Int {
        return boardList.size
    }

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_board_title)
        val addCardTextView: TextView = itemView.findViewById(R.id.tv_add_card)
        val cardRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_cards)
    }

    private fun showAddCardDialog(boardId: Int, cardAdapter: CardAdapter) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.create_board, null)
        builder.setView(dialogView)

        val cardNameEditText: EditText = dialogView.findViewById(R.id.inputName2Board)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val cardName = cardNameEditText.text.toString()
            if (cardName.isNotEmpty()) {
                val createCardRequest = CreateCardRequest(boardId, "", cardName)
                createCard(createCardRequest)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun createCard(request: CreateCardRequest) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            "AuthPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)
        val call: Call<Void> = cardApi.createCard(authToken, request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Notify the activity to refresh
                    (context as? AppCompatActivity)?.recreate()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure if needed
            }
        })
    }
}