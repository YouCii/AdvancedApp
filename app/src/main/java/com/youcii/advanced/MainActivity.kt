package com.youcii.advanced

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.youcii.advanced.databinding.ActivityMainBinding

import android.content.Intent
import android.provider.Settings

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        activityMainBinding.content.tvSet.setOnClickListener {
            if (Utils.isZenMode(this)) {
                Utils.setZenMode(this, false)
            } else {
                Utils.setZenMode(this, true)
            }
            activityMainBinding.content.tvSet.text = "isZenMode: ${Utils.isZenMode(this)}"
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.isZenModeGranted(this)) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, 0)
        }

        activityMainBinding.content.tvState.text = "isZenModeGranted: ${Utils.isZenModeGranted(this)}"
        activityMainBinding.content.tvSet.text = "isZenMode: ${Utils.isZenMode(this)}"
    }
}