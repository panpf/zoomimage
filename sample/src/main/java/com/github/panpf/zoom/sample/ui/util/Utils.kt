package com.github.panpf.zoom.sample.ui.util

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes


fun Context.newCoilResourceUri(@DrawableRes id: Int): Uri {
    return Uri.parse("android.resource://${packageName}/${id}")
}

fun newCoilAssetUri(@Suppress("SameParameterValue") path: String): Uri {
    return Uri.parse("file://filled/android_asset/$path")
}