package com.github.panpf.zoomimage.sample

import android.view.View
import androidx.fragment.app.Fragment

val Fragment.appSettings: AppSettings
    get() = this.requireContext().appSettings
val View.appSettings: AppSettings
    get() = this.context.appSettings

actual fun isDebugMode(): Boolean = BuildConfig.DEBUG