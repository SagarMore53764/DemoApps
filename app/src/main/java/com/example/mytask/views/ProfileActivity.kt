package com.example.mytask.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.example.mytask.databinding.ActivityProfileBinding
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    val EMAIL_REGEX =
        "[_a-zA-Z1-9]+(\\.[A-Za-z0-9]*)*@[A-Za-z0-9]+\\.[A-Za-z0-9]+(\\.[A-Za-z0-9]*)*"

    private var uri: Uri? = null
    private val SELECT_FILE = 1
    private val CAMERA = 2

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE =
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyStoragePermissions(this)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        sharedPreferences = getSharedPreferences("MyTaskSharedPreferences", Context.MODE_PRIVATE)


        uri = sharedPreferences.getString("uri", "")?.toUri()
        val bitmap = getBitmapFromUri(this, uri)
        if (bitmap != null) {
            binding.content.profileImg.setImageBitmap(bitmap)
        }

        binding.content.nameEt.setText(sharedPreferences.getString("Name", ""))
        binding.content.mobileNoEt.setText(sharedPreferences.getString("MobileNo", ""))
        binding.content.emailEt.setText(sharedPreferences.getString("Email", ""))
        binding.content.countryCode.setCountryForNameCode(sharedPreferences.getString("code", ""))

        binding.content.submitBtn.setOnClickListener {


            if (uri != null) {
                sharedPreferences.edit().putString("uri", uri.toString()).apply()
            }

            if (!binding.content.nameEt.text.isNullOrBlank()) {
                sharedPreferences.edit()
                    .putString("Name", binding.content.nameEt.text.toString().trim())
                    .apply()
            }

            sharedPreferences.edit()
                .putString("code", binding.content.countryCode.selectedCountryNameCode).apply()

            if (!binding.content.mobileNoEt.text.isNullOrBlank()) {
                sharedPreferences.edit()
                    .putString("MobileNo", binding.content.mobileNoEt.text.toString().trim())
                    .apply()
            }

            if (!binding.content.emailEt.text.isNullOrBlank()
                && binding.content.emailEt.text?.length ?: 0 > 5
                && binding.content.emailEt.text?.matches(EMAIL_REGEX.toRegex()) == true
            ) {
                sharedPreferences.edit()
                    .putString("Email", binding.content.emailEt.text.toString().trim())
                    .apply()
            } else {
                Toast.makeText(this, "Please enter valid email address", Toast.LENGTH_LONG).show()
            }

            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_LONG).show()

        }

        binding.content.imageLayout.setOnClickListener {

            uploadImage()
        }
    }

    private fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isEmpty()
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED
                ) {
//                    viewModel.getmSnackbar().value = "Cannot write images to external storage"
                }
            }
        }
    }

    /**
     * Handle the following URI
     * 1. Gallery App , example content://media/external/images/media/3753
     * 2. From Recents , example content://com.android.providers.media.documents/document/image:2505
     * 3. Google Drive, example content://com.google.android.apps.docs.storage/document/acc=1;doc=22
     * 4. Photos app, example content://com.google.android.apps.photos.content/0/https%3A%2F%2Flh5.googleusercontent.com%2F-H7yZgZCQmFF1zec-_uU0aiyCfol1TH-b9QBWnFZ7rU%3Ds0-d
     *
     * @param context
     * @param uri
     * @return
     */
    fun getBitmapFromUri(
        context: Context,
        uri: Uri?
    ): Bitmap? {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        return try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri!!, "r")
            val fileDescriptor =
                parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            image
        } catch (e: java.lang.Exception) {
            Log.e("File Read", "Failed to load image.", e)
            null
        } finally {
            try {
                parcelFileDescriptor?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("File Read", "Error closing ParcelFile Descriptor")
            }
        }
    }

    private fun uploadImage() {
        val items =
            arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA)
                }
                items[item] == "Choose from Library" -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT

                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            "Select Profile Image"
                        ), SELECT_FILE
                    )
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun getImageUri(
        inContext: Context,
        inImage: Bitmap?
    ): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }

        when (requestCode) {

            SELECT_FILE -> {
                if (data != null) {
                    if (data.data != null) {
                        uri = data.data
                    }
                }

            }

            CAMERA -> {

                if (data != null) {
                    if (data.extras?.get("data") != null) {
                        val thumbnail = data.extras?.get("data") as Bitmap
                        uri = getImageUri(this, thumbnail)
                    }
                }

            }

        }

        if (uri != null) {
            binding.content.profileImg.setImageURI(uri)
        }
    }


}