package com.github.panpf.zoomimage.sample.ui

import android.content.Intent
import android.os.Bundle
import com.github.panpf.zoomimage.sample.databinding.ActivityNavHostBinding
import com.github.panpf.zoomimage.sample.ui.base.BaseBindingActivity
import com.github.panpf.zoomimage.sample.util.collectWithLifecycle
import com.github.panpf.zoomimage.sample.util.ignoreFirst

class ViewMainActivity : BaseBindingActivity<ActivityNavHostBinding>() {

    override fun onCreate(binding: ActivityNavHostBinding, savedInstanceState: Bundle?) {
        appSettings.composePage.ignoreFirst().collectWithLifecycle(this) {
            startActivity(Intent(this, ComposeMainActivity::class.java))
            this@ViewMainActivity.finish()
        }
    }
}