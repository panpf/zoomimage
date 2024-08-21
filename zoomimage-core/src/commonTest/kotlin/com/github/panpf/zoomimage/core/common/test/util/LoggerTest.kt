package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.test.ListPipeline
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.defaultLogPipeline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

        logger1.level = Logger.Level.Info
        assertEquals(Logger.Level.Info, logger1.level)

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
    fun testV() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Info
        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Warn
        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Error
        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger.v("Hello")
        logger.v { "Hello2" }
        logger.v(exception, "Hello")
        logger.v(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)
    }

    @Test
    fun testD() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
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

        logger.level = Logger.Level.Info
        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Warn
        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Error
        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)
    }

    @Test
    fun testI() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
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

        logger.level = Logger.Level.Info
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

        logger.level = Logger.Level.Warn
        listPipeline.logs.clear()
        logger.i("Hello")
        logger.i { "Hello2" }
        logger.i(exception, "Hello")
        logger.i(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Error
        listPipeline.logs.clear()
        logger.i("Hello")
        logger.i { "Hello2" }
        logger.i(exception, "Hello")
        logger.i(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger.i("Hello")
        logger.i { "Hello2" }
        logger.i(exception, "Hello")
        logger.i(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)
    }

    @Test
    fun testW() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
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

        logger.level = Logger.Level.Info
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

        logger.level = Logger.Level.Warn
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

        logger.level = Logger.Level.Error
        listPipeline.logs.clear()
        logger.w("Hello")
        logger.w { "Hello2" }
        logger.w(exception, "Hello")
        logger.w(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)

        logger.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger.w("Hello")
        logger.w { "Hello2" }
        logger.w(exception, "Hello")
        logger.w(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)
    }

    @Test
    fun testE() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
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

        logger.level = Logger.Level.Info
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

        logger.level = Logger.Level.Warn
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

        logger.level = Logger.Level.Error
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

        logger.level = Logger.Level.Assert
        listPipeline.logs.clear()
        logger.e("Hello")
        logger.e { "Hello2" }
        logger.e(exception, "Hello")
        logger.e(exception) { "Hello2" }
        assertEquals(listOf(), listPipeline.logs)
    }

    @Test
    fun testA() {
        val listPipeline = ListPipeline()
        val exception = Exception("test exception")
        val logger = Logger("MyTag", pipeline = listPipeline)

        logger.level = Logger.Level.Verbose
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

        logger.level = Logger.Level.Debug
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

        logger.level = Logger.Level.Info
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

        logger.level = Logger.Level.Warn
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

        logger.level = Logger.Level.Error
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

        logger.level = Logger.Level.Assert
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

    @Test
    fun testEqualsAndHashCode() {
        val logger1 = Logger("MyTag")
        val logger12 = Logger("MyTag")
        val logger2 = Logger("MyTag2")

        assertEquals(expected = logger1, actual = logger1)
        assertEquals(expected = logger1, actual = logger12)
        assertNotEquals(illegal = logger1, actual = null as Any?)
        assertNotEquals(illegal = logger1, actual = Any())
        assertNotEquals(illegal = logger1, actual = logger2)

        assertEquals(expected = logger1.hashCode(), actual = logger12.hashCode())
        assertNotEquals(illegal = logger1.hashCode(), actual = logger2.hashCode())
    }

    @Test
    fun testToString() {
        val pipeline = defaultLogPipeline()
        val logger1 = Logger("MyTag")
        val logger2 = Logger("MyTag2")
        assertEquals(
            expected = "Logger(tag='MyTag', level=Info, pipeline=$pipeline)",
            actual = logger1.toString()
        )
        assertEquals(
            expected = "Logger(tag='MyTag2', level=Info, pipeline=$pipeline)",
            actual = logger2.toString()
        )
    }
}