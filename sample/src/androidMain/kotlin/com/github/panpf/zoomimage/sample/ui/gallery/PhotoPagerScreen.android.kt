package com.github.panpf.zoomimage.sample.ui.gallery

import android.content.Context
import android.os.Build
import com.github.panpf.tools4a.display.ktx.getStatusBarHeight


actual fun getTopMargin(context: Context): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        context.getStatusBarHeight()
    } else {
        0
    }
}