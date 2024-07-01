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
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sajan.bktguide.R
import com.sajan.bktguide.storage.PrefLogin
import com.sajan.bktguide.storage.PrefsImageUri
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.InputStream
import android.view.MenuInflater
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.sajan.bktguide.controller.TouristInfoPostResponseController
import com.sajan.bktguide.dataModels.TouristDto
import com.sajan.bktguide.protocols.OnResponseListener
import com.sajan.bktguide.utils.*
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*


class EditActivity : AppCompatActivity(), View.OnClickListener {

    private var isNewPassword = false
    private var imageUri: Uri? = null
    private var fileName: String? = null
    private var storageCheckPermission: Int = 0
    private var cameraCheckPermission: Int = 0
    private var permissionRequestCode: Int = 345
    private val requestCodeCamera = 25
    private val requestCodeGallery = 27
    private var imageUriV2: Uri? = null
    private var progressDialog: ProgressDialog? = null

    private var touristDto: TouristDto? = null
    private var touristDtoV3: TouristDto? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        touristDto = intent?.getParcelableExtra("dto")
        setUpActionbar()
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

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

    }

    private fun setUpActionbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@EditActivity)
        progressDialog!!.setCancelable(false)
    }

    private fun initViews() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {

            toolbar.title = "Your Info"

            ivUpdateProfile.visibility = View.GONE
            tilUpdateName.visibility = View.GONE
            tilUpdateUsername.visibility = View.GONE
            tilUpdateEmail.visibility = View.GONE
            tilOldPassword.visibility = View.GONE
            rgPasswordToggle.visibility = View.GONE
            btnDone.visibility = View.GONE
            ivEdit.visibility = View.GONE

            ivLargeProfile.visibility = View.VISIBLE
            tilLargeName.visibility = View.VISIBLE
            tilLargeEmail.visibility = View.VISIBLE
            etLargeName.setText(touristDto!!.name)
            etLargeEmail.setText(touristDto!!.email)
            etLargeName.isEnabled = false
            etLargeEmail.isEnabled = false
            Picasso.get().load(touristDto!!.imageUri).into(ivLargeProfile)
        } else {
            toolbar.title = "Update your profile"
            if (PrefsImageUri(this@EditActivity).fileName != null &&
                (PrefsImageUri(this@EditActivity).fileName)!!.isNotEmpty()
            ) {
                imageUriV2 = PrefsImageUri(this@EditActivity).imgUri!!.toUri()
                this.fileName = PrefsImageUri(this@EditActivity).fileName
                val image = imageUriV2
                var bitmap: Bitmap? = null
                val imageStream: InputStream
                try {
                    imageStream = contentResolver.openInputStream(image!!)!!
                    bitmap = BitmapFactory.decodeStream(imageStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ivUpdateProfile.setImageBitmap(bitmap)
            }


            rgPasswordToggle.setOnCheckedChangeListener(mOnRgTogglePassword)

            ivUpdateProfile.visibility = View.VISIBLE
            tilUpdateName.visibility = View.VISIBLE
            tilUpdateUsername.visibility = View.VISIBLE
            tilUpdateEmail.visibility = View.VISIBLE
            tilOldPassword.visibility = View.VISIBLE
            rgPasswordToggle.visibility = View.VISIBLE
            btnDone.visibility = View.VISIBLE
            ivEdit.visibility = View.VISIBLE


            etUpdateName.setText(touristDto!!.name)
            etUpdateEmail.setText(touristDto!!.email)
            etUpdateUsername.setText(touristDto!!.username)
            etOldPassword.setText(touristDto!!.password)
            tilUpdateEmail.visibility = View.GONE

            ivLargeProfile.visibility = View.INVISIBLE
            tilLargeName.visibility = View.INVISIBLE
            tilLargeEmail.visibility = View.INVISIBLE

            btnDone.setOnClickListener(this)
            ivUpdateProfile.setOnClickListener(this)

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> {
                if (allFirstPhaseValid()) {
                    val name = etUpdateName.text.toString()
//                  val email = etUpdateEmail.text.toString()
                    val username = etUpdateUsername.text.toString()

                    if ((isNewPassword)) {
                        if (newPasswordValid()) {
                            val touristDtoV2 = TouristDto().apply {
                                this.id = touristDto!!.id
                                this.name = name
                                this.username = username
                                this.password = etNewPassword.text.toString()
                            }
                            postEditedData(touristDtoV2)
                        }
                    } else {
                        val touristDtoV2 = TouristDto().apply {
                            this.id = touristDto!!.id
                            this.name = name
                            this.username = username
                            this.password = etOldPassword.text.toString()
                        }
                        postEditedData(touristDtoV2)
                    }
                }
            }
            R.id.ivUpdateProfile -> {
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
                    AlertDialog.Builder(this@EditActivity)
                        .setItems(
                            arrayOf("Take a photo", "Choose from Gallery", "Remove photo")
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@EditActivity,
                                        getImageFile(
                                            this@EditActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                    cameraIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@EditActivity.packageManager) != null) {
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
                                    ivUpdateProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@EditActivity,
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
    }

    private var imageFile: File? = null

    private fun getImageFile(context: Context, imageTag: String): File? {
        val baseDirectory = getBaseDirectory(context)
        fileName = "${imageTag}_${System.currentTimeMillis()}.png"
        return File(baseDirectory, fileName!!).also {
            this@EditActivity.imageFile = it
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            requestCodeCamera -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageUriV2 = Uri.fromFile(imageFile)
                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                    val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath, options)
                    ivUpdateProfile.setImageBitmap(bitmap)
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
                            this@EditActivity,
                            getImageFile(
                                this@EditActivity,
                                UUID.randomUUID().toString()
                            )
                        )
                        val fos = FileOutputStream(imageFile!!)
                        imageUriV2 = Uri.fromFile(imageFile)
                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        ivUpdateProfile.setImageBitmap(bitmap)
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun postEditedData(touristDtoV2: TouristDto) {
        touristDtoV3 = touristDtoV2
        if (isInternetConnected) {
            val paramsBuilder = getParamEdit(touristDtoV2)
            val touristInfoUpdatePostResponse =
                TouristInfoPostResponseController(URL_UPDATE, mOnPostResponseListener)
            touristInfoUpdatePostResponse.execute(paramsBuilder.toString())
            this@EditActivity::class.java.log("dtoData--> $touristDtoV2")
            this@EditActivity::class.java.log("paramsData--> $paramsBuilder")
        } else {
            AlertDialog.Builder(this@EditActivity)
                .setCancelable(false)
                .setMessage("No internet connection")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }


    private val mOnPostResponseListener = object :
        OnResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.show()
            progressDialog!!.setMessage("Editing....")
            this@EditActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()

            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@EditActivity,
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


                        touristDtoV3!!.id = id
                        touristDtoV3!!.name = name
                        touristDtoV3!!.email = email
                        touristDtoV3!!.username = username
                        touristDtoV3!!.imageUri = imageUriV2

                        PrefLogin(this@EditActivity).id = touristDtoV3!!.id
                        PrefLogin(this@EditActivity).name = touristDtoV3!!.name
                        PrefLogin(this@EditActivity).email = touristDtoV3!!.email
                        PrefLogin(this@EditActivity).username = touristDtoV3!!.username
                        PrefLogin(this@EditActivity).password = touristDtoV3!!.password

                        PrefsImageUri(this@EditActivity).imgUri = touristDtoV3!!.imageUri.toString()
                        PrefsImageUri(this@EditActivity).fileName = fileName

                        this@EditActivity::class.java.log("Updated ImageUri: Uri--> ${touristDtoV3!!.imageUri}")
                        this@EditActivity::class.java.log(
                            "Updated Image File Name--> ${PrefsImageUri(
                                this@EditActivity
                            ).fileName}"
                        )
                        this@EditActivity::class.java.log("Updated PrefsData: ID-->${PrefLogin(this@EditActivity).id}")
                        this@EditActivity::class.java.log(
                            "Updated PrefsData:  Name-->${PrefLogin(
                                this@EditActivity
                            ).name}"
                        )
                        this@EditActivity::class.java.log(
                            "Updated PrefsData: Email-->${PrefLogin(
                                this@EditActivity
                            ).email}"
                        )
                        this@EditActivity::class.java.log(
                            "Updated PrefsData: Username-->${PrefLogin(
                                this@EditActivity
                            ).username}"
                        )
                        this@EditActivity::class.java.log(
                            "Updated PrefsData: Password--> ${PrefLogin(
                                this@EditActivity
                            ).password}"
                        )
                    }
                    val intent = Intent(this@EditActivity, MainActivity::class.java)
                    intent.putExtra("dto", touristDtoV3)
                    startActivity(intent)
                    finish()
                } else {
                    etUpdateUsername.requestFocus()
                    Snackbar.make(
                        clEdit,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
            this@EditActivity::class.java.log("server up: $response")
        }

        override fun onError(result: String?) {
            this@EditActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@EditActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private val mOnDeleteAccountResponseListener = object : OnResponseListener {
        override fun onStarted(url: String) {
            progressDialog!!.setMessage("Deleting....")
            progressDialog?.show()
            this@EditActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@EditActivity,
                        jsonObjectResponse.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                    val account = GoogleSignIn.getLastSignedInAccount(this@EditActivity)
                    if (account != null) {
                        googleSignInClient.signOut()
                            .addOnCompleteListener(this@EditActivity) {
                                googleSignInClient.revokeAccess()
                            }
                        startActivity(Intent(this@EditActivity, LoginActivity::class.java))
                        PrefsImageUri(this@EditActivity).resetImageData()
                        PrefLogin(this@EditActivity).resetLoginPrefs()
                        finish()
                    }
                } else {
                    Snackbar.make(
                        clEdit,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            this@EditActivity::class.java.log("server up: $response")
        }

        override fun onError(result: String?) {
            this@EditActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@EditActivity)
                .setCancelable(false)
                .setMessage("Failed to delete, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private val mOnRgTogglePassword = RadioGroup.OnCheckedChangeListener { _, checkId ->
        when (checkId) {
            R.id.rbOldPassword -> {
                isNewPassword = false
                tilNewPassword.visibility = View.GONE
                tilConfirmNewPassword.visibility = View.GONE
                etOldPassword.setText(PrefLogin(this@EditActivity).password)
                etOldPassword.isEnabled = false
                etNewPassword.text = null
                etConfirmNewPassword.text = null
                tilOldPassword.isErrorEnabled = false
                tilOldPassword.clearFocus()
            }

            R.id.rbNewPassword -> {
                isNewPassword = true
                etOldPassword.isEnabled = true
                tilNewPassword.visibility = View.VISIBLE
                tilConfirmNewPassword.visibility = View.VISIBLE
                etOldPassword.text = null
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
                    AlertDialog.Builder(this@EditActivity)
                        .setItems(
                            arrayOf(
                                "Take a photo",
                                "Choose from Gallery",
                                "Remove Photo"
                            )
                        ) { dialog, which ->
                            when (which) {
                                0 -> {
                                    val cameraIntent =
                                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    imageUri = getFileUri(
                                        this@EditActivity,
                                        getImageFile(
                                            this@EditActivity,
                                            UUID.randomUUID().toString()
                                        )
                                    )
                                    cameraIntent.putExtra(
                                        MediaStore.EXTRA_OUTPUT,
                                        imageUri
                                    )
                                    cameraIntent.flags =
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    if (cameraIntent.resolveActivity(this@EditActivity.packageManager) != null) {
                                        startActivityForResult(
                                            Intent.createChooser(
                                                cameraIntent,
                                                "Take picture"
                                            ),
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
                                    ivUpdateProfile.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@EditActivity,
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


    private fun allFirstPhaseValid(): Boolean {
        if (etUpdateName.text.toString().isEmpty()) {
            tilUpdateName.error = "Your name?"
            tilUpdateName.isErrorEnabled = true
            etUpdateName.requestFocus()
            return false
        } else {
            tilUpdateName.isErrorEnabled = false
            tilUpdateName.error = null
            etUpdateName.clearFocus()
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

        if (etUpdateUsername.text.toString().isEmpty()) {
            tilUpdateUsername.isErrorEnabled = true
            etUpdateUsername.requestFocus()
            tilUpdateUsername.error = "Your username?"
            return false
        } else {
            tilUpdateUsername.isErrorEnabled = false
            tilUpdateUsername.error = null
            etUpdateUsername.clearFocus()
        }
        return true
    }

    private fun newPasswordValid(): Boolean {
        if (etNewPassword.text.toString().isEmpty()) {
            tilNewPassword.isErrorEnabled = true
            etNewPassword.requestFocus()
            tilNewPassword.error = "Your new password?"
            return false
        } else {
            tilNewPassword.isErrorEnabled = false
            tilNewPassword.error = null
            etNewPassword.clearFocus()
        }

        if (etNewPassword.text.toString().length < 6) {
            tilNewPassword.isErrorEnabled = true
            etNewPassword.requestFocus()
            tilNewPassword.error = "Password too short, maximum 6 characters."
            return false
        } else {
            tilNewPassword.isErrorEnabled = false
            tilNewPassword.error = null
            etNewPassword.clearFocus()
        }
        if ((etNewPassword.text.toString()) != etConfirmNewPassword.text.toString()) {
            tilConfirmNewPassword.isErrorEnabled = true
            etConfirmNewPassword.requestFocus()
            tilConfirmNewPassword.error = "Password mis-match."
            return false
        } else {
            tilConfirmNewPassword.isErrorEnabled = false
            tilConfirmNewPassword.error = null
            etConfirmNewPassword.clearFocus()
        }

        if (etOldPassword.text.toString() != PrefLogin(this@EditActivity).password) {
            tilOldPassword.isErrorEnabled = true
            etOldPassword.requestFocus()
            tilOldPassword.error = "Old password incorrect"
            return false
        } else {
            tilOldPassword.isErrorEnabled = false
            tilOldPassword.error = null
            etOldPassword.clearFocus()
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu_edit_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                AlertDialog.Builder(this@EditActivity)
                    .setMessage("Are you sure you want to exit? No effects will occur!")
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        val intent = Intent(this@EditActivity, MainActivity::class.java)
                        intent.putExtra("dto", touristDto)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
            R.id.actionDeleteAccount -> {

                if (isInternetConnected) {
                    AlertDialog.Builder(this@EditActivity)
                        .setCancelable(false)
                        .setMessage("Do you really want to delete account?")
                        .setPositiveButton("Delete Account") { dialog, _ ->
                            dialog.dismiss()
                            val paramsBuilder = getParamEdit(touristDto!!)
                            val deleteAccountPostResponse =
                                TouristInfoPostResponseController(
                                    URL_DELETE,
                                    mOnDeleteAccountResponseListener
                                )
                            deleteAccountPostResponse.execute(paramsBuilder.toString())
                            this@EditActivity::class.java.log("paramsData--> $paramsBuilder")
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create().show()

                } else {
                    AlertDialog.Builder(this@EditActivity)
                        .setCancelable(false)
                        .setMessage("No internet connection")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this@EditActivity)
            .setMessage("Are you sure you want to exit? No effects will occur!")
            .setPositiveButton("Exit") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this@EditActivity, MainActivity::class.java)
                intent.putExtra("dto", touristDto)
                startActivity(intent)
                super.onBackPressed()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}
