package com.skyinu.wardhere

import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestKt {
  fun test() {
   GlobalScope.launch {
       delay(1000)
   }
  }
}