package com.github.panpf.zoomimage.sample.util

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.panpf.zoomimage.sample.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectWithLifecycle(lifecycleOwner: LifecycleOwner, collector: FlowCollector<T>) {
    lifecycleOwner.lifecycleScope.launch {
        collect(collector)
    }
}

fun <T> Flow<T>.repeatCollectWithLifecycle(
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

val View.lifecycleOwner: LifecycleOwner
    get() {
        synchronized(this) {
            return (getTag(R.id.tagId_viewLifecycle) as ViewLifecycleOwner?)
                ?: ViewLifecycleOwner(this).apply {
                    setTag(R.id.tagId_viewLifecycle, this)
                }
        }
    }

class ViewLifecycleOwner(view: View) : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun onViewDetachedFromWindow(v: View) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                // The LifecycleRegistry that has been destroyed can no longer be used, and a new one must be created
                view.setTag(R.id.tagId_viewLifecycle, null)
            }
        })
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}