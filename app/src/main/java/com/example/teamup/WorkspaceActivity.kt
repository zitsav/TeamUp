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
import com.example.teamup.databinding.ActivityWorkspaceBinding
import com.example.teamup.databinding.CreateBoardBinding
import com.example.teamup.dataclasses.Board
import com.example.teamup.dataclasses.GetSingleWorkspaceResponse
import com.example.teamup.dataclasses.Workspace
import com.example.teamup.network.BoardApi
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.WorkspaceApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkspaceActivity : AppCompatActivity(), BoardAdapter.OnClickListener {

    private lateinit var binding: ActivityWorkspaceBinding
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var authToken: String
    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var boardApi: BoardApi
    private var workspaceId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkspaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        boardApi = RetrofitInstance.getRetrofitInstance().create(BoardApi::class.java)

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
        TODO("Not yet implemented")
    }
}