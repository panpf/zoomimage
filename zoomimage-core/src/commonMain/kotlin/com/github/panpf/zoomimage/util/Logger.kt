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

package com.github.panpf.zoomimage.util

expect fun createLogPipeline(): Logger.Pipeline

/**
 * Used to print log
 *
 * @see [com.github.panpf.zoomimage.core.test.util.LoggerTest]
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
     * Initial Level
     */
    level: Level? = null,

    /**
     * Specifies the output pipeline of the log
     */
    pipeline: Pipeline? = null,

    /**
     * The root logger, in order for all derived loggers to use the same level and pipeline
     */
    private val rootLogger: Logger? = null,
) {

    /**
     * The level of the log. The level of the root logger will be modified directly
     */
    var level: Level = rootLogger?.level ?: level ?: Level.Info
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
                    level = Level.Warn,
                    tag = tag,
                    msg = "Logger@${this.toHexString()}. setLevel. $oldLevel -> $value",
                    tr = null
                )
            }
        }

    /**
     * The pipeline of the log. The pipeline of the root logger will be modified directly
     */
    var pipeline: Pipeline = pipeline ?: rootLogger?.pipeline ?: createLogPipeline()
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
                oldPipeline.log(
                    level = Level.Warn,
                    tag = tag,
                    msg = "Logger@${this.toHexString()}. setPipeline. $oldPipeline -> $value",
                    tr = null
                )
                oldPipeline.flush()
                field = value
            }
        }

    /**
     * To create a new logger based on the current logger, you can only modify the values of module and showThreadName
     */
    fun newLogger(
        module: String? = this.module,
    ): Logger = Logger(
        tag = tag,
        module = module,
        rootLogger = rootLogger ?: this
    )


    /**
     * Print a log with the VERBOSE level
     */
    fun v(msg: String) {
        if (isLoggable(Level.Verbose)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Verbose, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(lazyMessage: () -> String) {
        if (isLoggable(Level.Verbose)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Verbose, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(throwable: Throwable?, msg: String) {
        if (isLoggable(Level.Verbose)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Verbose, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(Level.Verbose)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Verbose, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the DEBUG level
     */
    fun d(msg: String) {
        if (isLoggable(Level.Debug)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Debug, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(lazyMessage: () -> String) {
        if (isLoggable(Level.Debug)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Debug, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(throwable: Throwable?, msg: String) {
        if (isLoggable(Level.Debug)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Debug, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(Level.Debug)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Debug, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the INFO level
     */
    fun i(msg: String) {
        if (isLoggable(Level.Info)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Info, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(lazyMessage: () -> String) {
        if (isLoggable(Level.Info)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Info, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(throwable: Throwable?, msg: String) {
        if (isLoggable(Level.Info)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Info, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(Level.Info)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Info, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the WARN level
     */
    fun w(msg: String) {
        if (isLoggable(Level.Warn)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Warn, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(lazyMessage: () -> String) {
        if (isLoggable(Level.Warn)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Warn, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(throwable: Throwable?, msg: String) {
        if (isLoggable(Level.Warn)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Warn, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the WARN level
     */
    fun w(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(Level.Warn)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Warn, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the ERROR level
     */
    fun e(msg: String) {
        if (isLoggable(Level.Error)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Error, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(lazyMessage: () -> String) {
        if (isLoggable(Level.Error)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Error, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(throwable: Throwable?, msg: String) {
        if (isLoggable(Level.Error)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Error, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(Level.Error)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(Level.Error, tag, assembleMessage(lazyMessage()), throwable)
        }
    }


    /**
     * Print a log with the specified level
     */
    fun log(level: Level, msg: String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(msg), null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, lazyMessage: () -> String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(lazyMessage()), null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, throwable: Throwable?, msg: String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(msg), throwable)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, throwable: Throwable?, lazyMessage: () -> String) {
        if (isLoggable(level)) {
            val pipeline = (rootLogger ?: this).pipeline
            pipeline.log(level, tag, assembleMessage(lazyMessage()), throwable)
        }
    }

    fun isLoggable(level: Level): Boolean {
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

    private fun assembleMessage(msg: String): String = buildString {
        if (module?.isNotEmpty() == true) {
            append(module)
            if (isNotEmpty()) append(". ")
        }

        append(msg)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Logger) return false
        if (tag != other.tag) return false
        if (module != other.module) return false
        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + (module?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Logger(tag='$tag', module=$module, level=$level, pipeline=$pipeline)"
    }

    enum class Level {
        /**
         * Priority constant for the println method; use Log.v.
         */
        Verbose,

        /**
         * Priority constant for the println method; use Log.d.
         */
        Debug,

        /**
         * Priority constant for the println method; use Log.i.
         */
        Info,

        /**
         * Priority constant for the println method; use Log.w.
         */
        Warn,

        /**
         * Priority constant for the println method; use Log.e.
         */
        Error,

        /**
         * Priority constant for the println method.
         */
        Assert,
    }

    /**
     * The pipeline of the log
     */
    interface Pipeline {
        fun log(level: Level, tag: String, msg: String, tr: Throwable?)
        fun flush()
    }
}