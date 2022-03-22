package bou.amine.apps.readerforselfossv2.android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import bou.amine.apps.readerforselfossv2.android.LoginActivity

class Config(c: Context) {

    val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(c)

    val baseUrl: String
        get() = settings.getString("url", "")!!

    val userLogin: String
        get() = settings.getString("login", "")!!

    val userPassword: String
        get() = settings.getString("password", "")!!

    val httpUserLogin: String
        get() = settings.getString("httpUserName", "")!!

    val httpUserPassword: String
        get() = settings.getString("httpPassword", "")!!

    companion object {
        const val settingsName = "paramsselfoss"

        const val feedbackEmail = "aminecmi@gmail.com"

        const val translationUrl = "https://crwd.in/readerforselfoss"

        const val sourceUrl = "https://github.com/aminecmi/ReaderforSelfoss"

        const val trackerUrl = "https://github.com/aminecmi/ReaderforSelfoss/issues"

        const val syncChannelId = "sync-channel-id"

        const val newItemsChannelId = "new-items-channel-id"

        var apiVersion = 0

        /* Execute logout and clear all settings to default */
        fun logoutAndRedirect(
            c: Context,
            callingActivity: Activity,
            editor: SharedPreferences.Editor,
            baseUrlFail: Boolean = false
        ): Boolean {
            val settings = PreferenceManager.getDefaultSharedPreferences(c)
            settings.edit().clear().commit()
            val intent = Intent(c, LoginActivity::class.java)
            if (baseUrlFail) {
                intent.putExtra("baseUrlFail", baseUrlFail)
            }
            c.startActivity(intent)
            callingActivity.finish()
            return true
        }
    }
}
