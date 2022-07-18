package bou.amine.apps.readerforselfossv2.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.themes.Toppings
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.isBaseUrlValid
import com.ftinc.scoop.Scoop
import bou.amine.apps.readerforselfossv2.android.databinding.ActivityAddSourceBinding

import bou.amine.apps.readerforselfossv2.rest.SelfossApiImpl
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance


class AddSourceActivity : AppCompatActivity(), DIAware {

    private var mSpoutsValue: String? = null
    private lateinit var api: SelfossApiImpl

    private lateinit var appColors: AppColors
    private lateinit var binding: ActivityAddSourceBinding

    override val di by closestDI()
    private val apiDetailsService : ApiDetailsService by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        appColors = AppColors(this@AddSourceActivity)

        super.onCreate(savedInstanceState)
        binding = ActivityAddSourceBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        val scoop = Scoop.getInstance()
        scoop.bind(this, Toppings.PRIMARY.value, binding.toolbar)
        scoop.bindStatusBar(this, Toppings.PRIMARY_DARK.value)

        val drawable = binding.nameInput.background
        drawable.setTint(appColors.colorAccent)


        // TODO: clean
        binding.nameInput.background = drawable

        val drawable1 = binding.sourceUri.background
        drawable1.setTint(appColors.colorAccent)

        binding.sourceUri.background = drawable1

        val drawable2 = binding.tags.background
        drawable2.setTint(appColors.colorAccent)

        binding.tags.background = drawable2

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val settings =
                getSharedPreferences(Config.settingsName, Context.MODE_PRIVATE)
            api = SelfossApiImpl(
//                this,
//                this@AddSourceActivity,
//                settings.getBoolean("isSelfSignedCert", false),
//                prefs.getString("api_timeout", "-1")!!.toLong()
                apiDetailsService
            )
        } catch (e: IllegalArgumentException) {
            mustLoginToAddSource()
        }

        maybeGetDetailsFromIntentSharing(intent, binding.sourceUri, binding.nameInput)

        binding.saveBtn.setTextColor(appColors.colorAccent)

        binding.saveBtn.setOnClickListener {
            handleSaveSource(binding.tags, binding.nameInput.text.toString(), binding.sourceUri.text.toString(), api)
        }
    }

    override fun onResume() {
        super.onResume()
        val config = Config(this)

        if (config.baseUrl.isEmpty() || !config.baseUrl.isBaseUrlValid(this@AddSourceActivity)) {
            mustLoginToAddSource()
        } else {
            handleSpoutsSpinner(binding.spoutsSpinner, api, binding.progress, binding.formContainer)
        }
    }

    private fun handleSpoutsSpinner(
        spoutsSpinner: Spinner,
        api: SelfossApiImpl?,
        mProgress: ProgressBar,
        formContainer: ConstraintLayout
    ) {
        val spoutsKV = HashMap<String, String>()
        spoutsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (view != null) {
                    val spoutName = (view as TextView).text.toString()
                    mSpoutsValue = spoutsKV[spoutName]
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
                mSpoutsValue = null
            }
        }


        CoroutineScope(Dispatchers.Main).launch {
            var items = api!!.spouts()
            if (items != null) {

                val itemsStrings = items.map { it.value.name }
                for ((key, value) in items) {
                    spoutsKV[value.name] = key
                }

                mProgress.visibility = View.GONE
                formContainer.visibility = View.VISIBLE

                val spinnerArrayAdapter =
                    ArrayAdapter(
                        this@AddSourceActivity,
                        android.R.layout.simple_spinner_item,
                        itemsStrings
                    )
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spoutsSpinner.adapter = spinnerArrayAdapter
            } else {
                Toast.makeText(
                    this@AddSourceActivity,
                    R.string.cant_get_spouts,
                    Toast.LENGTH_SHORT
                ).show()
                mProgress.visibility = View.GONE
            }
        }
    }

    private fun maybeGetDetailsFromIntentSharing(
        intent: Intent,
        sourceUri: EditText,
        nameInput: EditText
    ) {
        if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            sourceUri.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            nameInput.setText(intent.getStringExtra(Intent.EXTRA_TITLE))
        }
    }

    private fun mustLoginToAddSource() {
        Toast.makeText(this, getString(R.string.addStringNoUrl), Toast.LENGTH_SHORT).show()
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun handleSaveSource(tags: EditText, title: String, url: String, api: SelfossApiImpl) {

        val sourceDetailsUnavailable =
            title.isEmpty() || url.isEmpty() || mSpoutsValue == null || mSpoutsValue!!.isEmpty()

        when {
            sourceDetailsUnavailable -> {
                Toast.makeText(this, R.string.form_not_complete, Toast.LENGTH_SHORT).show()
            }
            else -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val response: SelfossModel.SuccessResponse? = api.createSourceForVersion(
                            title,
                            url,
                            mSpoutsValue!!,
                            tags.text.toString(),
                            "",
                            PreferenceManager.getDefaultSharedPreferences(this@AddSourceActivity).getInt("apiVersionMajor", 0)
                        )
                    if (response != null) {
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddSourceActivity,
                            R.string.cant_create_source,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
