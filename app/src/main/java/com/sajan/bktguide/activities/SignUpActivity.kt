package com.sajan.bktguide.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.sajan.bktguide.utils.log
import com.sajan.bktguide.R
import com.sajan.bktguide.controller.TouristInfoPostResponseController
import com.sajan.bktguide.dataModels.TouristDto
import com.sajan.bktguide.protocols.OnResponseListener
import com.sajan.bktguide.storage.PrefLogin
import com.sajan.bktguide.storage.PrefsImageUri
import com.sajan.bktguide.utils.URL_REGISTER
import com.sajan.bktguide.utils.getParam
import com.sajan.bktguide.utils.isInternetConnected
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception
import java.util.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private var imageUriV2: Uri? = null
    private var imageUri: Uri? = null
    private var fileName: String? = null
    private var storageCheckPermission: Int = 0
    private var cameraCheckPermission: Int = 0
    private var permissionRequestCode: Int = 345
    private val requestCodeCamera = 25
    private val requestCodeGallery = 27
    private lateinit var passwordV2: String

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setUpActionBar()
        setUpProgressDialog()
        initViews()
        storageCheckPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        cameraCheckPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@SignUpActivity)
        progressDialog!!.setMessage("Registering....")
        progressDialog!!.setCancelable(false)
    }

    private fun initViews() {
        ivProfile.setOnClickListener(this)
        btnStart.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivProfile -> {
                if (storageCheckPermission != PackageManager.PERMISSION_GRANTED ||
                    cameraCheckPermission != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), permissionRequestCode
                    )
                } else {
                    AlertDialog.Builder(this@SignUpActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@SignUpActivity,
                                        getImageFile(
                                            this@SignUpActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@SignUpActivity.packageManager) != null) {
                                        startActivityForResult(
                                            Intent.createChooser(cameraIntent, "Take picture"),
                                            requestCodeCamera
                                        )
                                    }
                                }
                                1 -> {
                                    val intent = Intent()
                                    intent.type = "image/*"
                                    intent.action = Intent.ACTION_GET_CONTENT
                                    startActivityForResult(
                                        Intent.createChooser(
                                            intent,
                                            "Select Picture"
                                        ), requestCodeGallery
                                    )
                                }
                                2 -> {
                                    imageUriV2 = null
                                    fileName = null
                                    ivProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@SignUpActivity,
                                            R.drawable.ic_profile
                                        )
                                    )
                                }
                            }
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }

            R.id.btnStart -> {
                if (allFieldValid()) {
                    val name = etName.text.toString()
//                    val email = etEmail.text.toString()
                    val username = etUsername.text.toString()
                    val password = etPassword.text.toString()

                    val touristDto = TouristDto().apply {
                        this.name = name
                        //                        this.email = email
                        this.username = username
                        this.password = password
                        this.imageUri = imageUriV2
                    }
                    this.passwordV2 = password
                    if (isInternetConnected) {
                        val paramsBuilder = getParam(touristDto)
                        val touristInfoPostResponse =
                            TouristInfoPostResponseController(URL_REGISTER, mOnPostResponseListener)
                        touristInfoPostResponse.execute(paramsBuilder.toString())
                        this@SignUpActivity::class.java.log("dtoData--> $touristDto")
                        this@SignUpActivity::class.java.log("paramsData--> $paramsBuilder")
                    } else {
                        AlertDialog.Builder(this@SignUpActivity)
                            .setCancelable(false)
                            .setMessage("No internet connection")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
                }
            }
        }
    }

    private var touristDtoV2: TouristDto? = null

    private val mOnPostResponseListener = object :
        OnResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.show()
            this@SignUpActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@SignUpActivity::class.java.log("server up: $response")

            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@SignUpActivity,
                        jsonObjectResponse.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()

                    val jsonArrayResponseData = jsonObjectResponse.getJSONArray("data")
                    for (j in 0 until jsonArrayResponseData.length()) {
                        val jsonObjectResponseData = jsonArrayResponseData.getJSONObject(j)
                        val id = jsonObjectResponseData.getInt("id")
                        val name = jsonObjectResponseData.getString("name")
                        val email = jsonObjectResponseData.getString("email")
                        val username = jsonObjectResponseData.getString("username")

                        touristDtoV2 = TouristDto().apply {
                            this.id = id
                            this.name = name
                            this.email = email
                            this.username = username
                            this.imageUri = imageUriV2
                            this.password = passwordV2
                        }

                        PrefLogin(this@SignUpActivity).isLogin = true

                        PrefLogin(this@SignUpActivity).id = touristDtoV2!!.id
                        PrefLogin(this@SignUpActivity).name = touristDtoV2!!.name
                        PrefLogin(this@SignUpActivity).email = touristDtoV2!!.email
                        PrefLogin(this@SignUpActivity).username = touristDtoV2!!.username
                        PrefLogin(this@SignUpActivity).password = touristDtoV2!!.password

                        PrefsImageUri(this@SignUpActivity).imgUri =
                            touristDtoV2!!.imageUri.toString()
                        PrefsImageUri(this@SignUpActivity).fileName = fileName

                        this@SignUpActivity::class.java.log("ImageUri: Uri--> ${touristDtoV2!!.imageUri}")
                        this@SignUpActivity::class.java.log("Image File Name--> ${PrefsImageUri(this@SignUpActivity).fileName}")
                        this@SignUpActivity::class.java.log("PrefsData: ID-->${PrefLogin(this@SignUpActivity).id}")
                        this@SignUpActivity::class.java.log("PrefsData:  Name-->${PrefLogin(this@SignUpActivity).name}")
                        this@SignUpActivity::class.java.log("PrefsData: Email-->${PrefLogin(this@SignUpActivity).email}")
                        this@SignUpActivity::class.java.log("PrefsData: Username-->${PrefLogin(this@SignUpActivity).username}")
                        this@SignUpActivity::class.java.log("PrefsData: Password--> ${PrefLogin(this@SignUpActivity).password}")
                    }
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.putExtra("dto", touristDtoV2)
                    startActivity(intent)
                    finish()
                } else {
                    etUsername.requestFocus()
                    Snackbar.make(
                        clSignUp,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@SignUpActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@SignUpActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private fun allFieldValid(): Boolean {
        if (etName.text.toString().isEmpty()) {
            tilName.error = "Your name?"
            tilName.isErrorEnabled = true
            etName.requestFocus()
            return false
        } else {
            tilName.isErrorEnabled = false
            tilName.error = null
            etName.clearFocus()
        }
//        if (etEmail.text.toString().isNotEmpty()) {
//            if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches())) {
//                tilEmail.isErrorEnabled = true
//                etEmail.requestFocus()
//                tilEmail.error = "Email mis-match."
//                return false
//            } else {
//                tilEmail.isErrorEnabled = false
//                tilEmail.error = null
//                etEmail.clearFocus()
//            }
//        }

        if (etUsername.text.toString().isEmpty()) {
            tilUsername.isErrorEnabled = true
            etUsername.requestFocus()
            tilUsername.error = "Your username?"
            return false
        } else {
            tilUsername.isErrorEnabled = false
            tilUsername.error = null
            etUsername.clearFocus()
        }
        if (etPassword.text.toString().isEmpty()) {
            tilPassword.isErrorEnabled = true
            etPassword.requestFocus()
            tilPassword.error = "Your password?"
            return false
        } else {
            tilPassword.isErrorEnabled = false
            tilPassword.error = null
            etPassword.clearFocus()
        }

        if (etPassword.text.toString().length < 6) {
            tilPassword.isErrorEnabled = true
            etPassword.requestFocus()
            tilPassword.error = "Password too short, maximum 6 characters."
            return false
        } else {
            tilPassword.isErrorEnabled = false
            tilPassword.error = null
            etPassword.clearFocus()
        }
        if ((etPassword.text.toString()) != etConfirmPassword.text.toString()) {
            tilConfirmPassword.isErrorEnabled = true
            etConfirmPassword.requestFocus()
            tilConfirmPassword.error = "Password mis-match."
            return false
        } else {
            tilConfirmPassword.isErrorEnabled = false
            tilConfirmPassword.error = null
            etConfirmPassword.clearFocus()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            requestCodeCamera -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageUriV2 = Uri.fromFile(imageFile)
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath, options)
                    ivProfile.setImageBitmap(bitmap)
                }
            }
            requestCodeGallery -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val image = data.data
                        var bitmap: Bitmap? = null
                        val imageStream: InputStream
                        try {
                            imageStream = contentResolver.openInputStream(image!!)!!
                            bitmap = BitmapFactory.decodeStream(imageStream)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        getFileUri(
                            this@SignUpActivity,
                            getImageFile(
                                this@SignUpActivity,
                                UUID.randomUUID().toString()
                            )
                        )
                        val fos = FileOutputStream(imageFile!!)
                        imageUriV2 = Uri.fromFile(imageFile)
                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        ivProfile.setImageBitmap(bitmap)
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), permissionRequestCode
                    )
                } else {
                    AlertDialog.Builder(this@SignUpActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove Photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@SignUpActivity,
                                        getImageFile(
                                            this@SignUpActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@SignUpActivity.packageManager) != null) {
                                        startActivityForResult(
                                            Intent.createChooser(cameraIntent, "Take picture"),
                                            requestCodeCamera
                                        )
                                    }
                                }
                                1 -> {
                                    val intent = Intent()
                                    intent.type = "image/*"
                                    intent.action = Intent.ACTION_GET_CONTENT
                                    startActivityForResult(
                                        Intent.createChooser(
                                            intent,
                                            "Select Picture"
                                        ), requestCodeGallery
                                    )
                                }
                                2 -> {
                                    imageUriV2 = null
                                    fileName = null
                                    ivProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@SignUpActivity,
                                            R.drawable.ic_profile
                                        )
                                    )
                                }
                            }
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var imageFile: File? = null

    private fun getImageFile(context: Context, imageTag: String): File? {
        val baseDirectory = getBaseDirectory(context)
        fileName = "${imageTag}_${System.currentTimeMillis()}.png"
        return File(baseDirectory, fileName!!).also {
            this@SignUpActivity.imageFile = it
        }
    }

    private fun getFileUri(context: Context, file: File?): Uri? {
        return if (file != null) {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            null
        }
    }

    private fun getBaseDirectory(context: Context): File? {
        return context.filesDir
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                AlertDialog.Builder(this@SignUpActivity)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this@SignUpActivity)
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}
