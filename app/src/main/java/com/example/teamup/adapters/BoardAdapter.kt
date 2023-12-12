package com.example.teamup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card

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
    }

    override fun getItemCount(): Int {
        return boardList.size
    }

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_board_title)
        val cardRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_cards)
    }
}