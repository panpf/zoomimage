/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

/**
 * Get the platform default log pipeline
 */
expect fun defaultLogPipeline(): Logger.Pipeline

/**
 * Used to print log
 *
 * @see com.github.panpf.zoomimage.core.common.test.util.LoggerTest
 */
class Logger(
    /**
     * The tag of the log
     */
    val tag: String,

    /**
     * Initial Level
     */
    level: Level = Level.Info,

    /**
     * Specifies the output pipeline of the log
     */
    var pipeline: Pipeline = defaultLogPipeline(),
) {

    /**
     * The level of the log. The level of the root logger will be modified directly
     */
    var level: Level = level
        set(value) {
            if (value != field) {
                val oldValue = field
                field = value
                val msg = "Logger. setLevel. $oldValue -> $value"
                pipeline.log(level = Level.Warn, tag = tag, msg = msg, tr = null)
            }
        }


    /**
     * Print a log with the VERBOSE level
     */
    fun v(msg: String) {
        log(Level.Verbose, msg)
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(lazyMsg: () -> String) {
        log(Level.Verbose, lazyMsg)
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(tr: Throwable?, msg: String) {
        log(Level.Verbose, tr, msg)
    }

    /**
     * Print a log with the VERBOSE level
     */
    fun v(tr: Throwable?, lazyMsg: () -> String) {
        log(Level.Verbose, tr, lazyMsg)
    }


    /**
     * Print a log with the DEBUG level
     */
    fun d(msg: String) {
        log(Level.Debug, msg)
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(lazyMsg: () -> String) {
        log(Level.Debug, lazyMsg)
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(tr: Throwable?, msg: String) {
        log(Level.Debug, tr, msg)
    }

    /**
     * Print a log with the DEBUG level
     */
    fun d(tr: Throwable?, lazyMsg: () -> String) {
        log(Level.Debug, tr, lazyMsg)
    }


    /**
     * Print a log with the INFO level
     */
    fun i(msg: String) {
        if (isLoggable(Level.Info)) {
            pipeline.log(level = Level.Info, tag = tag, msg = msg, tr = null)
        }
    }

    /**
     * Print a log with the INFO level
     */
    fun i(lazyMsg: () -> String) {
        log(Level.Info, lazyMsg)
    }

    /**
     * Print a log with the INFO level
     */
    fun i(tr: Throwable?, msg: String) {
        log(Level.Info, tr, msg)
    }

    /**
     * Print a log with the INFO level
     */
    fun i(tr: Throwable?, lazyMsg: () -> String) {
        log(Level.Info, tr, lazyMsg)
    }


    /**
     * Print a log with the WARN level
     */
    fun w(msg: String) {
        log(Level.Warn, msg)
    }

    /**
     * Print a log with the WARN level
     */
    fun w(lazyMsg: () -> String) {
        log(Level.Warn, lazyMsg)
    }

    /**
     * Print a log with the WARN level
     */
    fun w(tr: Throwable?, msg: String) {
        log(Level.Warn, tr, msg)
    }

    /**
     * Print a log with the WARN level
     */
    fun w(tr: Throwable?, lazyMsg: () -> String) {
        log(Level.Warn, tr, lazyMsg)
    }


    /**
     * Print a log with the ERROR level
     */
    fun e(msg: String) {
        log(Level.Error, msg)
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(lazyMsg: () -> String) {
        log(Level.Error, lazyMsg)
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(tr: Throwable?, msg: String) {
        log(Level.Error, tr, msg)
    }

    /**
     * Print a log with the ERROR level
     */
    fun e(tr: Throwable?, lazyMsg: () -> String) {
        log(Level.Error, tr, lazyMsg)
    }


    /**
     * Print a log with the specified level
     */
    fun log(level: Level, msg: String) {
        if (isLoggable(level)) {
            pipeline.log(level = level, tag = tag, msg = msg, tr = null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, lazyMsg: () -> String) {
        if (isLoggable(level)) {
            val msg = lazyMsg()
            pipeline.log(level = level, tag = tag, msg = msg, tr = null)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, tr: Throwable?, msg: String) {
        if (isLoggable(level)) {
            pipeline.log(level = level, tag = tag, msg = msg, tr = tr)
        }
    }

    /**
     * Print a log with the specified level
     */
    fun log(level: Level, tr: Throwable?, lazyMsg: () -> String) {
        if (isLoggable(level)) {
            val msg = lazyMsg()
            pipeline.log(level = level, tag = tag, msg = msg, tr = tr)
        }
    }

    fun isLoggable(level: Level): Boolean {
        return level >= this.level
    }


    /**
     * Flush the log pipeline
     */
    fun flush() {
        pipeline.flush()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Logger
        return tag == other.tag
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

    override fun toString(): String {
        return "Logger(tag='$tag', level=$level, pipeline=$pipeline)"
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