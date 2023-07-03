package com.github.panpf.zoomimage

import android.util.Log
import com.github.panpf.zoomimage.Logger.Level.DEBUG
import com.github.panpf.zoomimage.Logger.Level.ERROR
import com.github.panpf.zoomimage.Logger.Level.INFO
import com.github.panpf.zoomimage.Logger.Level.VERBOSE
import com.github.panpf.zoomimage.Logger.Level.WARNING

class Logger {

    companion object {
        const val TAG = "ZoomImage"
    }

    var proxy: Proxy = LogProxy()

    var level: Level = INFO
        set(value) {
            val oldLevel = field
            field = value
            val newLevel = value.name
            Log.w(TAG, "Logger. setLevel. $oldLevel -> $newLevel")
        }

    fun isLoggable(level: Level): Boolean {
        return level >= this.level
    }


    fun v(module: String, lazyMessage: () -> String) {
        if (isLoggable(VERBOSE)) {
            proxy.v(TAG, joinModuleAndMsg(module, lazyMessage()), null)
        }
    }

    fun v(module: String, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(VERBOSE)) {
            proxy.v(TAG, joinModuleAndMsg(module, lazyMessage()), throwable)
        }
    }


    fun d(module: String, lazyMessage: () -> String) {
        if (isLoggable(DEBUG)) {
            proxy.d(TAG, joinModuleAndMsg(module, lazyMessage()), null)
        }
    }

    fun d(module: String, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(DEBUG)) {
            proxy.d(TAG, joinModuleAndMsg(module, lazyMessage()), throwable)
        }
    }


    fun i(module: String, lazyMessage: () -> String) {
        if (isLoggable(INFO)) {
            proxy.i(TAG, joinModuleAndMsg(module, lazyMessage()), null)
        }
    }

    fun i(module: String, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(INFO)) {
            proxy.i(TAG, joinModuleAndMsg(module, lazyMessage()), throwable)
        }
    }


    fun w(module: String, msg: String) {
        if (isLoggable(WARNING)) {
            proxy.w(TAG, joinModuleAndMsg(module, msg), null)
        }
    }

    fun w(module: String, throwable: Throwable?, msg: String) {
        if (isLoggable(WARNING)) {
            proxy.w(TAG, joinModuleAndMsg(module, msg), throwable)
        }
    }

    fun w(module: String, lazyMessage: () -> String) {
        if (isLoggable(WARNING)) {
            proxy.w(TAG, joinModuleAndMsg(module, lazyMessage()), null)
        }
    }

    fun w(module: String, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(WARNING)) {
            proxy.w(TAG, joinModuleAndMsg(module, lazyMessage()), throwable)
        }
    }


    fun e(module: String, msg: String) {
        if (isLoggable(ERROR)) {
            proxy.e(TAG, joinModuleAndMsg(module, msg), null)
        }
    }

    fun e(module: String, throwable: Throwable?, msg: String) {
        if (isLoggable(ERROR)) {
            proxy.e(TAG, joinModuleAndMsg(module, msg), throwable)
        }
    }

    fun e(module: String, lazyMessage: () -> String) {
        if (isLoggable(ERROR)) {
            proxy.e(TAG, joinModuleAndMsg(module, lazyMessage()), null)
        }
    }

    fun e(module: String, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(ERROR)) {
            proxy.e(TAG, joinModuleAndMsg(module, lazyMessage()), throwable)
        }
    }


    fun flush() {
        proxy.flush()
    }

    private fun joinModuleAndMsg(module: String?, msg: String): String {
        val threadName = Thread.currentThread().name.let {
            // kotlin coroutine thread name 'DefaultDispatcher-worker-1' change to 'worker1'
            if (it.startsWith("DefaultDispatcher-worker-")) {
                it.replace("DefaultDispatcher-worker-", "work")
            } else {
                it
            }
        }
        return if (module?.isNotEmpty() == true) {
            "$threadName - $module. $msg"
        } else {
            "$threadName - $msg"
        }
    }

    override fun toString(): String = "Logger(level=$level,proxy=$proxy)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Logger
        if (level != other.level) return false
        if (proxy != other.proxy) return false
        return true
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + proxy.hashCode()
        return result
    }


    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE,
    }

    interface Proxy {
        fun v(tag: String, msg: String, tr: Throwable?)
        fun d(tag: String, msg: String, tr: Throwable?)
        fun i(tag: String, msg: String, tr: Throwable?)
        fun w(tag: String, msg: String, tr: Throwable?)
        fun e(tag: String, msg: String, tr: Throwable?)
        fun flush()
    }

    class LogProxy : Proxy {
        override fun v(tag: String, msg: String, tr: Throwable?) {
            Log.v(tag, msg, tr)
        }

        override fun d(tag: String, msg: String, tr: Throwable?) {
            Log.d(tag, msg, tr)
        }

        override fun i(tag: String, msg: String, tr: Throwable?) {
            Log.i(tag, msg, tr)
        }

        override fun w(tag: String, msg: String, tr: Throwable?) {
            Log.w(tag, msg, tr)
        }

        override fun e(tag: String, msg: String, tr: Throwable?) {
            Log.e(tag, msg, tr)
        }

        override fun flush() {

        }

        override fun toString(): String = "LogProxy"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}