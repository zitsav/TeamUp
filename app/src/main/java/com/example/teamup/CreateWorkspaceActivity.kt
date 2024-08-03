package com.example.teamup

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.UserDao
import com.example.teamup.databinding.ActivityCreateWorkspaceBinding
import com.example.teamup.dataclasses.CreateWorkspaceRequest
import com.example.teamup.dataclasses.MessageResponse
import com.example.teamup.network.RetrofitInstance
import com.example.teamup.network.WorkspaceApi
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class CreateWorkspaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateWorkspaceBinding
    private var iconUri: Uri? = null
    private lateinit var authToken: String
    private lateinit var storageReference: StorageReference
    private lateinit var workspaceApi: WorkspaceApi
    private lateinit var progressDialog: ProgressDialog
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        binding = ActivityCreateWorkspaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseStorage()

        userDao = AppDatabase.getDatabase(this).userDao()

        CoroutineScope(Dispatchers.IO).launch {
            val authTokenEntity = userDao.getAuthToken()
            authToken = "Bearer ${authTokenEntity?.token}"

            withContext(Dispatchers.Main) {
                setupUI()
            }
        }
    }

    private fun setupUI(){
        workspaceApi = RetrofitInstance.getRetrofitInstance().create(WorkspaceApi::class.java)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        binding.iconIv.setOnClickListener {
            showIconPickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            val title = binding.titleEt.text.toString().trim()

            if (title.isEmpty()) {
                binding.titleEt.error = "Title is required"
                return@setOnClickListener
            }

            if (iconUri != null) {
                compressAndUploadIconAndCreateWorkspace(title)
            } else {
                createWorkspace(title, null)
            }
        }
    }

    private fun initializeFirebaseStorage() {
        FirebaseStorage.getInstance().apply {
            storageReference = reference
        }
    }

    private fun showIconPickOptions() {
        val popupMenu = PopupMenu(this, binding.iconIv)
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
                        pickIconGallery()
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
                pickIconGallery()
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val areAllGranted = result.values.all { it }
            if (areAllGranted) {
                pickIconCamera()
            } else {
                Toast.makeText(this, "Camera or Storage Permissions Both Denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun pickIconGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        iconGalleryActivityResultLauncher.launch(intent)
    }

    private fun pickIconCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "TEMP_ICON_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_ICON_DESCRIPTION")
        }
        iconUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, iconUri)
        }
        iconCameraActivityResultLauncher.launch(intent)
    }

    private val iconGalleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                iconUri = result.data?.data
                iconUri?.let {
                    binding.iconIv.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private val iconCameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                iconUri?.let {
                    binding.iconIv.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private fun compressAndUploadIconAndCreateWorkspace(title: String) {
        progressDialog.setMessage("Uploading Icon...")
        progressDialog.show()

        iconUri?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val compressedData = outputStream.toByteArray()

            val storageRef = storageReference.child("icons/${System.currentTimeMillis()}.jpg")
            storageRef.putBytes(compressedData)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val iconUrl = downloadUri.toString()
                        progressDialog.dismiss()
                        createWorkspace(title, iconUrl)
                    }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Icon upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createWorkspace(title: String, icon: String?) {
        val createWorkspaceRequest = CreateWorkspaceRequest(title, icon)

        workspaceApi.createWorkspace(authToken, createWorkspaceRequest)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CreateWorkspaceActivity,
                            "Workspace created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                        Toast.makeText(
                            this@CreateWorkspaceActivity,
                            "Failed to create workspace",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(TAG, "Network error: ${t.message}")
                    Toast.makeText(
                        this@CreateWorkspaceActivity,
                        "Network error. Please check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    companion object {
        private const val TAG = "CreateWorkspaceActivity"
    }
}