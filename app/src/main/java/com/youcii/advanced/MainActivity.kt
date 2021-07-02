package com.youcii.advanced

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import javassist.util.proxy.Proxy
import javassist.util.proxy.ProxyFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textview.text = MyApplication.aptContent
        textview.setOnClickListener {
            val routerClass1 = RouterClass1()
            val routerClass2 = RouterClass2()
        }

        val asd: String? = null
        textview.text = asd!!.trim()
    }

    private fun showToast() {
        Toast.makeText(this, "invoke from asm", Toast.LENGTH_LONG).show()
    }

    /**
     * 对于android不适用Javassist.ProxyFactory
     */
    private fun proxyFactory(): String {
        val proxyFactory = ProxyFactory().apply {
            superclass = ProxyTest::class.java
            setFilter { it.name == "test" }
        }
        val proxyClass = proxyFactory.createClass() // throw NullPointException
        (proxyClass as Proxy).setHandler { self, thisMethod, _, args ->
            val result = thisMethod.invoke(self, args) as String
            return@setHandler result + result
        }

        val proxyTest = proxyClass.newInstance() as ProxyTest
        return proxyTest.test()
    }

}