package com.example.teamup.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card

class CardAdapter(
    private val context: Context,
    private val cardList: ArrayList<Card>,
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentCard = cardList[position]
        holder.cardTitleTextView.text = currentCard.title

        val listAdapter = ListAdapter(context, ArrayList(currentCard.lists))
        holder.listRecyclerView.layoutManager = LinearLayoutManager(context)
        holder.listRecyclerView.adapter = listAdapter
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitleTextView: TextView = itemView.findViewById(R.id.tv_card_title)
        val listRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_list)
    }
}