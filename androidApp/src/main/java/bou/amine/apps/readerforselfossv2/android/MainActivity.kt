package bou.amine.apps.readerforselfossv2.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import bou.amine.apps.readerforselfossv2.Greeting
import android.widget.TextView
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.logging.Logger

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CoroutineScope(Dispatchers.IO).launch {
            val s = SelfossApi().getItems("unread", 300, 0).forEach { i -> println(i.getImages()) }
            Logger.getLogger(")").info(s.toString())
        }

        val tv: TextView = findViewById(R.id.text_view)
        tv.text = greet()
    }
}
