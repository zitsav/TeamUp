package com.example.teamup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.adapters.BoardAdapter
import com.example.teamup.dataclasses.CreateBoardRequest
import com.example.teamup.dataclasses.Workspace
import com.example.teamup.interfaces.BoardApi
import com.example.teamup.interfaces.WorkspaceApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkspaceActivity : AppCompatActivity() {

    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var boardApi: BoardApi
    private lateinit var recyclerViewBoards: RecyclerView
    private lateinit var fabCreateBoard: FloatingActionButton
    private lateinit var accessToken: String
    private lateinit var authToken: String
    private var workspaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        boardApi = RetrofitInstance.getRetrofitInstance().create(BoardApi::class.java)
        recyclerViewBoards = findViewById(R.id.recycler_view_boards)
        fabCreateBoard = findViewById(R.id.fab_create_board)

        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        accessToken = sharedPreferences.getString("AuthToken", "") ?: ""
        authToken = "Bearer $accessToken"

        // Get the workspaceId from the Intent extras
        workspaceId = intent.getIntExtra("workspaceId", -1)

        val boardAdapter = BoardAdapter()
        recyclerViewBoards.layoutManager = LinearLayoutManager(this)
        recyclerViewBoards.adapter = boardAdapter

        fetchWorkspaceData()

        fabCreateBoard.setOnClickListener {
            showCreateBoardDialog()
        }
    }

    private fun fetchWorkspaceData() {
        workspaceApi.getWorkspaceById(authToken, workspaceId).enqueue(object : Callback<Workspace> {
            override fun onResponse(call: Call<Workspace>, response: Response<Workspace>) {
                if (response.isSuccessful) {
                    val workspace = response.body()
                    workspace?.let {
                        val boardList = it.boards
                        val boardAdapter = recyclerViewBoards.adapter as BoardAdapter
                        boardAdapter.submitList(boardList)
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    val responseBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "Raw response: $responseBody")
                    showToast("Error fetching workspace data. Check logs for details.")
                }
            }

            override fun onFailure(call: Call<Workspace>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                showToast("Network error. Please check your internet connection.")
            }
        })
    }

    private fun showCreateBoardDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.create_board, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Create New Board")
            .setPositiveButton("Create") { _, _ ->
                val titleEditText = dialogView.findViewById<EditText>(R.id.inputName2)
                val descriptionEditText = dialogView.findViewById<EditText>(R.id.inputDescription2)

                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    val createBoardRequest = CreateBoardRequest(title, description, workspaceId)
                    createBoard(createBoardRequest)
                } else {
                    showToast("Please enter a board title")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun createBoard(createBoardRequest: CreateBoardRequest) {
        boardApi.createBoard(authToken, createBoardRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Board created successfully")
                    fetchWorkspaceData()
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    showToast("Error creating board")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "WorkspaceActivity"
    }
}