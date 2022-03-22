package bou.amine.apps.readerforselfossv2.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import bou.amine.apps.readerforselfossv2.android.HomeActivity
import bou.amine.apps.readerforselfossv2.android.LoginActivity
import bou.amine.apps.readerforselfossv2.android.MainActivity
import bou.amine.apps.readerforselfossv2.android.utils.Config
import org.junit.After

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    lateinit var intent: Intent
    lateinit var preferencesEditor: SharedPreferences.Editor
    private lateinit var url: String
    private lateinit var username: String
    private lateinit var password: String

    @Rule @JvmField
    val rule = ActivityTestRule(MainActivity::class.java, true, false)

    @Before
    fun setUp() {
        intent = Intent()
        val context = getInstrumentation().targetContext

        // create a SharedPreferences editor
        preferencesEditor = context.getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE).edit()

        url = BuildConfig.LOGIN_URL
        username = BuildConfig.LOGIN_USERNAME
        password = BuildConfig.LOGIN_PASSWORD

        Intents.init()
    }

    @Test
    fun checkFirstOpenLaunchesIntro() {
        preferencesEditor.putString("url", "")
        preferencesEditor.putString("password", "")
        preferencesEditor.putString("login", "")
        preferencesEditor.commit()

        rule.launchActivity(intent)

        intended(hasComponent(LoginActivity::class.java.name))
        intended(hasComponent(HomeActivity::class.java.name), times(0))
    }

    @Test
    fun checkNotFirstOpenLaunchesLogin() {
        preferencesEditor.putString("url", url)
        preferencesEditor.putString("password", password)
        preferencesEditor.putString("login", username)
        preferencesEditor.commit()

        rule.launchActivity(intent)

        intended(hasComponent(MainActivity::class.java.name))
        intended(hasComponent(HomeActivity::class.java.name))
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }
}