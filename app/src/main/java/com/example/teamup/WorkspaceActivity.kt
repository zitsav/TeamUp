package com.example.teamup

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.adapters.BoardAdapter
import com.example.teamup.adapters.SearchUserAdapter
import com.example.teamup.databinding.ActivityWorkspaceBinding
import com.example.teamup.databinding.CreateBoardBinding
import com.example.teamup.dataclasses.AddCardUserRequest
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.GetSingleWorkspaceResponse
import com.example.teamup.dataclasses.MoveCardRequest
import com.example.teamup.dataclasses.SearchUserRequest
import com.example.teamup.dataclasses.SearchUserResponse
import com.example.teamup.dataclasses.User
import com.example.teamup.dataclasses.Workspace
import com.example.teamup.network.BoardApi
import com.example.teamup.network.CardApi
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.UserApi
import com.example.teamup.network.WorkspaceApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class WorkspaceActivity : AppCompatActivity(), BoardAdapter.OnClickListener {

    private lateinit var binding: ActivityWorkspaceBinding
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var authToken: String
    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var boardApi: BoardApi
    private lateinit var cardApi: CardApi
    private lateinit var userApi: UserApi
    private var workspaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkspaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        boardApi = RetrofitInstance.getRetrofitInstance().create(BoardApi::class.java)
        cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)
        userApi = RetrofitInstance.getRetrofitInstance().create(UserApi::class.java)

        val sharedPreferences: SharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"

        workspaceId = intent.getIntExtra("workspaceId", -1)

        fetchWorkspaceById(workspaceId)
    }

    private fun fetchWorkspaceById(workspaceId: Int) {
        val call: Call<GetSingleWorkspaceResponse> = workspaceApi.getWorkspaceById(authToken, workspaceId)

        call.enqueue(object : Callback<GetSingleWorkspaceResponse> {
            override fun onResponse(call: Call<GetSingleWorkspaceResponse>, response: Response<GetSingleWorkspaceResponse>) {
                if (response.isSuccessful) {
                    val workspace = response.body()?.workspace
                    if (workspace != null) {
                        val boardList = ArrayList(workspace.boards)
                        setupRecyclerView(boardList)
                    }
                }
            }

            override fun onFailure(call: Call<GetSingleWorkspaceResponse>, t: Throwable) {
                Log.e("WorkspaceActivity", "Error fetching workspace: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(boardList: ArrayList<Board>) {
        binding.recyclerViewBoards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        boardAdapter = BoardAdapter(this, boardList, this)
        binding.recyclerViewBoards.adapter = boardAdapter
    }

    override fun onClick(board: Board) {
        // Handle board click
    }

    fun handleCardOptionClick(menuItem: MenuItem, card: Card) {
        when (menuItem.itemId) {
            R.id.action_delete_card -> deleteCard(card.id)
            R.id.action_assign_user -> showAssignUserDialog(card.id)
        }
    }

    private fun deleteCard(cardId: Int) {
        cardApi.deleteCard(authToken, cardId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Card deleted successfully")
                    fetchWorkspaceById(workspaceId)
                } else {
                    showToast("Error deleting card")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun showAssignUserDialog(cardId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_add_user, null)
        val etSearchUser = dialogView.findViewById<EditText>(R.id.etSearchUser)
        val rvSearchResults = dialogView.findViewById<RecyclerView>(R.id.rvSearchResults)

        val userList = ArrayList<User>()
        val searchUserAdapter = SearchUserAdapter(this, userList) { user ->
            addUserToCard(cardId, user.email)
        }
        rvSearchResults.adapter = searchUserAdapter
        rvSearchResults.layoutManager = LinearLayoutManager(this)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Assign User")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        etSearchUser.setOnKeyListener { _, keyCode, _ ->
            val query = etSearchUser.text.toString().trim()
            if (keyCode == KeyEvent.KEYCODE_ENTER && query.isNotEmpty()) {
                searchUsers(query, searchUserAdapter)
                true
            } else {
                false
            }
        }

        dialog.show()
    }

    private fun searchUsers(query: String, adapter: SearchUserAdapter) {
        val request = SearchUserRequest(query)
        userApi.searchUser(authToken, request).enqueue(object : Callback<SearchUserResponse> {
            override fun onResponse(call: Call<SearchUserResponse>, response: Response<SearchUserResponse>) {
                if (response.isSuccessful) {
                    val users = response.body()?.users ?: emptyList()
                    adapter.updateUsers(users)
                } else {
                    showToast("Error searching users")
                }
            }

            override fun onFailure(call: Call<SearchUserResponse>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun addUserToCard(cardId: Int, email: String) {
        val request = AddCardUserRequest(email, cardId)
        cardApi.addCardUser(authToken, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("User added to card successfully")
                } else {
                    showToast("Error adding user to card")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    fun moveCardToDifferentBoard(cardId: Int, newBoardId: Int, newPosition: Int) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)

        val moveCardRequest = MoveCardRequest(newBoardId, newPosition)
        val call: Call<Void> = cardApi.moveCardToDifferentBoard(authToken, cardId, moveCardRequest)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Card moved to different board successfully")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Error", "Failed to move card to different board")
            }
        })
    }    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}