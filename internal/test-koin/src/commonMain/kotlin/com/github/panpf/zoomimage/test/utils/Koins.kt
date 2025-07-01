package com.github.panpf.zoomimage.test.utils

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object Koins {

    private var _application: KoinApplication? = null
    private val lock = SynchronizedObject()

    fun initial(module: Module) {
        if (_application != null) return
        synchronized(lock) {
            if (_application != null) return
            val newApplication = startKoin {
                modules(module)
            }
            _application = newApplication
        }
    }
}