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
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossApi
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Spout
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SuccessResponse
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.themes.Toppings
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.isBaseUrlValid
import com.ftinc.scoop.Scoop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import bou.amine.apps.readerforselfossv2.android.databinding.ActivityAddSourceBinding


class AddSourceActivity : AppCompatActivity() {

    private var mSpoutsValue: String? = null
    private lateinit var api: SelfossApi

    private lateinit var appColors: AppColors
    private lateinit var binding: ActivityAddSourceBinding

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
            api = SelfossApi(
                this,
                this@AddSourceActivity,
                settings.getBoolean("isSelfSignedCert", false),
                prefs.getString("api_timeout", "-1")!!.toLong()
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
        api: SelfossApi?,
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

        var items: Map<String, Spout>
        api!!.spouts().enqueue(object : Callback<Map<String, Spout>> {
            override fun onResponse(
                call: Call<Map<String, Spout>>,
                response: Response<Map<String, Spout>>
            ) {
                if (response.body() != null) {
                    items = response.body()!!

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
                    handleProblemWithSpouts()
                }
            }

            override fun onFailure(call: Call<Map<String, Spout>>, t: Throwable) {
                handleProblemWithSpouts()
            }

            private fun handleProblemWithSpouts() {
                Toast.makeText(
                    this@AddSourceActivity,
                    R.string.cant_get_spouts,
                    Toast.LENGTH_SHORT
                ).show()
                mProgress.visibility = View.GONE
            }
        })
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

    private fun handleSaveSource(tags: EditText, title: String, url: String, api: SelfossApi) {

        val sourceDetailsUnavailable =
            title.isEmpty() || url.isEmpty() || mSpoutsValue == null || mSpoutsValue!!.isEmpty()

        when {
            sourceDetailsUnavailable -> {
                Toast.makeText(this, R.string.form_not_complete, Toast.LENGTH_SHORT).show()
            }
            PreferenceManager.getDefaultSharedPreferences(this).getInt("apiVersionMajor", 0) > 1 -> {
                val tagList = tags.text.toString().split(",").map { it.trim() }
                api.createSourceApi2(
                    title,
                    url,
                    mSpoutsValue!!,
                    tagList,
                    ""
                ).enqueue(object : Callback<SuccessResponse> {
                    override fun onResponse(
                        call: Call<SuccessResponse>,
                        response: Response<SuccessResponse>
                    ) {
                        if (response.body() != null && response.body()!!.isSuccess) {
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddSourceActivity,
                                R.string.cant_create_source,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                        Toast.makeText(
                            this@AddSourceActivity,
                            R.string.cant_create_source,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            else -> {
                api.createSource(
                    title,
                    url,
                    mSpoutsValue!!,
                    tags.text.toString(),
                    ""
                ).enqueue(object : Callback<SuccessResponse> {
                    override fun onResponse(
                        call: Call<SuccessResponse>,
                        response: Response<SuccessResponse>
                    ) {
                        if (response.body() != null && response.body()!!.isSuccess) {
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddSourceActivity,
                                R.string.cant_create_source,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                        Toast.makeText(
                            this@AddSourceActivity,
                            R.string.cant_create_source,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }
}
