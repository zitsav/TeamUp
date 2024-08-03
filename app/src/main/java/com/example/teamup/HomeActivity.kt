package com.example.teamup

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.teamup.adapters.SearchUserAdapter
import com.example.teamup.adapters.WorkspaceAdapter
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.UserDao
import com.example.teamup.databinding.ActivityHomeBinding
import com.example.teamup.dataclasses.AddWorkspaceUserRequest
import com.example.teamup.dataclasses.CreateWorkspaceRequest
import com.example.teamup.dataclasses.EditWorkspaceRequest
import com.example.teamup.dataclasses.GetAllWorkspaceResponse
import com.example.teamup.dataclasses.MessageResponse
import com.example.teamup.dataclasses.SearchUserRequest
import com.example.teamup.dataclasses.SearchUserResponse
import com.example.teamup.dataclasses.User
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.UserApi
import com.example.teamup.network.WorkspaceApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var userApi: UserApi
    private lateinit var workspaceAdapter: WorkspaceAdapter
    private lateinit var authToken: String
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userDao: UserDao
    private var addUserDialog: AlertDialog? = null
    private var editWorkspaceDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        userDao = AppDatabase.getDatabase(this).userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val authTokenEntity = userDao.getAuthToken()
            authToken = "Bearer ${authTokenEntity?.token}"

            withContext(Dispatchers.Main) {
                setupUI()
            }
        }
    }

    private fun setupUI() {
        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)
        userApi = RetrofitInstance.getRetrofitInstance().create(UserApi::class.java)

        workspaceAdapter = WorkspaceAdapter(this, { workspaceId ->
            navigateToWorkspaceActivity(workspaceId)
        }, { workspaceId, menuItem ->
            handleOptionClick(workspaceId, menuItem)
        })
        binding.recyclerView.adapter = workspaceAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fetchAllWorkspaces()
        setupNavigationDrawer()

        binding.fabCreateBoard.setOnClickListener {
            startActivity(Intent(this, CreateWorkspaceActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_notification -> {
                    showToast("Notifications not implemented yet")
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
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
                Log.e(TAG, "Network error", t)
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun handleOptionClick(workspaceId: Int, menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_add_user -> showAddUserDialog(workspaceId)
            R.id.action_delete_workspace -> deleteWorkspace(workspaceId)
            R.id.action_edit_workspace -> showEditWorkspaceDialog(workspaceId)
        }
    }

    private fun showAddUserDialog(workspaceId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_add_user, null)
        val etSearchUser = dialogView.findViewById<EditText>(R.id.etSearchUser)
        val rvSearchResults = dialogView.findViewById<RecyclerView>(R.id.rvSearchResults)

        val userList = ArrayList<User>()
        val searchUserAdapter = SearchUserAdapter(this, userList) { user ->
            addUserToWorkspace(workspaceId, user.email)
        }
        rvSearchResults.adapter = searchUserAdapter
        rvSearchResults.layoutManager = LinearLayoutManager(this)

        addUserDialog = AlertDialog.Builder(this)
            .setTitle("Add User")
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

        addUserDialog?.show()
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

    private fun addUserToWorkspace(workspaceId: Int, email: String) {
        val request = AddWorkspaceUserRequest(workspaceId, email)
        workspaceApi.addWorkspaceMember(authToken, request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    showToast("User added successfully")
                    addUserDialog?.dismiss()
                } else {
                    showToast("Error adding user")
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun deleteWorkspace(workspaceId: Int) {
        workspaceApi.removeWorkspace(authToken, workspaceId).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    showToast("Workspace deleted successfully")
                    fetchAllWorkspaces()
                } else {
                    showToast("Error deleting workspace")
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun showEditWorkspaceDialog(workspaceId: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialogue_edit_workspace, null)
        val etWorkspaceName = dialogView.findViewById<EditText>(R.id.etWorkspaceName)

        editWorkspaceDialog = AlertDialog.Builder(this)
            .setTitle("Edit Workspace")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etWorkspaceName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    editWorkspace(workspaceId, newName)
                } else {
                    showToast("Workspace name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        editWorkspaceDialog?.show()
    }

    private fun editWorkspace(workspaceId: Int, newName: String) {
        val request = EditWorkspaceRequest(newName)
        workspaceApi.editWorkspace(authToken, workspaceId, request).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    showToast("Workspace updated successfully")
                    fetchAllWorkspaces()
                    editWorkspaceDialog?.dismiss()
                } else {
                    showToast("Error updating workspace")
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                showToast("Network error. Please try again.")
            }
        })
    }

    private fun navigateToWorkspaceActivity(workspaceId: Int) {
        val intent = Intent(this, WorkspaceActivity::class.java)
        intent.putExtra("workspaceId", workspaceId)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}