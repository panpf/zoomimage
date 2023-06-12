package com.github.panpf.zoomimage.sample.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> Flow<T>.collect(lifecycleOwner: LifecycleOwner, collector: FlowCollector<T>) {
    lifecycleOwner.lifecycleScope.launch {
        collect(collector)
    }
}

fun <T> Flow<T>.repeatCollect(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State,
    collector: FlowCollector<T>
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect(collector)
        }
    }
}