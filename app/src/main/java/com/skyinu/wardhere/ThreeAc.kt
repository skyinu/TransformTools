package com.skyinu.wardhere

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent

class ThreeAc : Activity(){
    private var byteArray = ByteArray(1)
    private var count  = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        byteArray = ByteArray(1024*1024*10*count++)
        Log.e("TAG1","deDEDE")

        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        byteArray = ByteArray(1024*1024*10*count++)
        Log.e("TAG1","deDEDE")
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        byteArray = ByteArray(1024*1024*10*count++)
        Log.e("TAG1","deDEDE")
        return super.dispatchKeyEvent(event)
    }
}