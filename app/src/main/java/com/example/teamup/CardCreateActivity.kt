package com.example.teamup

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.teamup.database.AppDatabase
import com.example.teamup.database.UserDao
import com.example.teamup.databinding.ActivityCardCreateBinding
import com.example.teamup.dataclasses.Card
import com.example.teamup.dataclasses.CreateCardRequest
import com.example.teamup.network.CardApi
import com.example.teamup.network.RetrofitInstance
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
import java.io.IOException
import kotlin.properties.Delegates

class CardCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardCreateBinding
    private var imageUrl: String? = null
    private var imageUri: Uri? = null
    private var boardId = -1
    private lateinit var storageReference: StorageReference
    private lateinit var authToken: String
    private lateinit var cardApi: CardApi
    private lateinit var progressDialog: ProgressDialog
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        binding = ActivityCardCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseStorage()

        boardId = intent.getIntExtra("BOARD_ID", -1)

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
        cardApi = RetrofitInstance.getRetrofitInstance().create(CardApi::class.java)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please Wait")
            setCanceledOnTouchOutside(false)
        }

        binding.iconIv.setOnClickListener {
            showImagePickOptions()
        }

        binding.postAdBtn.setOnClickListener {
            val title = binding.titleEt.text.toString().trim()
            val description = binding.descEt.text.toString().trim()

            if (title.isEmpty()) {
                binding.titleEt.error = "Title is required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                binding.descEt.error = "Description is required"
                return@setOnClickListener
            }

            if (imageUri != null) {
                uploadImageToFirebaseAndCreateCard(title, description)
            } else {
                createCard(title, description, null)
            }
        }
    }

    private fun initializeFirebaseStorage() {
        FirebaseStorage.getInstance().apply {
            storageReference = reference
        }
    }

    private fun showImagePickOptions() {
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
            put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
            put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                imageUri?.let {
                    binding.iconIv.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri?.let {
                    binding.iconIv.setImageURI(it)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    private fun compressImage(imageUri: Uri): ByteArray {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    private fun uploadImageToFirebaseAndCreateCard(title: String, description: String) {
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()

        val storageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
        val compressedImage = compressImage(imageUri!!)

        storageRef.putBytes(compressedImage)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrl = downloadUri.toString()
                    progressDialog.dismiss()
                    createCard(title, description, imageUrl)
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createCard(title: String, description: String, image: String?) {
        val createCardRequest = CreateCardRequest(title, description, image, boardId)

        cardApi.createCard(authToken, createCardRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CardCreateActivity, "Card created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    Toast.makeText(this@CardCreateActivity, "Failed to create card", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}")
                Toast.makeText(this@CardCreateActivity, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val TAG = "CardCreateActivity"
    }
}