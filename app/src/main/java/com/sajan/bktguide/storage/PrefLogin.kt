package com.sajan.bktguide.storage

import android.content.Context

class PrefLogin(context: Context) {

    private val prefsManager = context.getSharedPreferences("Login_Prefs", Context.MODE_PRIVATE)

    private val keyIsLogin = "KEY_IS_Login"
    private val keyEmail = "KEY_EMAIL"
    private val keyName = "KEY_NAME"
    private val keyUsername = "KEY_USERNAME"
    private val keyPassword = "KEY_PASSWORD"
    private val keyID = "KEY_ID"

    var isLogin: Boolean
        set(value) {
            prefsManager.edit().putBoolean(keyIsLogin, value).apply()
        }
        get() {
            return prefsManager.getBoolean(keyIsLogin, false)
        }

    var id: Int
        set(value) {
            prefsManager.edit().putInt(keyID, value).apply()
        }
        get() {
            return prefsManager.getInt(keyID, 0)
        }

    var name: String?
        set(value) {
            prefsManager.edit().putString(keyName, value).apply()
        }
        get() {
            return prefsManager.getString(keyName, null)
        }


    var email: String?
        set(value) {
            prefsManager.edit().putString(keyEmail, value).apply()
        }
        get() {
            return prefsManager.getString(keyEmail, null)
        }


    var username: String?
        set(value) {
            prefsManager.edit().putString(keyUsername, value).apply()
        }
        get() {
            return prefsManager.getString(keyUsername, null)
        }


    var password: String?
        set(value) {
            prefsManager.edit().putString(keyPassword, value).apply()
        }
        get() {
            return prefsManager.getString(keyPassword, null)
        }

    fun resetLoginPrefs() {
        prefsManager.edit().clear().apply()
    }
}