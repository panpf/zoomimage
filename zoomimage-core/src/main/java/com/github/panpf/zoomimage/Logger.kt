package com.github.panpf.zoomimage

import android.util.Log

class Logger(
    val tag: String,
    val module: String? = null,
    val showThreadName: Boolean = false,
    private val rootLogger: Logger? = null // In order for all derived loggers to use levels and pipelines from rootLogger
) {

    private val threadNameLocal by lazy { ThreadLocal<String>() }

    var level: Int = rootLogger?.level ?: INFO
        get() {
            return rootLogger?.level ?: field
        }
        set(value) {
            val rootLogger = rootLogger
            if (rootLogger != null) {
                rootLogger.level = value
                if (field != value) {
                    field = value
                }
            } else if (field != value) {
                val oldLevel = field
                field = value
                Log.w(tag, "Logger. setLevel. ${levelName(oldLevel)} -> ${levelName(value)}")
            }
        }
    var pipeline: Pipeline = rootLogger?.pipeline ?: AndroidLogPipeline()
        get() {
            return rootLogger?.pipeline ?: field
        }
        set(value) {
            val rootLogger = rootLogger
            if (rootLogger != null) {
                rootLogger.pipeline = value
                if (field != value) {
                    field = value
                }
            } else if (field != value) {
                val oldPipeline = field
                oldPipeline.flush()
                field = value
                Log.w(tag, "Logger. setPipeline. $oldPipeline -> $value")
            }
        }

    fun newLogger(
        module: String? = this.module,
        showThreadName: Boolean = this.showThreadName
    ): Logger {
        return Logger(
            tag = tag,
            module = module,
            showThreadName = showThreadName,
            rootLogger = rootLogger ?: this
        )
    }


    fun v(msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= VERBOSE) {
            logger.pipeline.log(VERBOSE, tag, assembleMessage(msg), null)
        }
    }

    fun v(lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (this.level >= VERBOSE) {
            logger.pipeline.log(VERBOSE, tag, assembleMessage(lazyMessage()), null)
        }
    }

    fun v(throwable: Throwable?, msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= VERBOSE) {
            logger.pipeline.log(VERBOSE, tag, assembleMessage(msg), throwable)
        }
    }

    fun v(throwable: Throwable?, lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= VERBOSE) {
            logger.pipeline.log(VERBOSE, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    fun d(msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= DEBUG) {
            logger.pipeline.log(DEBUG, tag, assembleMessage(msg), null)
        }
    }

    fun d(lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= DEBUG) {
            logger.pipeline.log(DEBUG, tag, assembleMessage(lazyMessage()), null)
        }
    }

    fun d(throwable: Throwable?, msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= DEBUG) {
            logger.pipeline.log(DEBUG, tag, assembleMessage(msg), throwable)
        }
    }

    fun d(throwable: Throwable?, lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= DEBUG) {
            logger.pipeline.log(DEBUG, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    fun i(msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= INFO) {
            logger.pipeline.log(INFO, tag, assembleMessage(msg), null)
        }
    }

    fun i(lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= INFO) {
            logger.pipeline.log(INFO, tag, assembleMessage(lazyMessage()), null)
        }
    }

    fun i(throwable: Throwable?, msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= INFO) {
            logger.pipeline.log(INFO, tag, assembleMessage(msg), throwable)
        }
    }

    fun i(throwable: Throwable?, lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= INFO) {
            logger.pipeline.log(INFO, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    fun w(msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= WARN) {
            logger.pipeline.log(WARN, tag, assembleMessage(msg), null)
        }
    }

    fun w(lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= WARN) {
            logger.pipeline.log(WARN, tag, assembleMessage(lazyMessage()), null)
        }
    }

    fun w(throwable: Throwable?, msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= WARN) {
            logger.pipeline.log(WARN, tag, assembleMessage(msg), throwable)
        }
    }

    fun w(throwable: Throwable?, lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= WARN) {
            logger.pipeline.log(WARN, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    fun e(msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= ERROR) {
            logger.pipeline.log(ERROR, tag, assembleMessage(msg), null)
        }
    }

    fun e(lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= ERROR) {
            logger.pipeline.log(ERROR, tag, assembleMessage(lazyMessage()), null)
        }
    }

    fun e(throwable: Throwable?, msg: String) {
        val logger = rootLogger ?: this
        if (logger.level >= ERROR) {
            logger.pipeline.log(ERROR, tag, assembleMessage(msg), throwable)
        }
    }

    fun e(throwable: Throwable?, lazyMessage: () -> String) {
        val logger = rootLogger ?: this
        if (logger.level >= ERROR) {
            logger.pipeline.log(ERROR, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    fun flush() {
        val logger = rootLogger ?: this
        logger.pipeline.flush()
    }

    private fun assembleMessage(msg: String): String {
        return if (showThreadName) {
            val threadName = getThreadName()
            if (module?.isNotEmpty() == true) {
                "$threadName - $module. $msg"
            } else {
                "$threadName - $msg"
            }
        } else {
            if (module?.isNotEmpty() == true) {
                "$module. $msg"
            } else {
                msg
            }
        }
    }

    private fun getThreadName(): String? {
        val threadName = threadNameLocal.get()
        return if (threadName == null) {
            val name = Thread.currentThread().name.let {
                // kotlin coroutine thread name 'DefaultDispatcher-worker-1' change to 'worker1'
                if (it.startsWith("DefaultDispatcher-worker-")) {
                    "worker${it.substring("DefaultDispatcher-worker-".length)}"
                } else if (it.startsWith("Thread-")) {
                    "Thread${it.substring("Thread-".length)}"
                } else {
                    it
                }
            }
            threadNameLocal.set(name)
            name
        } else {
            threadName
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Logger

        if (tag != other.tag) return false
        if (module != other.module) return false
        if (pipeline != other.pipeline) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + (module?.hashCode() ?: 0)
        result = 31 * result + pipeline.hashCode()
        return result
    }

    override fun toString(): String {
        return "Logger2(tag='$tag', module=$pipeline=$pipeline, level=$level)"
    }

    companion object {
        /**
         * Priority constant for the println method; use Log.v.
         */
        const val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        const val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        const val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        const val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        const val ERROR = 6

        /**
         * Priority constant for the println method.
         */
        const val ASSERT = 7

        fun levelName(level: Int): String = when (level) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }

        fun level(levelName: String): Int = when (levelName) {
            "VERBOSE" -> VERBOSE
            "DEBUG" -> DEBUG
            "INFO" -> INFO
            "WARN" -> WARN
            "ERROR" -> ERROR
            "ASSERT" -> ASSERT
            else -> -1
        }
    }

    interface Pipeline {
        fun log(level: Int, tag: String, msg: String, tr: Throwable?)
        fun flush()
    }

    class AndroidLogPipeline : Pipeline {
        override fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
            when (level) {
                VERBOSE -> Log.v(tag, msg, tr)
                DEBUG -> Log.d(tag, msg, tr)
                INFO -> Log.i(tag, msg, tr)
                WARN -> Log.w(tag, msg, tr)
                ERROR -> Log.e(tag, msg, tr)
                ASSERT -> Log.wtf(tag, msg, tr)
            }
        }

        override fun flush() {
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }

        override fun toString(): String {
            return "AndroidLogPipeline"
        }
    }
}