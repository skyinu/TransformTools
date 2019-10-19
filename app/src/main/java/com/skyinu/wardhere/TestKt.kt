package com.skyinu.wardhere

import android.util.Log
import android.view.View

class TestKt {
  fun test() {
    val a = 0
    Log.e("TAG1", "a=$a")
    View(null).setOnClickListener {
      print("ddd")
    }
    when(a){
      0->{
        println("test")
      }
      1->{
        print("dddd")
      }
    }
  }
}