package com.example.teamup

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teamup.adapters.WorkspaceAdapter
import com.example.teamup.dataclasses.CreateWorkspaceRequest
import com.example.teamup.dataclasses.GetAllWorkspaceResponseItem
import com.example.teamup.interfaces.WorkspaceApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var recyclerView: RecyclerView
    private lateinit var workspaceAdapter: WorkspaceAdapter
    private lateinit var fabCreateBoard: FloatingActionButton
    private lateinit var accessToken: String
    private lateinit var authToken: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        recyclerView = findViewById(R.id.recyclerView)
        fabCreateBoard = findViewById(R.id.fab_create_board)

        workspaceAdapter = WorkspaceAdapter { workspaceId ->
            navigateToWorkspaceActivity(workspaceId)
        }
        recyclerView.adapter = workspaceAdapter

        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        accessToken = sharedPreferences.getString("AuthToken", "") ?: ""
        authToken = "Bearer $accessToken"

        recyclerView.layoutManager = LinearLayoutManager(this) // Set a LinearLayoutManager

        fetchAllWorkspaces()

        fabCreateBoard.setOnClickListener {
            showCreateWorkspaceDialog()
        }
    }

    private fun fetchAllWorkspaces() {
        workspaceApi.getAllWorkspaces(authToken).enqueue(object : Callback<List<GetAllWorkspaceResponseItem>> {
            override fun onResponse(
                call: Call<List<GetAllWorkspaceResponseItem>>,
                response: Response<List<GetAllWorkspaceResponseItem>>
            ) {
                if (response.isSuccessful) {
                    val workspaceList = response.body()
                    workspaceList?.let {
                        workspaceAdapter.submitList(workspaceList) // Update the adapter with workspaceList
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    // Log the raw response body for debugging
                    val responseBody = response.errorBody()?.string() ?: ""
                    Log.e(TAG, "Raw response: $responseBody")
                    showToast("Error fetching workspaces. Check logs for details.")
                }
            }

            override fun onFailure(call: Call<List<GetAllWorkspaceResponseItem>>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                showToast("Network error. Please check your internet connection.")
            }
        })
    }

    private fun navigateToWorkspaceActivity(workspaceId: Int) {
        val intent = Intent(this, WorkspaceActivity::class.java)
        intent.putExtra("workspaceId", workspaceId)
        startActivity(intent)
    }

    private fun showCreateWorkspaceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.create_workspace, null)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        val inputName: EditText = dialogView.findViewById(R.id.inputName)
        val createButton: Button = dialogView.findViewById(R.id.createButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        createButton.setOnClickListener {
            val title = inputName.text.toString().trim()
            if (title.isNotEmpty()) {
                val createWorkspaceRequest = CreateWorkspaceRequest(title)
                workspaceApi.createWorkspace(authToken, createWorkspaceRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            showToast("Workspace created successfully")
                            fetchAllWorkspaces()
                        } else {
                            Log.e(TAG, "API error: ${response.code()}")
                            showToast("Error creating workspace")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e(TAG, "Network error: ${t.message}")
                        showToast("Network error. Please try again.")
                    }
                })
                dialog.dismiss()
            } else {
                showToast("Please enter a title")
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}
