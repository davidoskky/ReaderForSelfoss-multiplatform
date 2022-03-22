package bou.amine.apps.readerforselfossv2.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bou.amine.apps.readerforselfossv2.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = Intent(this, LoginActivity::class.java)

        startActivity(intent)
        finish()
    }
}
