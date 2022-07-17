package bou.amine.apps.readerforselfossv2.android.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService

class AndroidApiDetailsService(c: Context) : ApiDetailsService {
    val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(c)
    private var _apiVersion: Int = -1
    private var _baseUrl: String = ""
    private var _userName: String = ""
    private var _password: String = ""
    override fun logApiCalls(message: String) {
        Log.d("LogApiCalls", message)
    }


    override fun getApiVersion(): Int {
        if (_apiVersion == -1) {
            _apiVersion = settings.getInt("apiVersionMajor", -1)
            return _apiVersion
        }
        return _apiVersion
    }

    override fun getBaseUrl(): String {
        if (_baseUrl.isEmpty()) {
            _baseUrl = settings.getString("url", "")!!
        }
        return _baseUrl
    }

    override fun getUserName(): String {
        if (_userName.isEmpty()) {
            _userName = settings.getString("login", "")!!
        }
        return _userName
    }

    override fun getPassword(): String {
        if (_password.isEmpty()) {
            _password = settings.getString("password", "")!!
        }
        return _password
    }

    override fun refresh() {
        _password = settings.getString("password", "")!!
        _userName = settings.getString("login", "")!!
        _baseUrl = settings.getString("url", "")!!
        _baseUrl = settings.getString("url", "")!!
        _apiVersion = settings.getInt("apiVersionMajor", -1)
    }
}