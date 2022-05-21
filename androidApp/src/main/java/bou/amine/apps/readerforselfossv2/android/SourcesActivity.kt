package bou.amine.apps.readerforselfossv2.android

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import bou.amine.apps.readerforselfossv2.android.adapters.SourcesListAdapter
import bou.amine.apps.readerforselfossv2.android.databinding.ActivitySourcesBinding
import bou.amine.apps.readerforselfossv2.android.service.AndroidApiDetailsService
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.themes.Toppings
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import com.ftinc.scoop.Scoop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class SourcesActivity : AppCompatActivity() {

    private lateinit var appColors: AppColors
    private lateinit var binding: ActivitySourcesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        appColors = AppColors(this@SourcesActivity)
        binding = ActivitySourcesBinding.inflate(layoutInflater)
        val view = binding.root

        val scoop = Scoop.getInstance()
        scoop.bind(this, Toppings.PRIMARY.value, binding.toolbar)
        scoop.bindStatusBar(this, Toppings.PRIMARY_DARK.value)

        super.onCreate(savedInstanceState)

        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.fab.rippleColor = appColors.colorAccentDark
        binding.fab.backgroundTintList = ColorStateList.valueOf(appColors.colorAccent)
    }

    override fun onStop() {
        super.onStop()
        binding.recyclerView.clearOnScrollListeners()
    }

    override fun onResume() {
        super.onResume()
        val mLayoutManager = LinearLayoutManager(this)

        val settings =
            getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val apiDetailsService = AndroidApiDetailsService(this@SourcesActivity)
        val api = SelfossApi(
//            this,
//            this@SourcesActivity,
//            settings.getBoolean("isSelfSignedCert", false),
//            prefs.getString("api_timeout", "-1")!!.toLong()
            apiDetailsService
        )
        var items: ArrayList<SelfossModel.Source>

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = mLayoutManager

        if (this@SourcesActivity.isNetworkAvailable(binding.recyclerView)) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = api.sources()
                if (response != null) {
                    items = response
                    val mAdapter = SourcesListAdapter(this@SourcesActivity, items, api,
                        apiDetailsService
                    )
                    binding.recyclerView.adapter = mAdapter
                    mAdapter.notifyDataSetChanged()
                    if (items.isEmpty()) {
                        Toast.makeText(
                            this@SourcesActivity,
                            R.string.nothing_here,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@SourcesActivity,
                        R.string.cant_get_sources,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this@SourcesActivity, AddSourceActivity::class.java))
        }
    }
}
