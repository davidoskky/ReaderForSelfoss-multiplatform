package bou.amine.apps.readerforselfossv2.android

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import bou.amine.apps.readerforselfossv2.android.databinding.ActivityImageBinding
import bou.amine.apps.readerforselfossv2.android.fragments.ImageFragment

class ImageActivity : AppCompatActivity() {
    private lateinit var allImages : ArrayList<String>
    private var position : Int = 0

    private lateinit var binding: ActivityImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        allImages = intent.getStringArrayListExtra("allImages") as ArrayList<String>
        position = intent.getIntExtra("position", 0)

        binding.pager.adapter = ScreenSlidePagerAdapter(this)
        binding.pager.setCurrentItem(position, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = allImages.size

        override fun createFragment(position: Int): Fragment = ImageFragment.newInstance(allImages[position])
    }
}