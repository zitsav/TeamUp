package com.example.teamup

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teamup.databinding.ActivityCardCreateBinding
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.network.CardApi
import com.example.teamup.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val boardId = intent.getIntExtra("BOARD_ID", -1)

        binding.btnSubmit.setOnClickListener {
            val cardTitle = binding.etCardTitle.text.toString()
            val cardDescription = binding.etCardDescription.text.toString()
            val cardImage = binding.etCardImage.text.toString()

            if (cardTitle.isNotEmpty()) {
                val createCardRequest = CreateCardRequest(
                    boardId = boardId,
                    title = cardTitle,
                    description = cardDescription.ifEmpty { null },
                    image = cardImage.ifEmpty { null },
                )
                createCard(createCardRequest)
            }
        }
    }

    private fun createCard(request: CreateCardRequest) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "AuthPrefs",
            MODE_PRIVATE
        )
        val authToken = "Bearer ${sharedPreferences.getString("AuthToken", "")}"
        val cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)
        val call: Call<Void> = cardApi.createCard(authToken, request)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@CardCreateActivity, WorkspaceActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("CreateCardActivity", "Failed to create card: ${t.message}")
            }
        })
    }
}
