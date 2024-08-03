package com.example.teamup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.UserDao
import com.example.teamup.dataclasses.GetAllWorkspaceResponse
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.WorkspaceApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {
    private val splashDelay: Long = 3000
    private lateinit var userDao: UserDao
    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var authToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userDao = AppDatabase.getDatabase(this).userDao()
        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val authTokenEntity = userDao.getAuthToken()
            authToken = authTokenEntity?.token?.let { "Bearer $it" } ?: ""

            if (authToken.isNotEmpty()) {
                validateToken()
            } else {
                navigateToMainActivity()
            }
        }
    }

    private fun validateToken() {
        workspaceApi.getAllWorkspaces(authToken).enqueue(object :
            Callback<GetAllWorkspaceResponse> {
            override fun onResponse(
                call: Call<GetAllWorkspaceResponse>,
                response: Response<GetAllWorkspaceResponse>
            ) {
                if (response.isSuccessful) {
                    navigateToHomeActivity()
                } else {
                    navigateToMainActivity()
                }
            }

            override fun onFailure(call: Call<GetAllWorkspaceResponse>, t: Throwable) {
                navigateToMainActivity()
            }
        })
    }

    private fun navigateToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}