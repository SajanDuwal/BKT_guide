package com.sajan.bktguide.protocols

interface OnResponseListener {
    fun onStarted(url: String)
    fun onComplete(response: String)
    fun onError(result: String?)
}