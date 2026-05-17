package com.github.panpf.zoomimage.sample

import android.content.Intent
import android.os.Bundle
import com.github.panpf.zoomimage.sample.ui.ComposeMainActivity
import com.github.panpf.zoomimage.sample.ui.ViewMainActivity
import com.github.panpf.zoomimage.sample.ui.base.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appSettings.composePage.value) {
            startActivity(Intent(this, ComposeMainActivity::class.java))
        } else {
            startActivity(Intent(this, ViewMainActivity::class.java))
        }
        finish()
    }
}