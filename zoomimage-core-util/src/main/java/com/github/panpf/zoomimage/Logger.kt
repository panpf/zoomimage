/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage

import androidx.annotation.IntDef
import java.io.ByteArrayOutputStream
import java.io.PrintStream


/**
 * Used to print log
 *
 * @see [com.github.panpf.zoomimage.core.test.LoggerTest]
 */
class Logger(
    /**
     * The tag of the log
     */
    val tag: String,

    /**
     * The module name of the log
     */
    val module: String? = null,

    /**
     * Whether to show the name of the current thread in the log
     */
    val showThreadName: Boolean = false,

    /**
     * Specifies the output pipeline of the log
     */
    pipeline: Pipeline? = null,

    /**
     * The root logger, in order for all derived loggers to use the same level and pipeline
     */
    private val rootLogger: Logger? = null,
) {

    private val threadNameLocal by lazy { ThreadLocal<String>() }

    /**
     * The level of the log. The level of the root logger will be modified directly
     */
    var level: Int = rootLogger?.level ?: INFO
        get() = rootLogger?.level ?: field
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
                pipeline.log(
                    WARN,
                    tag,
                    "Logger. setLevel. ${levelName(oldLevel)} -> ${levelName(value)}",
                    null
                )
            }
        }

    /**
     * The pipeline of the log. The pipeline of the root logger will be modified directly
     */
    var pipeline: Pipeline = pipeline ?: rootLogger?.pipeline ?: LogPipeline()
        get() = rootLogger?.pipeline ?: field
        set(value) {
            val rootLogger = rootLogger
            if (rootLogger != null) {
                rootLogger.pipeline = value
                if (field != value) {
                    field = value
                }
            } else if (field != value) {
                val oldPipeline = field
                oldPipeline.log(WARN, tag, "Logger. setPipeline. $oldPipeline -> $value", null)
                oldPipeline.flush()
                field = value
            }
        }

    /**
     * To create a new logger based on the current logger, you can only modify the values of module and showThreadName
     */
    fun newLogger(
        module: String? = this.module,
        showThreadName: Boolean = this.showThreadName
    ): Logger = Logger(
        tag = tag,
        module = module,
        showThreadName = showThreadName,
        rootLogger = rootLogger ?: this
    )


    /**
     * Print a log with the VERBOSE level
     */
    fun v(msg: String) {
        if (isLoggable(VERBOSE)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(VERBOSE, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(lazyMessage: () -> String) {
        if (isLoggable(VERBOSE)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(VERBOSE, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(throwable: Throwable?, msg: String) {
        if (isLoggable(VERBOSE)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(VERBOSE, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(VERBOSE)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(VERBOSE, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the DEBUG level
     */
    fun d(msg: String) {
        if (isLoggable(DEBUG)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(DEBUG, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(lazyMessage: () -> String) {
        if (isLoggable(DEBUG)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(DEBUG, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(throwable: Throwable?, msg: String) {
        if (isLoggable(DEBUG)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(DEBUG, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(DEBUG)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(DEBUG, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the INFO level
     */
    fun i(msg: String) {
        if (isLoggable(INFO)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(INFO, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(lazyMessage: () -> String) {
        if (isLoggable(INFO)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(INFO, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(throwable: Throwable?, msg: String) {
        if (isLoggable(INFO)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(INFO, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(INFO)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(INFO, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the WARN level
     */
    fun w(msg: String) {
        if (isLoggable(WARN)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(WARN, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(lazyMessage: () -> String) {
        if (isLoggable(WARN)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(WARN, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(throwable: Throwable?, msg: String) {
        if (isLoggable(WARN)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(WARN, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(WARN)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(WARN, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the ERROR level
     */
    fun e(msg: String) {
        if (isLoggable(ERROR)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(ERROR, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(lazyMessage: () -> String) {
        if (isLoggable(ERROR)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(ERROR, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(throwable: Throwable?, msg: String) {
        if (isLoggable(ERROR)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(ERROR, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(ERROR)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(ERROR, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the specified level
     */
    fun log(@Level level: Int, msg: String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(@Level level: Int, lazyMessage: () -> String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(@Level level: Int, throwable: Throwable?, msg: String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(@Level level: Int, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(lazyMessage()), throwable)
        }
    }

    fun isLoggable(level: Int): Boolean {
        val logger = rootLogger ?: this
        return level >= logger.level
    }


    /**
     * Flush the log pipeline
     */
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
        } else if (module?.isNotEmpty() == true) {
            "$module. $msg"
        } else {
            msg
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
        if (showThreadName != other.showThreadName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + (module?.hashCode() ?: 0)
        result = 31 * result + showThreadName.hashCode()
        return result
    }

    override fun toString(): String {
        return "Logger(tag='$tag', module=$module, showThreadName=$showThreadName, level=$level, pipeline=$pipeline)"
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT)
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    annotation class Level

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

        /**
         * Get the name of the level
         */
        fun levelName(level: Int): String = when (level) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }

        /**
         * Get the level of the name
         */
        fun level(levelName: String): Int = when (levelName) {
            "VERBOSE" -> VERBOSE
            "DEBUG" -> DEBUG
            "INFO" -> INFO
            "WARN" -> WARN
            "ERROR" -> ERROR
            "ASSERT" -> ASSERT
            else -> throw IllegalArgumentException("Unknown level name: $levelName")
        }
    }

    /**
     * The pipeline of the log
     */
    interface Pipeline {
        fun log(level: Int, tag: String, msg: String, tr: Throwable?)
        fun flush()
    }


    /**
     * The pipeline of the log, which prints the log to the println
     */
    class LogPipeline : Pipeline {
        override fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
            if (tr != null) {
                val trString = stackTraceToString(tr)
                println("${levelName(level)}. $tag. $msg. $trString")
            } else {
                println("${levelName(level)}. $tag. $msg")
            }
        }

        private fun stackTraceToString(throwable: Throwable): String {
            val arrayOutputStream = ByteArrayOutputStream()
            val printWriter = PrintStream(arrayOutputStream)
            throwable.printStackTrace(printWriter)
            return String(arrayOutputStream.toByteArray())
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
            return "LogPipeline"
        }
    }
}