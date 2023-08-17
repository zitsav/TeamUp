package com.example.teamup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.R
import com.example.teamup.dataclasses.Card

class CardAdapter : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private val cardList: MutableList<Card> = mutableListOf()

    fun submitList(newList: List<Card>) {
        cardList.clear()
        cardList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.bind(card)
    }

    override fun getItemCount(): Int = cardList.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_card_title)
        private val listRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view_list)

        fun bind(card: Card) {
            titleTextView.text = card.title

            val listAdapter = ListAdapter()
            listRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            listRecyclerView.adapter = listAdapter
            listAdapter.submitList(card.lists)
        }
    }
}
