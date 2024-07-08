package com.example.teamup.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.CardCreateActivity
import com.example.teamup.R
import com.example.teamup.databinding.CreateBoardBinding
import com.example.teamup.databinding.ItemBoardBinding
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.network.CardApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardAdapter(
    private val context: Context,
    private val boardList: ArrayList<Board>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    interface OnClickListener {
        fun onClick(board: Board)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val currentBoard = boardList[position]
        holder.binding.tvBoardTitle.text = currentBoard.title

        val cardAdapter = CardAdapter(context, ArrayList(currentBoard.cards))
        holder.binding.recyclerViewCards.layoutManager = LinearLayoutManager(context)
        holder.binding.recyclerViewCards.adapter = cardAdapter

        holder.binding.tvAddCard.setOnClickListener {
            val intent = Intent(context, CardCreateActivity::class.java)
            intent.putExtra("BOARD_ID", currentBoard.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = boardList.size

    inner class BoardViewHolder(val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root)
}