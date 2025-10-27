package com.anviam.fragmentapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.util.Base64
import android.widget.Toast
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.anviam.fragmentapp.network.HFService
import com.anviam.fragmentapp.network.HFRequest
import com.anviam.fragmentapp.network.HFParams


class CaptureImage : AppCompatActivity() {
    private lateinit var imagePreview: ImageView
    private lateinit var btnCamera: MaterialButton
    private lateinit var btnGallery: MaterialButton
    private lateinit var btnTransform: MaterialButton
    private lateinit var etPrompt: TextInputEditText

    private var cameraImageUri: Uri? = null
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagePreview.setImageURI(uri)
            selectedImageUri = uri
        }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let {
                imagePreview.setImageURI(it)
                selectedImageUri = it
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCameraCapture()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_capture_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imagePreview = findViewById(R.id.imagePreview)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnTransform = findViewById(R.id.btnTransform)
        etPrompt = findViewById(R.id.etPrompt)

        cameraImageUri = savedInstanceState?.getParcelable("camera_uri")

        btnCamera.setOnClickListener { ensureCameraAndStart() }
        btnGallery.setOnClickListener { openGalleryPicker() }
        btnTransform.setOnClickListener { onTransformClicked() }
    }

    private fun ensureCameraAndStart() {
        val hasCam = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCam) startCameraCapture() else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun startCameraCapture() {
        cameraImageUri = createImageUri()
        val uri = cameraImageUri ?: return
        takePicture.launch(uri)
    }

    private fun openGalleryPicker() {
        // Android 13+ Photo Picker needs no permission. Below 13, PickVisualMedia also avoids permissions.
        pickMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun createImageUri(): Uri? {
        return try {
            val imagesDir = File(cacheDir, "images").apply { mkdirs() }
            val image = File.createTempFile("IMG_", ".jpg", imagesDir)
            val authority = "${packageName}.fileprovider"
            FileProvider.getUriForFile(this, authority, image)
        } catch (e: Exception) {
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        cameraImageUri?.let { outState.putParcelable("camera_uri", it) }
    }

    private fun onTransformClicked() {
        val uri = selectedImageUri
        if (uri == null) {
            Toast.makeText(this, "Select or capture an image first", Toast.LENGTH_SHORT).show()
            return
        }
        val prompt = etPrompt.text?.toString()?.trim().orEmpty()
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Enter a prompt", Toast.LENGTH_SHORT).show()
            return
        }
        val token = getString(R.string.hf_token)
        if (token.isBlank()) {
            Toast.makeText(this, "Set your HF token in strings.xml (hf_token)", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val imageBytes = withContext(Dispatchers.IO) {
                    readAllBytes(contentResolver.openInputStream(uri))
                }
                val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                val response = withContext(Dispatchers.IO) {
                    HFService.api.transform(
                        auth = "Bearer $token",
                        body = HFRequest(inputs = base64, parameters = HFParams(prompt))
                    )
                }

                if (response.isSuccessful) {
                    val contentType = response.headers()["Content-Type"] ?: ""
                    val bytes = withContext(Dispatchers.IO) { response.body()?.bytes() }
                    if (bytes != null) {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            imagePreview.setImageBitmap(bmp)
                        } else {
                            Toast.makeText(this@CaptureImage, "Unexpected response", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@CaptureImage, "Empty response", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val code = response.code()
                    Toast.makeText(this@CaptureImage, "Error $code", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CaptureImage, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readAllBytes(input: InputStream?): ByteArray {
        input ?: return ByteArray(0)
        input.use { ins ->
            val buffer = ByteArray(8 * 1024)
            val bos = ByteArrayOutputStream()
            var n: Int
            while (true) {
                n = ins.read(buffer)
                if (n < 0) break
                bos.write(buffer, 0, n)
            }
            return bos.toByteArray()
        }
    }
}