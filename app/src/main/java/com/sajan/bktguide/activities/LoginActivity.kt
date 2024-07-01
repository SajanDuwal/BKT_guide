package com.sajan.bktguide.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.sajan.bktguide.R
import com.sajan.bktguide.controller.TouristInfoPostResponseController
import com.sajan.bktguide.dataModels.TouristDto
import com.sajan.bktguide.protocols.OnResponseListener
import com.sajan.bktguide.storage.PrefLogin
import com.sajan.bktguide.storage.PrefsImageUri
import com.sajan.bktguide.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var progressDialog: ProgressDialog? = null
    private var touristDtoV2: TouristDto? = null
    private var touristInfoDto = TouristDto()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val REQUEST_GOOGLE_SIGIN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (PrefLogin(this@LoginActivity).isLogin) {
            val id = PrefLogin(this@LoginActivity).id
            val name = PrefLogin(this@LoginActivity).name
            val email = PrefLogin(this@LoginActivity).email
            val username = PrefLogin(this@LoginActivity).username
            val password = PrefLogin(this@LoginActivity).password

            val touristDto: TouristDto
            touristDto = TouristDto().apply {
                this.id = id
                this.name = name
                this.email = email
                this.username = username
                this.password = password
                this.imageUri = PrefsImageUri(this@LoginActivity).imgUri!!.toUri()
            }
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra("dto", touristDto)
            startActivity(intent)
            finish()
        } else {
            setContentView(R.layout.activity_login)
            setUpProgressDialog()
            initViews()

            val googleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestProfile()
                    .requestEmail()
                    .build()
            googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        }
    }

    private fun initViews() {
        btnSignUp.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
        btnGoogleSignIn.setOnClickListener(this)
        tvForgetPassword.setOnClickListener(this)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog!!.setMessage("Login....")
        progressDialog!!.setCancelable(false)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSignUp -> {
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                finish()
            }

            R.id.btnLogin -> {
                if (allFieldValid()) {
                    val username = etUsername.text.toString()
                    val password = etPassword.text.toString()

                    touristDtoV2 = TouristDto().apply {
                        this.username = username
                        this.password = password
                    }

                    val paramsBuilder = getLoginParam(touristDtoV2!!)
                    if (isInternetConnected) {
                        val loginResponseController =
                            TouristInfoPostResponseController(URL_LOGIN, mOnLoginResponse)
                        loginResponseController.execute(paramsBuilder.toString())
                    } else {
                        AlertDialog.Builder(this@LoginActivity)
                            .setCancelable(false)
                            .setMessage("No internet connected")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
                }
            }

            R.id.tvForgetPassword -> {
                startActivity(Intent(this@LoginActivity, ForgetPassword::class.java))
                finish()
            }

            R.id.btnGoogleSignIn -> {
                if (isInternetConnected) {
                    val intent = googleSignInClient.signInIntent
                    startActivityForResult(intent, REQUEST_GOOGLE_SIGIN_IN)
                } else {
                    Toast.makeText(this@LoginActivity, "No interned connected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private val mOnLoginResponse = object :
        OnResponseListener {
        override fun onStarted(url: String) {
            progressDialog?.show()
            this@LoginActivity::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@LoginActivity::class.java.log("server up login Response: $response")
            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@LoginActivity,
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

                        touristDtoV2!!.id = id
                        touristDtoV2!!.name = name
                        touristDtoV2!!.email = email
                        touristDtoV2!!.username = username
                        touristDtoV2!!.imageUri = PrefsImageUri(this@LoginActivity).imgUri!!.toUri()

                        PrefLogin(this@LoginActivity).isLogin = true
                        PrefLogin(this@LoginActivity).id = touristDtoV2!!.id
                        PrefLogin(this@LoginActivity).name = touristDtoV2!!.name
                        PrefLogin(this@LoginActivity).email = touristDtoV2!!.email
                        PrefLogin(this@LoginActivity).username = touristDtoV2!!.username
                        PrefLogin(this@LoginActivity).password = touristDtoV2!!.password

                        this@LoginActivity::class.java.log(
                            "ImageUri (login): Uri--> ${PrefsImageUri(
                                this@LoginActivity
                            ).imgUri}"
                        )
                        this@LoginActivity::class.java.log(
                            "File Name (login): Uri--> ${PrefsImageUri(
                                this@LoginActivity
                            ).fileName}"
                        )
                        this@LoginActivity::class.java.log(
                            "PrefsData: (login)ID-->${PrefLogin
                                (
                                this@LoginActivity
                            ).id}"
                        )
                        this@LoginActivity::class.java.log(
                            "PrefsData:  (login)Name-->${PrefLogin(
                                this@LoginActivity
                            ).name}"
                        )
                        this@LoginActivity::class.java.log(
                            "PrefsData: (login)Email-->${PrefLogin(
                                this@LoginActivity
                            ).email}"
                        )
                        this@LoginActivity::class.java.log(
                            "PrefsData: (login)Username-->${PrefLogin(
                                this@LoginActivity
                            ).username}"
                        )
                        this@LoginActivity::class.java.log(
                            "PrefsData: (login)Password--> ${PrefLogin(
                                this@LoginActivity
                            ).password}"
                        )
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("dto", touristDtoV2)
                    startActivity(intent)
                    finish()
                } else {
                    etUsername.requestFocus()
                    Snackbar.make(
                        clRoot,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@LoginActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@LoginActivity)
                .setCancelable(false)
                .setMessage("Login Failed, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private fun allFieldValid(): Boolean {
        if (etUsername.text.toString().isEmpty()) {
            tilUsername.isErrorEnabled = true
            etUsername.requestFocus()
            tilUsername.error = "Provide username?"
            return false
        } else {
            tilUsername.isErrorEnabled = false
            tilUsername.error = null
            etUsername.clearFocus()
        }
        if (etPassword.text.toString().isEmpty()) {
            tilPassword.isErrorEnabled = true
            etPassword.requestFocus()
            tilPassword.error = "Provide password?"
            return false
        } else {
            tilPassword.isErrorEnabled = false
            tilPassword.error = null
            etPassword.clearFocus()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_GOOGLE_SIGIN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val task: Task<GoogleSignInAccount> =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleAccountData(task)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleAccountData(task: Task<GoogleSignInAccount>) {
        try {
            if (task.isSuccessful) {
                val account = task.getResult(ApiException::class.java)
                userData(account, null)
            }
        } catch (e: ApiException) {
            userData(null, task.exception!!.localizedMessage)
        }
    }

    private fun userData(account: GoogleSignInAccount?, message: String?) {
        if (account != null) {
            val name = account.displayName
            val email = account.email
            val photoUri = account.photoUrl

            touristInfoDto.name = name
            touristInfoDto.email = email
            touristInfoDto.imageUri = photoUri
            PrefsImageUri(this@LoginActivity).imgUri = photoUri.toString()
            PrefsImageUri(this@LoginActivity).fileName = name

            if (isInternetConnected) {
                val stringParams = getParam(touristInfoDto)
                val getGmailTouristPostResponse =
                    TouristInfoPostResponseController(URL_GMAIL_LOGIN, mOnGmailLoginResponse)
                getGmailTouristPostResponse.execute(stringParams.toString())
            }
        } else {
            Toast.makeText(this@LoginActivity, "login failed ($message)", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val mOnGmailLoginResponse = object :
        OnResponseListener {
        override fun onStarted(url: String) {
            progressDialog?.show()
            this@LoginActivity::class.java.log("connectiog to url -> $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@LoginActivity::class.java.log("server up: $response")

            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@LoginActivity,
                        "LoggedIn",
                        Toast.LENGTH_SHORT
                    ).show()

                    val jsonArrayResponseData = jsonObjectResponse.getJSONArray("data")
                    for (j in 0 until jsonArrayResponseData.length()) {
                        val jsonObjectResponseData = jsonArrayResponseData.getJSONObject(j)
                        val id = jsonObjectResponseData.getInt("id")
                        val name = jsonObjectResponseData.getString("name")
                        val email = jsonObjectResponseData.getString("email")
                        val username = jsonObjectResponseData.getString("username")

                        touristInfoDto.id = id
                        touristInfoDto.name = name
                        touristInfoDto.email = email
                        touristInfoDto.username = username

                        PrefLogin(this@LoginActivity).isLogin = true

                        PrefLogin(this@LoginActivity).id = touristInfoDto.id
                        PrefLogin(this@LoginActivity).name = touristInfoDto.name
                        PrefLogin(this@LoginActivity).email = touristInfoDto.email
                        PrefLogin(this@LoginActivity).username = touristInfoDto.username

                        this@LoginActivity::class.java.log("PrefsData: ID-->${PrefLogin(this@LoginActivity).id}")
                        this@LoginActivity::class.java.log("PrefsData:  Name-->${PrefLogin(this@LoginActivity).name}")
                        this@LoginActivity::class.java.log("PrefsData: Email-->${PrefLogin(this@LoginActivity).email}")
                        this@LoginActivity::class.java.log("PrefsData: Username-->${PrefLogin(this@LoginActivity).username}")
                        this@LoginActivity::class.java.log("PrefsData: Username-->${touristInfoDto}")
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("dto", touristInfoDto)
                    startActivity(intent)
                    finish()
                }
            }
        }

        override fun onError(result: String?) {
            this@LoginActivity::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@LoginActivity)
                .setCancelable(false)
                .setMessage("Failed in registration, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

    }

    override fun onStart() {
        super.onStart()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            userData(account, null)
        }
    }
}
