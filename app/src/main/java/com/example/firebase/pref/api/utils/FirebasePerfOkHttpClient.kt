package com.example.firebase.pref.api.utils

import android.os.SystemClock
import android.util.Log
import androidx.annotation.Keep
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * @author: wangpan
 * @email: p.wang@aftership.com
 * @date: 2022/4/18
 */
@Keep
@Suppress("unused")
object FirebasePerfOkHttpClient {

    @JvmStatic
    fun enqueue(call: Call, callback: Callback) {
        val url = call.request().url().toString()
        val method = call.request().method()
        val startTime = SystemClock.uptimeMillis()
        var useTime: Long
        var isSuccessful: Boolean
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                useTime = SystemClock.uptimeMillis() - startTime
                isSuccessful = response.isSuccessful
                log(url, method, isSuccessful, useTime)
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call, e: IOException) {
                useTime = SystemClock.uptimeMillis() - startTime
                isSuccessful = false
                log(url, method, isSuccessful, useTime)
                callback.onFailure(call, e)
            }
        })
    }

    @JvmStatic
    @Throws(IOException::class)
    fun execute(call: Call): Response {
        val url = call.request().url().toString()
        val method = call.request().method()
        val startTime = SystemClock.uptimeMillis()
        var useTime = -1L
        var isSuccessful = false
        try {
            val response = call.execute()
            useTime = SystemClock.uptimeMillis() - startTime
            isSuccessful = response.isSuccessful
            return response
        } catch (e: IOException) {
            useTime = SystemClock.uptimeMillis() - startTime
            throw e
        } finally {
            log(url, method, isSuccessful, useTime)
        }
    }

    private fun log(url: String, method: String, isSuccessful: Boolean, useTime: Long) {
        Log.e("tag", "=========log start=========")
        Log.e("tag", "url: $url")
        Log.e("tag", "method: $method")
        Log.e("tag", "isSuccessful: $isSuccessful")
        Log.e("tag", "useTime: $useTime")
        Log.e("tag", "=========log end=========")
    }

}