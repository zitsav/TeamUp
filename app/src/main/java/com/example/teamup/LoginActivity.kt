package com.example.teamup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.teamup.dataclasses.AuthTokenResponse
import com.example.teamup.dataclasses.LoginRequest
import com.example.teamup.interfaces.AuthApi
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText

    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editEmail = findViewById(R.id.edit_text_email)
        editPassword = findViewById(R.id.edit_text_password)

        val loginButton: Button = findViewById(R.id.btn_login)

        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)

        loginButton.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()

            val loginRequest = LoginRequest(email, password)

            authApi.login(loginRequest).enqueue(object : Callback<AuthTokenResponse> {
                override fun onResponse(call: Call<AuthTokenResponse>, response: Response<AuthTokenResponse>) {
                    if (response.isSuccessful) {
                        val authToken = response.body()?.token
                        if (!authToken.isNullOrBlank()) {
                            val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("AuthToken", authToken)
                            editor.apply()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            showToast("Authentication failed. Please check your credentials.")
                        }
                    } else {
                        showToast("Authentication failed. Please check your credentials.")
                    }
                }

                override fun onFailure(call: Call<AuthTokenResponse>, t: Throwable) {
                    showToast("An error occurred. Please try again later.")
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}