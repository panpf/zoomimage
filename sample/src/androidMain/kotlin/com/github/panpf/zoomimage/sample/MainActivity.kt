package com.github.panpf.zoomimage.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Modifier
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.SketchZoomAsyncImage
import com.github.panpf.zoomimage.sample.ui.base.BaseActivity
import com.google.android.material.internal.EdgeToEdgeUtils

class MainActivity : BaseActivity() {

    @OptIn(ExperimentalFoundationApi::class)
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

//        supportFragmentManager
//            .beginTransaction()
//            .replace(android.R.id.content, MainFragment())
//            .commit()

        setContent {
            HorizontalPager(
                state = rememberPagerState {
                    ResourceImages.values.size
                },
                modifier = Modifier.fillMaxSize()
            ) {
                SketchZoomAsyncImage(
                    uri = ResourceImages.values[it].uri,
                    contentDescription = "",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}