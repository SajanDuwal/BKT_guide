package com.sajan.bktguide.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.sajan.bktguide.R
import com.sajan.bktguide.controller.TouristInfoPostResponseController
import com.sajan.bktguide.dataModels.TouristDto
import com.sajan.bktguide.protocols.OnResponseListener
import com.sajan.bktguide.utils.*
import kotlinx.android.synthetic.main.activity_forget_password.*
import org.json.JSONArray

class ForgetPassword : AppCompatActivity(), View.OnClickListener {

    private var touristDtoV2 = TouristDto()
    private var btnResetPasswordToggle: Button? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_forget_password)
        setUpActionBar()
        initView()
        setUpProgressDialog()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView() {
        tilNewResetPassword.visibility = View.GONE
        tilResetNewConfirmPassword.visibility = View.GONE
        tilUsername.visibility = View.VISIBLE
        btnResetPasswordToggle = findViewById(R.id.btnResetPasswordToggle)
        btnResetPasswordToggle!!.text = "OK"
        btnResetPasswordToggle!!.setOnClickListener(this)
    }

    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(this@ForgetPassword)
        progressDialog!!.setCancelable(false)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnResetPasswordToggle -> {
                if (btnResetPasswordToggle!!.text == "OK") {
                    if (etUsername.text.toString().isEmpty()) {
                        tilUsername.isErrorEnabled = true
                        tilUsername.error = "Provide Username"
                        etUsername.requestFocus()
                    } else {
                        tilUsername.isErrorEnabled = false
                        tilUsername.error = null
                        etUsername.clearFocus()
                        val username = etUsername.text.toString()
                        val stringParams = "username=$username"
                        if (isInternetConnected) {
                            val getUserDetailPostResponse =
                                TouristInfoPostResponseController(
                                    URL_GET_ID,
                                    mOnPostResponseListener
                                )
                            getUserDetailPostResponse.execute(stringParams)
                        } else {
                            AlertDialog.Builder(this@ForgetPassword)
                                .setCancelable(false)
                                .setMessage("No internet connected")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create().show()
                        }
                    }
                } else if (btnResetPasswordToggle!!.text == "Done") {
                    if (newPasswordValid()) {
                        val password = etResetNewPassword.text.toString()
                        touristDtoV2.password = password
                        val stringParams = getParamEdit(touristDtoV2)
                        if (isInternetConnected) {
                            val getPasswordResetPostResponse =
                                TouristInfoPostResponseController(
                                    URL_UPDATE,
                                    mOnPasswordRestPostResponseListener
                                )
                            getPasswordResetPostResponse.execute(stringParams.toString())
                        } else {
                            AlertDialog.Builder(this@ForgetPassword)
                                .setCancelable(false)
                                .setMessage("No internet connected")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create().show()
                        }
                    }
                }
            }
        }
    }

    private val mOnPostResponseListener = object :
        OnResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.setTitle("Verifying....")
            progressDialog?.show()
            this@ForgetPassword::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@ForgetPassword::class.java.log("server up: --- > $response")
            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@ForgetPassword,
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

                        touristDtoV2.id = id
                        touristDtoV2.name = name
                        touristDtoV2.email = email
                        touristDtoV2.username = username

                        this@ForgetPassword::class.java.log("proceed TO RESET--- > $touristDtoV2")
                    }
                    tilNewResetPassword.visibility = View.VISIBLE
                    tilResetNewConfirmPassword.visibility = View.VISIBLE
                    tilUsername.isEnabled = false
                    btnResetPasswordToggle!!.text = "Done"
                    tvInfo.visibility = View.GONE
                } else {
                    etUsername.requestFocus()
                    Snackbar.make(
                        clForgetPassword,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@ForgetPassword::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@ForgetPassword)
                .setCancelable(false)
                .setMessage("Verification Failed, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private val mOnPasswordRestPostResponseListener = object :
        OnResponseListener {

        override fun onStarted(url: String) {
            progressDialog?.setTitle("Processing....")
            progressDialog?.show()
            this@ForgetPassword::class.java.log("connecting to $url")
        }

        override fun onComplete(response: String) {
            progressDialog?.dismiss()
            this@ForgetPassword::class.java.log("server up: $response")
            val jsonArrayResponse = JSONArray(response)
            for (i in 0 until jsonArrayResponse.length()) {
                val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
                if (jsonObjectResponse.getBoolean("response")) {
                    Toast.makeText(
                        this@ForgetPassword,
                        jsonObjectResponse.getString("Password reset"),
                        Toast.LENGTH_SHORT
                    ).show()

                    val jsonArrayResponseData = jsonObjectResponse.getJSONArray("data")
                    for (j in 0 until jsonArrayResponseData.length()) {
                        val jsonObjectResponseData = jsonArrayResponseData.getJSONObject(j)
                        val id = jsonObjectResponseData.getInt("id")
                        val name = jsonObjectResponseData.getString("name")
                        val email = jsonObjectResponseData.getString("email")
                        val username = jsonObjectResponseData.getString("username")
                        touristDtoV2.id = id
                        touristDtoV2.name = name
                        touristDtoV2.email = email
                        touristDtoV2.username = username
                    }
                    this@ForgetPassword::class.java.log("On reset password data --- > $touristDtoV2")
                    startActivity(Intent(this@ForgetPassword, LoginActivity::class.java))
                    finish()
                } else {
                    etUsername.requestFocus()
                    Snackbar.make(
                        clForgetPassword,
                        jsonObjectResponse.getString("message"),
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

        override fun onError(result: String?) {
            this@ForgetPassword::class.java.log("Error : $result")
            progressDialog?.dismiss()
            AlertDialog.Builder(this@ForgetPassword)
                .setCancelable(false)
                .setMessage("Failed to reset password, Try again!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    private fun newPasswordValid(): Boolean {
        if (etResetNewPassword.text.toString().isEmpty()) {
            tilNewResetPassword.isErrorEnabled = true
            etResetNewPassword.requestFocus()
            tilNewResetPassword.error = "Your new password?"
            return false
        } else {
            tilNewResetPassword.isErrorEnabled = false
            tilNewResetPassword.error = null
            etResetNewPassword.clearFocus()
        }

        if (etResetNewPassword.text.toString().length < 6) {
            tilNewResetPassword.isErrorEnabled = true
            etResetNewPassword.requestFocus()
            tilNewResetPassword.error = "Password too short, maximum 6 characters."
            return false
        } else {
            tilNewResetPassword.isErrorEnabled = false
            tilNewResetPassword.error = null
            etResetNewPassword.clearFocus()
        }
        if ((etResetNewPassword.text.toString()) != etResetNewConfirmPassword.text.toString()) {
            tilResetNewConfirmPassword.isErrorEnabled = true
            etResetNewConfirmPassword.requestFocus()
            tilResetNewConfirmPassword.error = "Password mis-match."
            return false
        } else {
            tilResetNewConfirmPassword.isErrorEnabled = false
            tilResetNewConfirmPassword.error = null
            etResetNewConfirmPassword.clearFocus()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                AlertDialog.Builder(this@ForgetPassword)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(this@ForgetPassword, LoginActivity::class.java))
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
        AlertDialog.Builder(this@ForgetPassword)
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this@ForgetPassword, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }
}
