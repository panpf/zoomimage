package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoggerTest {

    @Test
    fun testTag() {
        Logger(tag = "MyTag").apply {
            assertEquals("MyTag", tag)
        }

        Logger(tag = "MyTag2").apply {
            assertEquals("MyTag2", tag)
        }
    }

    @Test
    fun testLevel() {
        val logger1 = Logger(tag = "MyTag")
        assertEquals(Logger.Level.Info, logger1.level)

        val logger2 = Logger(tag = "MyTag2").apply {
            level = Logger.Level.Debug
        }
        assertEquals(Logger.Level.Debug, logger2.level)

        /*
         * rootLogger
         */
        val logger11 = Logger(tag = "MyTag")
        val logger111 = Logger(tag = "MyTag")
        assertEquals(Logger.Level.Info, logger1.level)
        assertEquals(Logger.Level.Info, logger11.level)
        assertEquals(Logger.Level.Info, logger111.level)

        logger1.level = Logger.Level.Error
        assertEquals(Logger.Level.Error, logger1.level)
        assertEquals(Logger.Level.Info, logger11.level)
        assertEquals(Logger.Level.Info, logger111.level)

        logger11.level = Logger.Level.Warn
        assertEquals(Logger.Level.Error, logger1.level)
        assertEquals(Logger.Level.Warn, logger11.level)
        assertEquals(Logger.Level.Info, logger111.level)

        logger111.level = Logger.Level.Verbose
        assertEquals(Logger.Level.Error, logger1.level)
        assertEquals(Logger.Level.Warn, logger11.level)
        assertEquals(Logger.Level.Verbose, logger111.level)


        val listPipeline = ListPipeline()
        logger1.pipeline = listPipeline

        logger1.level = Logger.Level.Verbose
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf(
                "Verbose-MyTag-Hello",
                "Debug-MyTag-Hello",
                "Info-MyTag-Hello",
                "Warn-MyTag-Hello",
                "Error-MyTag-Hello",
                "Assert-MyTag-Hello"
            ),
            listPipeline.logs
        )

        logger1.level = Logger.Level.Debug
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf(
                "Debug-MyTag-Hello",
                "Info-MyTag-Hello",
                "Warn-MyTag-Hello",
                "Error-MyTag-Hello",
                "Assert-MyTag-Hello"
            ),
            listPipeline.logs
        )

        logger1.level = Logger.Level.Info
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf(
                "Info-MyTag-Hello",
                "Warn-MyTag-Hello",
                "Error-MyTag-Hello",
                "Assert-MyTag-Hello"
            ),
            listPipeline.logs
        )

        logger1.level = Logger.Level.Warn
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf(
                "Warn-MyTag-Hello",
                "Error-MyTag-Hello",
                "Assert-MyTag-Hello"
            ),
            listPipeline.logs
        )

        logger1.level = Logger.Level.Error
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf(
                "Error-MyTag-Hello",
                "Assert-MyTag-Hello"
            ),
            listPipeline.logs
        )

        logger1.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.Level.Assert, "Hello")
        assertEquals(
            listOf("Assert-MyTag-Hello"),
            listPipeline.logs
        )
    }

    @Test
    fun testPrint() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")

        val logger = Logger("MyTag").apply {
            pipeline = listPipeline
            level = Logger.Level.Verbose
        }

        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(
            listOf(
                "Verbose-MyTag-Hello",
                "Verbose-MyTag-Hello2",
                "Verbose-MyTag-Hello-$exception",
                "Verbose-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        assertEquals(
            listOf(
                "Debug-MyTag-Hello",
                "Debug-MyTag-Hello2",
                "Debug-MyTag-Hello-$exception",
                "Debug-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.i("Hello")
        logger.i { "Hello2" }
        logger.i(exception, "Hello")
        logger.i(exception) { "Hello2" }
        assertEquals(
            listOf(
                "Info-MyTag-Hello",
                "Info-MyTag-Hello2",
                "Info-MyTag-Hello-$exception",
                "Info-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.w("Hello")
        logger.w { "Hello2" }
        logger.w(exception, "Hello")
        logger.w(exception) { "Hello2" }
        assertEquals(
            listOf(
                "Warn-MyTag-Hello",
                "Warn-MyTag-Hello2",
                "Warn-MyTag-Hello-$exception",
                "Warn-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.e("Hello")
        logger.e { "Hello2" }
        logger.e(exception, "Hello")
        logger.e(exception) { "Hello2" }
        assertEquals(
            listOf(
                "Error-MyTag-Hello",
                "Error-MyTag-Hello2",
                "Error-MyTag-Hello-$exception",
                "Error-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.log(Logger.Level.Assert, "Hello")
        logger.log(Logger.Level.Assert) { "Hello2" }
        logger.log(Logger.Level.Assert, exception, "Hello")
        logger.log(Logger.Level.Assert, exception) { "Hello2" }
        assertEquals(
            listOf(
                "Assert-MyTag-Hello",
                "Assert-MyTag-Hello2",
                "Assert-MyTag-Hello-$exception",
                "Assert-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )
    }

    @Test
    fun testPipeline() {
        val listPipeline = ListPipeline()

        Logger("MyTag").apply {
            assertNotNull(
                pipeline.toString()
                    .takeIf { it == "AndroidLogPipeline" || it == "PrintlnLogPipeline" })

            pipeline = listPipeline
            assertEquals("ListPipeline", pipeline.toString())
        }
    }

    @Test
    fun testFlush() {
        val listPipeline = ListPipeline()

        Logger("MyTag").apply {
            pipeline = listPipeline

            i("Hello")
            assertEquals(
                listOf("Info-MyTag-Hello"),
                listPipeline.logs
            )

            flush()
            assertEquals(
                listOf("Info-MyTag-Hello", "flush"),
                listPipeline.logs
            )

            flush()
            assertEquals(
                listOf("Info-MyTag-Hello", "flush", "flush"),
                listPipeline.logs
            )
        }
    }

    private class ListPipeline : Logger.Pipeline {

        var logs = mutableListOf<String>()

        override fun log(level: Logger.Level, tag: String, msg: String, tr: Throwable?) {
            val finalMsg = if (tr != null) {
                "${level}-$tag-$msg-$tr"
            } else {
                "${level}-$tag-$msg"
            }
            logs.add(finalMsg)
        }

        override fun flush() {
            logs.add("flush")
        }

        override fun toString(): String {
            return "ListPipeline"
        }
    }
}