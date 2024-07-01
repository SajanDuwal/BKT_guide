package com.sajan.bktguide.controller

import android.os.AsyncTask
import com.sajan.bktguide.protocols.OnResponseListener
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TouristInfoPostResponseController(
    private val url: String,
    private val onResponseListener: OnResponseListener
) :
    AsyncTask<String, Void, String>() {

    override fun onPreExecute() {
        onResponseListener.onStarted(url)
    }

    override fun doInBackground(vararg params: String): String {
        return try {
            val urlToConnect = URL(url)
            val httpURLConnection = urlToConnect.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.addRequestProperty("Accept", "application/json")
            httpURLConnection.addRequestProperty(
                "Content-type",
                "application/x-www-form-urlencoded"
            )
            httpURLConnection.doOutput = true
            httpURLConnection.doInput = true

            val dataOutputStream = DataOutputStream(httpURLConnection.outputStream)
            dataOutputStream.writeBytes(params[0])
            dataOutputStream.flush()
            dataOutputStream.close()

            httpURLConnection.connect()

            return if (httpURLConnection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
                val builder = StringBuilder()
                var line: String?
                do {
                    line = reader.readLine()
                    if (line != null) {
                        builder.append(line)
                    }
                } while (line != null)
                builder.toString()
            } else {
                val reader = BufferedReader(InputStreamReader(httpURLConnection.errorStream))
                val builder = StringBuilder()
                var line: String?
                do {
                    line = reader.readLine()
                    if (line != null) {
                        builder.append(line)
                    }
                } while (line != null)
                builder.toString()
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null && !result.startsWith("Error")) {
            onResponseListener.onComplete(result)
        } else {
            onResponseListener.onError(result)
        }
    }
}