package com.github.panpf.zoomimage.test

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import kotlin.reflect.KClass

suspend fun <T : Activity> KClass<T>.suspendLaunchActivityWithUse(action: suspend (ActivityScenario<T>) -> Unit) {
    ActivityScenario.launch(this.java).use { scenario ->
        action(scenario)
    }
}