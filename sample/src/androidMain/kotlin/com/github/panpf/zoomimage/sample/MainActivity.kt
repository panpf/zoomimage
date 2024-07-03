package com.github.panpf.zoomimage.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.panpf.zoomimage.sample.ui.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, MainFragment())
            .commit()
    }
}