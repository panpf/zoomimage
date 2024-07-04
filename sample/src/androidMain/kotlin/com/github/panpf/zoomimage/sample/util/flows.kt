package com.github.panpf.zoomimage.sample.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectWithLifecycle(owner: LifecycleOwner, collector: FlowCollector<T>): Job {
    return owner.lifecycleScope.launch {
        collect(collector)
    }
}

fun <T> Flow<T>.repeatCollectWithLifecycle(
    owner: LifecycleOwner,
    state: Lifecycle.State,
    collector: FlowCollector<T>
): Job {
    return owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(state) {
            collect(collector)
        }
    }
}

fun <T> Flow<T>.repeatCollectWithLifecycle(
    lifecycle: Lifecycle,
    state: Lifecycle.State,
    collector: FlowCollector<T>
): Job {
    return lifecycle.coroutineScope.launch {
        lifecycle.repeatOnLifecycle(state) {
            collect(collector)
        }
    }
}