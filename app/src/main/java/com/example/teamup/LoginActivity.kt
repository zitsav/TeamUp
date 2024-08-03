package com.example.teamup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.AuthToken
import com.example.teamup.database.User
import com.example.teamup.database.UserDao
import com.example.teamup.dataclasses.AuthResponse
import com.example.teamup.dataclasses.LoginRequest
import com.example.teamup.network.AuthApi
import com.example.teamup.network.RetrofitInstance
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var authApi: AuthApi
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editEmail = findViewById(R.id.edit_text_email)
        editPassword = findViewById(R.id.edit_text_password)

        val loginButton: Button = findViewById(R.id.btn_login)
        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)
        userDao = AppDatabase.getDatabase(this).userDao()

        loginButton.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()
            val fcmToken = getFcmToken()

            val loginRequest = LoginRequest(email, password, fcmToken)

            authApi.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val authToken = response.body()?.token
                        val email2 = response.body()?.email
                        val id = response.body()?.id
                        val name = response.body()?.name
                        val profile = response.body()?.profile

                        if (!authToken.isNullOrBlank() && email2 != null && id != null && name != null) {
                            val user = User(
                                email = email,
                                id = id,
                                name = name,
                                profile = profile
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                userDao.insertUser(user)
                                userDao.insertAuthToken(AuthToken(1, authToken))
                                withContext(Dispatchers.Main) {
                                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            showToast("Authentication failed. Please check your credentials.")
                        }
                    } else {
                        showToast("Authentication failed. Please check your credentials.")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    showToast("An error occurred. Please try again later.")
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getFcmToken(): String? {
        //to be implemented
        return ""
    }
}
