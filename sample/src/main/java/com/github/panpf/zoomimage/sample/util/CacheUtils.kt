package com.github.panpf.zoomimage.sample.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import kotlin.math.roundToLong


internal fun Context.getAppMemoryClassBytes(): Int {
    val activityManager =
        getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val isLargeHeap =
        (applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP) != 0
    val memoryClass = when {
        activityManager != null && isLargeHeap -> activityManager.largeMemoryClass
        activityManager != null && !isLargeHeap -> activityManager.memoryClass
        else -> 16
    }
    return memoryClass * 1024 * 1024
}

internal fun Context.isLowRamDevice(): Boolean {
    val activityManager =
        getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    return VERSION.SDK_INT < VERSION_CODES.KITKAT || activityManager?.isLowRamDevice == true
}

internal fun Context.getMaxAvailableMemoryCacheBytes(): Long {
    val appMemoryClassBytes = getAppMemoryClassBytes()
    val lowRamDevice = isLowRamDevice()
    return ((if (lowRamDevice) 0.4f else 0.5f) * appMemoryClassBytes).roundToLong()
}