package com.example.teamup

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.example.teamup.databinding.ActivityRegisterBinding
import com.example.teamup.dataclasses.AuthResponse
import com.example.teamup.dataclasses.RegisterRequest
import com.example.teamup.network.AuthApi
import com.example.teamup.network.RetrofitInstance
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var profileUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var authApi: AuthApi
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseStorage()

        authApi = RetrofitInstance.getRetrofitInstance().create(AuthApi::class.java)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        binding.btnUploadProfile.setOnClickListener {
            showProfilePickOptions()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.editTextName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required"
                return@setOnClickListener
            }

            if (profileUri != null) {
                compressAndUploadProfile(name, email, password)
            } else {
                registerUser(name, null, email, password)
            }
        }
    }

    private fun initializeFirebaseStorage() {
        FirebaseStorage.getInstance().apply {
            storageReference = reference
        }
    }

    private fun showProfilePickOptions() {
        val popupMenu = PopupMenu(this, binding.imageProfile)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                }

                2 -> {
                    pickProfileFromGallery()
                }
            }
            true
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val areAllGranted = result.values.all { it }
            if (areAllGranted) {
                pickProfileFromCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun pickProfileFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        profileGalleryActivityResultLauncher.launch(intent)
    }

    private fun pickProfileFromCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "TEMP_PROFILE_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_PROFILE_DESCRIPTION")
        }
        profileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, profileUri)
        }
        profileCameraActivityResultLauncher.launch(intent)
    }

    private val profileGalleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileUri = result.data?.data
                profileUri?.let {
                    binding.imageProfile.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private val profileCameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileUri?.let {
                    binding.imageProfile.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private fun compressAndUploadProfile(name: String, email: String, password: String) {
        progressDialog.setMessage("Uploading Profile Picture...")
        progressDialog.show()

        profileUri?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val compressedData = outputStream.toByteArray()

            val storageRef = storageReference.child("profiles/${System.currentTimeMillis()}.jpg")
            storageRef.putBytes(compressedData)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val profileUrl = downloadUri.toString()
                        progressDialog.dismiss()
                        registerUser(name, profileUrl, email, password)
                    }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Profile upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun registerUser(name: String, profile: String?, email: String, password: String) {
        val fcmToken = null
        val registerRequest = RegisterRequest(name, profile, email, fcmToken, password)

        authApi.register(registerRequest)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}