package com.github.panpf.zoomimage.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun TestLifecycle(block: @Composable () -> Unit) {
    val lifecycleOwner = remember { TestLifecycleOwner() }
    CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        block()
    }
}

class TestLifecycleOwner : LifecycleOwner {
    private val _lifecycle = TestLifecycle()
    override val lifecycle: Lifecycle
        get() = _lifecycle
}

class TestLifecycle : Lifecycle() {

    val owner = object : LifecycleOwner {
        override val lifecycle get() = this@TestLifecycle
    }

    override val currentState: State
        get() = State.RESUMED

    override fun addObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
        // Call the lifecycle methods in order and do not hold a reference to the observer.
        observer.onStateChanged(owner, Event.ON_CREATE)
        observer.onStateChanged(owner, Event.ON_START)
        observer.onStateChanged(owner, Event.ON_RESUME)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
    }

    override fun toString() = "TestLifecycle"
}