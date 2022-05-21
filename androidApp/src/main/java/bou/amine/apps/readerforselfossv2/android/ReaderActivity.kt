package bou.amine.apps.readerforselfossv2.android

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import bou.amine.apps.readerforselfossv2.android.databinding.ActivityReaderBinding
import bou.amine.apps.readerforselfossv2.android.fragments.ArticleFragment
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_1_2
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_2_3
import bou.amine.apps.readerforselfossv2.android.persistence.migrations.MIGRATION_3_4
import bou.amine.apps.readerforselfossv2.android.service.AndroidApiDetailsService
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.themes.Toppings
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.toggleStar
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import com.ftinc.scoop.Scoop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReaderActivity : AppCompatActivity() {

    private var markOnScroll: Boolean = false
    private var currentItem: Int = 0
    private lateinit var userIdentifier: String
    private lateinit var appColors: AppColors

    private lateinit var api: SelfossApi

    private lateinit var toolbarMenu: Menu

    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    private lateinit var binding: ActivityReaderBinding

    private var activeAlignment: Int = 1
    private val JUSTIFY = 1
    private val ALIGN_LEFT = 2

    private fun showMenuItem(willAddToFavorite: Boolean) {
        if (willAddToFavorite) {
            toolbarMenu.findItem(R.id.star).icon.setTint(Color.WHITE)
        } else {
            toolbarMenu.findItem(R.id.star).icon.setTint(Color.RED)
        }
    }

    private fun canFavorite() {
        showMenuItem(true)
    }

    private fun canRemoveFromFavorite() {
        showMenuItem(false)
    }

    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appColors = AppColors(this)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "selfoss-database"
        ).addMigrations(MIGRATION_1_2).addMigrations(MIGRATION_2_3).addMigrations(MIGRATION_3_4).build()

        val scoop = Scoop.getInstance()
        scoop.bind(this, Toppings.PRIMARY.value, binding.toolBar)
        scoop.bindStatusBar(this, Toppings.PRIMARY_DARK.value)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val settings =
            getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefs.edit()

        userIdentifier = prefs.getString("unique_id", "")!!
        markOnScroll = prefs.getBoolean("mark_on_scroll", false)
        activeAlignment = prefs.getInt("text_align", JUSTIFY)

        api = SelfossApi(
//            this,
//            this@ReaderActivity,
//            settings.getBoolean("isSelfSignedCert", false),
//            prefs.getString("api_timeout", "-1")!!.toLong()
            AndroidApiDetailsService(this@ReaderActivity)
        )

        if (allItems.isEmpty()) {
            finish()
        }

        currentItem = intent.getIntExtra("currentItem", 0)

        readItem(allItems[currentItem])

        binding.pager.adapter = ScreenSlidePagerAdapter(this)
        binding.pager.setCurrentItem(currentItem, false)
    }

    override fun onResume() {
        super.onResume()

        binding.indicator.setViewPager(binding.pager)
    }

    private fun readItem(item: SelfossModel.Item) {
        if (markOnScroll) {
                CoroutineScope(Dispatchers.IO).launch {
                    api.markAsRead(item.id.toString())
                    // TODO: update item in DB
                }
            }
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) :
        FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = allItems.size

        override fun createFragment(position: Int): Fragment = ArticleFragment.newInstance(allItems[position])

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val currentFragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.currentItem) as ArticleFragment
                currentFragment.scrollDown()
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                val currentFragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.currentItem) as ArticleFragment
                currentFragment.scrollUp()
                true
            }
            else -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    private fun alignmentMenu(showJustify: Boolean) {
        toolbarMenu.findItem(R.id.align_left).isVisible = !showJustify
        toolbarMenu.findItem(R.id.align_justify).isVisible = showJustify
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.reader_menu, menu)
        toolbarMenu = menu

        if (allItems.isNotEmpty() && allItems[currentItem].starred) {
            canRemoveFromFavorite()
        } else {
            canFavorite()
        }
        if (activeAlignment == JUSTIFY) {
            alignmentMenu(false)
        } else {
            alignmentMenu(true)
        }

        binding.pager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        if (allItems[position].starred) {
                            canRemoveFromFavorite()
                        } else {
                            canFavorite()
                        }
                        readItem(allItems[position])
                    }
                }
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun afterSave() {
            allItems[binding.pager.currentItem] =
                    allItems[binding.pager.currentItem].toggleStar()
            canRemoveFromFavorite()
        }

        fun afterUnsave() {
            allItems[binding.pager.currentItem] = allItems[binding.pager.currentItem].toggleStar()
            canFavorite()
        }

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.star -> {
                if (allItems[binding.pager.currentItem].starred) {
                    CoroutineScope(Dispatchers.IO).launch {
                        api.unstarr(allItems[binding.pager.currentItem].id.toString())
                        // TODO: update in DB
                    }
                    afterUnsave()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        api.starr(allItems[binding.pager.currentItem].id.toString())
                        // TODO: update in DB
                    }
                    afterSave()
                }
            }
            R.id.align_left -> {
                editor.putInt("text_align", ALIGN_LEFT)
                editor.apply()
                alignmentMenu(true)
                refreshFragment()
            }
            R.id.align_justify -> {
                editor.putInt("text_align", JUSTIFY)
                editor.apply()
                alignmentMenu(false)
                refreshFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshFragment() {
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    companion object {
        var allItems: ArrayList<SelfossModel.Item> = ArrayList()
    }
}
