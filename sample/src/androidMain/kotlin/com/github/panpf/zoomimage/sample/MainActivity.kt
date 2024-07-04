package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.github.panpf.zoomimage.sample.ui.MainFragment
import com.github.panpf.zoomimage.sample.ui.base.BaseActivity
import com.google.android.material.internal.EdgeToEdgeUtils

class MainActivity : BaseActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EdgeToEdgeUtils.applyEdgeToEdge(/* window = */ window,/* edgeToEdgeEnabled = */ true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#60000000")
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        }

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, MainFragment())
            .commit()
    }
}