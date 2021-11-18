package com.youcii.advanced

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        entrance_lesson_list.layoutManager = LinearLayoutManager(this)
        entrance_lesson_list.adapter = object: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(TextView(this@MainActivity)) {
                }
            }

            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
                (viewHolder.itemView as TextView).text = position.toString()
            }

            override fun getItemCount(): Int {
                return 100
            }
        }
    }

}

val Number.dp: Int
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()
