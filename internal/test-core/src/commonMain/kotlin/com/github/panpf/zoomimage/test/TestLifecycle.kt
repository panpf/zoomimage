package com.github.panpf.zoomimage.test

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class TestLifecycleOwner(initialState: State = State.RESUMED) : LifecycleOwner {
    private val _lifecycle = TestLifecycle(initialState)
    override val lifecycle: Lifecycle
        get() = _lifecycle
}

class TestLifecycle(val initialState: State = State.RESUMED) : Lifecycle() {

    val owner = object : LifecycleOwner {
        override val lifecycle get() = this@TestLifecycle
    }

    override val currentState: State
        get() = initialState

    override fun addObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
        // Call the lifecycle methods in order and do not hold a reference to the observer.
        if (initialState >= State.CREATED) {
            observer.onStateChanged(owner, Event.ON_CREATE)
        }
        if (initialState >= State.STARTED) {
            observer.onStateChanged(owner, Event.ON_START)
        }
        if (initialState >= State.RESUMED) {
            observer.onStateChanged(owner, Event.ON_RESUME)
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
    }

    override fun toString() = "TestLifecycle"
}