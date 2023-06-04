package com.github.panpf.zoomimage.sample.util

import android.net.Uri
import androidx.core.net.toUri

fun sketchUri2CoilUri(@Suppress("SameParameterValue") uri: String): Uri {
    return if (uri.startsWith("asset://")) {
        Uri.parse("file://filled/android_asset/${uri.substring("asset://".length)}")
    } else {
        uri.toUri()
    }
}