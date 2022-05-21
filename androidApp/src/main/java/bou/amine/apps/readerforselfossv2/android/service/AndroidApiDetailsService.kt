package bou.amine.apps.readerforselfossv2.android.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService

class AndroidApiDetailsService(c: Context) : ApiDetailsService {
    val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(c)
    private var apiVersion: Int = -1
    private var baseUrl: String = ""
    private var userName: String = ""
    private var password: String = ""
    override fun logApiCalls(message: String) {
        Log.d("LogApiCalls", message)
    }


    override fun getApiVersion(): Int {
        if (apiVersion != -1) {
            apiVersion = settings.getInt("apiVersion", -1)!!
        }
        return apiVersion
    }

    override fun getBaseUrl(): String {
        if (baseUrl.isEmpty()) {
            baseUrl = settings.getString("url", "")!!
        }
        return baseUrl
    }

    override fun getUserName(): String {
        if (userName.isEmpty()) {
            userName = settings.getString("login", "")!!
        }
        return userName
    }

    override fun getPassword(): String {
        if (password.isEmpty()) {
            password = settings.getString("password", "")!!
        }
        return password
    }
}