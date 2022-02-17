package com.skyinu.wardhere

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.gson.JsonSyntaxException
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.io.File

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    TestKt().test()
    fab.setOnClickListener {
      val intent = Intent(this, SecondActivity::class.java)
      startActivity(intent)
    }
    JsonSyntaxException("oh gson can't be found")
  }

  private fun catchTest(): String? {
    try {
      val `object`: Any? = null
      return `object`!!.toString()
    } catch (ex: Exception) {

    }

    return null
  }

  private fun catchTest2(): Boolean {
    try {
      val file: File? = null
      return file!!.delete()
    } catch (ex: NullPointerException) {
      try {
        val test = "3aaa1"
        return Integer.parseInt(test) == 1
      } catch (ignore: Exception) {

      }

    } catch (ex: Exception) {

    }

    return true
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId


    return if (id == R.id.action_settings) {
      true
    } else super.onOptionsItemSelected(item)

  }

  // 在 Activity 类中
  fun doSomething() {
    // 在示例中启动了 10 个协程，且每个都工作了不同的时长
    repeat(10) { i ->
      launch {
        delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒等等不同的时间
        println("Coroutine $i is done")
      }
    }
  }
}
