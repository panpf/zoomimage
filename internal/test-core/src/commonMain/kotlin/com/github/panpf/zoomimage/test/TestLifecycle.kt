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

class TestLifecycle(initialState: State = State.RESUMED) : Lifecycle() {

    private val observers = mutableListOf<LifecycleEventObserver>()

    val owner = object : LifecycleOwner {
        override val lifecycle get() = this@TestLifecycle
    }

    override var currentState: State = initialState
        set(value) {
            if (field != value) {
                val event = when (value) {
                    State.INITIALIZED -> Event.ON_CREATE
                    State.CREATED -> Event.ON_CREATE
                    State.STARTED -> Event.ON_START
                    State.RESUMED -> Event.ON_RESUME
                    State.DESTROYED -> Event.ON_DESTROY
                    else -> throw IllegalArgumentException("Unknown state: $value")
                }
                field = value
                observers.forEach {
                    it.onStateChanged(owner, event)
                }
            }
        }

    override fun addObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
        observers.add(observer)
        // Call the lifecycle methods in order and do not hold a reference to the observer.
        if (currentState >= State.CREATED) {
            observer.onStateChanged(owner, Event.ON_CREATE)
        }
        if (currentState >= State.STARTED) {
            observer.onStateChanged(owner, Event.ON_START)
        }
        if (currentState >= State.RESUMED) {
            observer.onStateChanged(owner, Event.ON_RESUME)
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        require(observer is LifecycleEventObserver) {
            "Observer must implement LifecycleEventObserver"
        }
        observers.remove(observer)
    }

    override fun toString() = "TestLifecycle"
}