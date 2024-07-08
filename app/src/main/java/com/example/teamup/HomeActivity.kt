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
import com.example.teamup.adapters.WorkspaceAdapter
import com.example.teamup.databinding.ActivityHomeBinding
import com.example.teamup.dataclasses.CreateWorkspaceRequest
import com.example.teamup.dataclasses.GetAllWorkspaceResponse
import com.example.teamup.dataclasses.MessageResponse
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.WorkspaceApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var workspaceAdapter: WorkspaceAdapter
    private lateinit var accessToken: String
    private lateinit var authToken: String
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)

        workspaceAdapter = WorkspaceAdapter(this) { workspaceId ->
            navigateToWorkspaceActivity(workspaceId)
        }
        binding.recyclerView.adapter = workspaceAdapter

        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        accessToken = sharedPreferences.getString("AuthToken", "") ?: ""
        authToken = "Bearer $accessToken"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fetchAllWorkspaces()

        binding.fabCreateBoard.setOnClickListener {
            showCreateWorkspaceDialog()
        }
    }

    private fun fetchAllWorkspaces() {
        workspaceApi.getAllWorkspaces(authToken).enqueue(object : Callback<GetAllWorkspaceResponse> {
            override fun onResponse(
                call: Call<GetAllWorkspaceResponse>,
                response: Response<GetAllWorkspaceResponse>
            ) {
                if (response.isSuccessful) {
                    val workspaceList = response.body()?.workspaces ?: emptyList()
                    workspaceAdapter.submitList(workspaceList)
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    showToast("Error fetching workspaces")
                }
            }

            override fun onFailure(call: Call<GetAllWorkspaceResponse>, t: Throwable) {
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
                val createWorkspaceRequest = CreateWorkspaceRequest(title, null)
                workspaceApi.createWorkspace(authToken, createWorkspaceRequest).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        if (response.isSuccessful) {
                            showToast("Workspace created successfully")
                            fetchAllWorkspaces()
                        } else {
                            Log.e(TAG, "API error: ${response.code()}")
                            showToast("Error creating workspace")
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
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