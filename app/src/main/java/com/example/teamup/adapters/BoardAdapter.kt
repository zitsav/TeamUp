package com.example.teamup.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.CardCreateActivity
import com.example.teamup.R
import com.example.teamup.WorkspaceActivity
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.UserDao
import com.example.teamup.databinding.CreateBoardBinding
import com.example.teamup.databinding.ItemBoardBinding
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.ChangeCardPositionRequest
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.network.CardApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardAdapter(
    private val context: Context,
    private val boardList: ArrayList<Board>,
    private val listener: OnClickListener,
    private val userDao: UserDao
) : RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

    interface OnClickListener {
        fun onClick(board: Board)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ItemBoardBinding.inflate(LayoutInflater.from(context), parent, false)
        return BoardViewHolder(binding)
    }

    override fun getItemCount(): Int = boardList.size

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val currentBoard = boardList[position]
        with(holder.binding) {
            tvBoardTitle.text = currentBoard.title

            val dividerColor = getDividerColor(currentBoard.title)
            dividerLine.setBackgroundColor(dividerColor)

            val cardAdapter = CardAdapter(
                context,
                ArrayList(currentBoard.cards),
                { menuItem, card ->
                    (context as WorkspaceActivity).handleCardOptionClick(menuItem, card)
                },
                userDao
            )
            recyclerViewCards.layoutManager = LinearLayoutManager(context)
            recyclerViewCards.adapter = cardAdapter

            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    cardAdapter.notifyItemMoved(fromPosition, toPosition)
                    updateCardPositions(currentBoard.cards, fromPosition, toPosition, currentBoard.id, context)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }
            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(recyclerViewCards)

            root.setOnClickListener {
                listener.onClick(currentBoard)
            }

            tvAddCard.setOnClickListener {
                val intent = Intent(context, CardCreateActivity::class.java).apply {
                    putExtra("BOARD_ID", currentBoard.id)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun getDividerColor(title: String): Int {
        return when (title) {
            "Ongoing" -> ContextCompat.getColor(context, R.color.yellow)
            "ToDo" -> ContextCompat.getColor(context, R.color.red)
            "Finished" -> ContextCompat.getColor(context, R.color.green)
            else -> ContextCompat.getColor(context, R.color.background)
        }
    }


    inner class BoardViewHolder(val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root)

    private fun updateCardPositions(cards: List<Card>, fromPosition: Int, toPosition: Int, boardId: Int, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val authTokenEntity = (context as AppCompatActivity).let {
                AppDatabase.getDatabase(it).userDao().getAuthToken()
            }
            val authToken = "Bearer ${authTokenEntity?.token}"
            val cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)

            val movedCard = cards[fromPosition]
            val newPosition = toPosition + 1

            val changeCardPositionRequest = ChangeCardPositionRequest(newPosition)
            val call: Call<Void> = cardApi.changeCardPosition(authToken, movedCard.id, changeCardPositionRequest)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("Success", "Card position updated successfully")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("Error", "Failed to update card position")
                }
            })
        }
    }
}