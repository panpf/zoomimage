package com.github.panpf.zoomimage.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun TestLifecycle(initialState: State = State.RESUMED, block: @Composable () -> Unit) {
    val lifecycleOwner = remember { TestLifecycleOwner(initialState) }
    CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        block()
    }
}