package com.github.panpf.zoom.sample

import androidx.multidex.MultiDexApplication
import com.tencent.mmkv.MMKV

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}