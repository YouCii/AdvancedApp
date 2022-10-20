package com.youcii.advanced

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.snackbar.Snackbar
import com.youcii.advanced.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private val openDrawable by lazy {
        AppCompatResources.getDrawable(this, R.drawable.live_audio_open) as LayerDrawable
    }
    private val volumeDrawable by lazy {
        openDrawable.getDrawable(1) as ClipDrawable
    }
    private var level = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)

        activityMainBinding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        activityMainBinding.content.tvContent.text = "哈哈哈"

        activityMainBinding.content.viewBack.background = openDrawable
        activityMainBinding.content.tvContent.setOnClickListener {
            level += 0.1
            volumeDrawable.level = ((12 * level + 11) / 28 * 10000).toInt()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}