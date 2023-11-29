package com.stressmeter.myruns

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivImage : ImageView
    private lateinit var imageButton: Button
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var rgGender : RadioGroup
    private lateinit var etClass: EditText
    private lateinit var etMajor: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var sharedPref : SharedPreferences
    private lateinit var profViewModel: ProfileViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>

    private val profPicName = "prof_img.jpg"
    private val tempImgFileName = "temp_img.jpg"
    private lateinit var tempImgFile: File
    private lateinit var tempImgUri: Uri
    private lateinit var tempImgFile2 : File
    private lateinit var tempImgUri2 : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // initialize the views and the variables
        ivImage = findViewById(R.id.ivImage)
        imageButton = findViewById(R.id.imageButton)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        rgGender = findViewById(R.id.rgGender)
        etClass = findViewById(R.id.etClass)
        etMajor = findViewById(R.id.etMajor)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // This does NOT CAUSE a crash if there is no file at the specified path
        // the permanent file location
        tempImgFile = File(getExternalFilesDir(null), profPicName)
        tempImgUri = FileProvider.getUriForFile(this, "com.stressmeter.myruns", tempImgFile)
        // the temporary file location, only stored if clicked on save button
        tempImgFile2 = File(getExternalFilesDir(null), tempImgFileName)
        tempImgUri2 = FileProvider.getUriForFile(this, "com.stressmeter.myruns", tempImgFile2)

        // initialize the view model
        profViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // initialize the observers for image and edit text views
        profViewModel.userImage.observe(this) {
            ivImage.setImageBitmap(it)
        }
        profViewModel.username.observe(this) {
            etName.setText(it)
        }
        profViewModel.email.observe(this) {
            etEmail.setText(it)
        }
        profViewModel.phone.observe(this) {
            etPhone.setText(it)
        }
        profViewModel.gender.observe(this) {
            // if an option is selected, set the radio button to that option
            if(it != -1 && it != null) {
                findViewById<RadioButton>(it)?.isChecked = true
            }
        }
        profViewModel.classYear.observe(this) {
            // if the class is not empty or not an integer
            if(it != -1 && it != null) {
                etClass.setText(it.toString())
            }
        }
        profViewModel.major.observe(this) {
            etMajor.setText(it)
        }

        // set the click listener for the image button to launch the camera
        imageButton.setOnClickListener {
            val options = arrayOf("Camera", "Gallery")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select an option")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri2)
                            cameraResult.launch(intent)
                        }
                        1 -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            intent.type = "image/*";
                            cameraResult.launch(intent)
                        }
                    }
                }.show()
        }

        // ActivityResultLauncher launches an activity for which you would like a result when it finished.
        // registerForActivityResult() is a call to register an activity result callback, invoked when the activity you launched with the launcher finishes.
        // StartActivityForResult() is a contract that starts an activity for which you would like a result when it finished.
        cameraResult = registerForActivityResult(StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) {
                if(result.data != null && result.data?.data != null){
                    Log.d("xd", "image from gallery")
                    val selectedImageUri = result.data?.data!!
                    // Save the selected image to the temporary file
                    saveImageToTempFile(selectedImageUri)
                    profViewModel.userImage.value = Util.getBitmap(this, tempImgUri2)
                }
                else {
                    Log.d("xd", "image from camera")
                    // Image captured from the camera
                    val bitmap = Util.getBitmap(this, tempImgUri2)
                    profViewModel.userImage.value = bitmap
                }
            }
        }

        // load the profile data onCreeate
        loadProfile()

        saveButton.setOnClickListener {
            saveProfile()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        cancelButton.setOnClickListener {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveImageToTempFile(selectedImageUri: Uri) {
        val inputStream = contentResolver.openInputStream(selectedImageUri)
        val outputStream = FileOutputStream(tempImgFile2)
        inputStream.use { input ->
            outputStream.use { output ->
                input?.copyTo(output)
            }
        }
    }

    private fun saveProfile() {
        // If a new picture is taken, copy it to the permanent file location and delete the temporary file.
        if (tempImgFile2.exists()) {
            Log.d("xd", "tempImgFile2 exists")
            tempImgFile2.copyTo(tempImgFile, true)
            tempImgFile2.delete()
        }
        // Editor is used to modify the shared preferences.
        val editor = sharedPref.edit()
        editor.putString("name", etName.text.toString())
        editor.putString("email", etEmail.text.toString())
        editor.putString("phone", etPhone.text.toString())
        editor.putInt("gender", rgGender.checkedRadioButtonId)

        // If the class value is not an integer, set it to -1.
        val classVal = etClass.text.toString().toIntOrNull() ?: -1
        editor.putInt("class", classVal)

        editor.putString("major", etMajor.text.toString())
        editor.apply()
        finish()
    }

    private fun loadProfile() {
        // If there is an image in the permanent file location, load it.
        if(tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            profViewModel.userImage.value = bitmap
        }
        if(sharedPref.contains("name")) {
            profViewModel.username.value = sharedPref.getString("name", "")
        }
        if(sharedPref.contains("email")) {
            profViewModel.email.value = sharedPref.getString("email", "")
        }
        if(sharedPref.contains("phone")) {
            profViewModel.phone.value = sharedPref.getString("phone", "")
        }
        if(sharedPref.contains("gender")) {
            profViewModel.gender.value = sharedPref.getInt("gender", -1)
        }
        if(sharedPref.contains("class")) {
            profViewModel.classYear.value = sharedPref.getInt("class", -1)
        }
        if(sharedPref.contains("major")) {
            profViewModel.major.value = sharedPref.getString("major", "")
        }
    }
}