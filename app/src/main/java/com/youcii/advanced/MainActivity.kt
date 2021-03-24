package com.youcii.advanced

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.youcii.loadprovider.BaseLoader
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val service: ServiceLoader<BaseLoader> = ServiceLoader.load(BaseLoader::class.java)
        service.forEach {
            Toast.makeText(this, it.init(), Toast.LENGTH_SHORT).show()
        }
    }

}