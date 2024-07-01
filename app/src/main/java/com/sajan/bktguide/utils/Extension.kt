package com.sajan.bktguide.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.sajan.bktguide.BuildConfig
import com.sajan.bktguide.dataModels.TouristDto

fun <T> Class<T>.log(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e("BKT GUIDE", "${this.simpleName}::$message")
    }
}

fun getParam(touristDto: TouristDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "name=" + touristDto.name +
                "&email=" + touristDto.email +
                "&username=" + touristDto.username +
                //"&imageUri=" + touristDto.imageUri +
                "&password=" + touristDto.password
    )
    return paramBuilder
}

fun getParamEdit(touristDto: TouristDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "id=" + touristDto.id +
                "&name=" + touristDto.name +
                "&email=" + touristDto.email +
                "&username=" + touristDto.username +
                //"&imageUri=" + touristDto.imageUri +
                "&password=" + touristDto.password
    )
    return paramBuilder
}

fun getLoginParam(touristDto: TouristDto): StringBuilder {
    val paramBuilder = StringBuilder()
    paramBuilder.append(
        "username=" + touristDto.username +
                "&password=" + touristDto.password
    )
    return paramBuilder
}

val Context.isInternetConnected: Boolean
    get() {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
