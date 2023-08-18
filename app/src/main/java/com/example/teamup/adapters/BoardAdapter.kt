package com.example.teamup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Board

class BoardAdapter : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    private val boardList: MutableList<Board> = mutableListOf()

    fun submitList(newList: List<Board>) {
        boardList.clear()
        boardList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_board, parent, false)
        return BoardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = boardList[position]
        holder.bind(board)
    }

    override fun getItemCount(): Int = boardList.size

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_board_title)
        private val cardRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_cards)

        fun bind(board: Board) {
            titleTextView.text = board.title

            val cardAdapter = CardAdapter()
            cardRecyclerView.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            cardRecyclerView.adapter = cardAdapter
            cardAdapter.submitList(board.cards)
        }
    }
}