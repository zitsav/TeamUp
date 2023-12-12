package com.example.teamup

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.adapters.BoardAdapter
import com.example.teamup.adapters.CardAdapter
import com.example.teamup.adapters.ListAdapter
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.CreateBoardRequest
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.dataclasses.CreateListRequest
import com.example.teamup.dataclasses.Lists
import com.example.teamup.dataclasses.Workspace
import com.example.teamup.interfaces.BoardApi
import com.example.teamup.interfaces.CardApi
import com.example.teamup.interfaces.ListApi
import com.example.teamup.interfaces.WorkspaceApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkspaceActivity : AppCompatActivity(), BoardAdapter.OnClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var authToken: String
    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var boardApi: BoardApi
    private var workspaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace)
        recyclerView = findViewById(R.id.recycler_view_boards)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        boardApi = RetrofitInstance.getRetrofitInstance().create(BoardApi::class.java)

        val sharedPreferences: SharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"

        workspaceId = intent.getIntExtra("workspaceId", -1)

        val fabCreateBoard: FloatingActionButton = findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            showCreateBoardDialog()
        }

        fetchWorkspaceById(workspaceId)
    }

    private fun showCreateBoardDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.create_board, null)
        builder.setView(dialogView)

        val boardNameEditText: EditText = dialogView.findViewById(R.id.inputName2Board)

        builder.setPositiveButton("Submit") { dialog, _ ->
            val boardName = boardNameEditText.text.toString()
            if (boardName.isNotEmpty()) {
                val createBoardRequest = CreateBoardRequest("", boardName, workspaceId)
                createBoard(createBoardRequest)
            } else {
                Toast.makeText(this, "Enter title", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun createBoard(request: CreateBoardRequest) {
        val call: Call<Void> = boardApi.createBoard(authToken, request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchWorkspaceById(workspaceId)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
            }
        })
    }

    private fun fetchWorkspaceById(workspaceId: Int) {
        val call: Call<Workspace> = workspaceApi.getWorkspaceById(authToken, workspaceId)

        call.enqueue(object : Callback<Workspace> {
            override fun onResponse(call: Call<Workspace>, response: Response<Workspace>) {
                if (response.isSuccessful) {
                    val workspace = response.body()
                    if (workspace != null) {
                        val boardList = ArrayList(workspace.boards)
                        setupRecyclerView(boardList)
                    }
                }
            }

            override fun onFailure(call: Call<Workspace>, t: Throwable) {
            }
        })
    }

    private fun setupRecyclerView(boardList: ArrayList<Board>) {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        boardAdapter = BoardAdapter(this, boardList)
        recyclerView.adapter = boardAdapter
    }

    override fun onClick(board: Board) {
    }
}