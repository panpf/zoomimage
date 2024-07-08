package com.github.panpf.zoomimage.sample.ui.gallery

import android.content.Context
import com.github.panpf.tools4a.display.ktx.getStatusBarHeight


actual fun getTopMargin(context: Context): Int = context.getStatusBarHeight()