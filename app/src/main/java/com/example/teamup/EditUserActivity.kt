package com.example.teamup

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.example.teamup.databinding.ActivityEditUserBinding
import com.example.teamup.dataclasses.MessageResponse
import com.example.teamup.dataclasses.UpdateUserRequest
import com.example.teamup.network.AuthApi
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.UserApi
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUserBinding
    private var profileUri: Uri? = null
    private lateinit var authToken: String
    private lateinit var storageReference: StorageReference
    private lateinit var userApi: UserApi
    private lateinit var progressDialog: ProgressDialog
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseStorage()

        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("AuthToken", "") ?: ""
        authToken = "Bearer $accessToken"

        userApi = RetrofitInstance.getRetrofitInstance().create(UserApi::class.java)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.imageProfile2.setOnClickListener {
            showProfilePickOptions()
        }

        binding.btnUploadProfile2.setOnClickListener {
            if (profileUri != null) {
                uploadProfileImage()
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister2.setOnClickListener {
            val name = binding.editTextName2.text.toString().trim()
            if (name.isEmpty()) {
                binding.editTextName2.error = "Name is required"
                return@setOnClickListener
            }
            updateUser(name, profileUri?.toString())
        }
    }

    private fun initializeFirebaseStorage() {
        FirebaseStorage.getInstance().apply {
            storageReference = reference
        }
    }

    private fun showProfilePickOptions() {
        val popupMenu = PopupMenu(this, binding.imageProfile2)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val cameraPermissions = arrayOf(android.Manifest.permission.CAMERA)
                        requestCameraPermission.launch(cameraPermissions)
                    } else {
                        val cameraPermissions = arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        requestCameraPermission.launch(cameraPermissions)
                    }
                }
                2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery()
                    } else {
                        val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        requestStoragePermission.launch(storagePermission)
                    }
                }
            }
            true
        }
    }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImageGallery()
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val areAllGranted = result.values.all { it }
            if (areAllGranted) {
                pickImageCamera()
            } else {
                Toast.makeText(this, "Camera or Storage Permissions Both Denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "TEMP_PROFILE_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_PROFILE_DESCRIPTION")
        }
        profileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, profileUri)
        }
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileUri = result.data?.data
                profileUri?.let {
                    binding.imageProfile2.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileUri?.let {
                    binding.imageProfile2.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private fun uploadProfileImage() {
        progressDialog.setMessage("Uploading Profile Image...")
        progressDialog.show()

        val storageRef = storageReference.child("profiles/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(profileUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val profileUrl = downloadUri.toString()
                    progressDialog.dismiss()
                    updateUser(binding.editTextName2.text.toString().trim(), profileUrl)
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUser(name: String, profileUrl: String?) {
        val updateUserRequest = UpdateUserRequest(profileUrl, name)

        userApi.updateUser(authToken, userId, updateUserRequest).enqueue(object :
            Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditUserActivity, "User updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    Toast.makeText(this@EditUserActivity, "Failed to update user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                Toast.makeText(this@EditUserActivity, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val TAG = "EditUserActivity"
    }
}