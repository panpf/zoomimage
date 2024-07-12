package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.Logger
import org.junit.Assert
import org.junit.Test

class LoggerTest {

    @Test
    fun testTag() {
        Logger(tag = "MyTag").apply {
            Assert.assertEquals("MyTag", tag)
        }

        Logger(tag = "MyTag2").apply {
            Assert.assertEquals("MyTag2", tag)
        }
    }

    @Test
    fun testModule() {
        Logger(tag = "MyTag").apply {
            Assert.assertEquals(null, module)
        }

        Logger(tag = "MyTag2", module = "MyModule").apply {
            Assert.assertEquals("MyModule", module)
        }.newLogger(module = "MyModule2").apply {
            Assert.assertEquals("MyModule2", module)
        }
    }

    @Test
    fun testLevel() {
        val logger1 = Logger(tag = "MyTag")
        Assert.assertEquals(Logger.Level.Info, logger1.level)

        val logger2 = Logger(tag = "MyTag2").apply {
            level = Logger.Level.Debug
        }
        Assert.assertEquals(Logger.Level.Debug, logger2.level)

        /*
         * rootLogger
         */
        val logger11 = logger1.newLogger()
        val logger111 = logger1.newLogger()
        Assert.assertEquals(Logger.Level.Info, logger1.level)
        Assert.assertEquals(Logger.Level.Info, logger11.level)
        Assert.assertEquals(Logger.Level.Info, logger111.level)

        logger1.level = Logger.Level.Error
        Assert.assertEquals(Logger.Level.Error, logger1.level)
        Assert.assertEquals(Logger.Level.Error, logger11.level)
        Assert.assertEquals(Logger.Level.Error, logger111.level)

        logger11.level = Logger.Level.Warn
        Assert.assertEquals(Logger.Level.Warn, logger1.level)
        Assert.assertEquals(Logger.Level.Warn, logger11.level)
        Assert.assertEquals(Logger.Level.Warn, logger111.level)

        logger111.level = Logger.Level.Verbose
        Assert.assertEquals(Logger.Level.Verbose, logger1.level)
        Assert.assertEquals(Logger.Level.Verbose, logger11.level)
        Assert.assertEquals(Logger.Level.Verbose, logger111.level)


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
        Assert.assertEquals(
            listOf(
                "VERBOSE-MyTag-Hello",
                "DEBUG-MyTag-Hello",
                "INFO-MyTag-Hello",
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
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
        Assert.assertEquals(
            listOf(
                "DEBUG-MyTag-Hello",
                "INFO-MyTag-Hello",
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
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
        Assert.assertEquals(
            listOf(
                "INFO-MyTag-Hello",
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
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
        Assert.assertEquals(
            listOf(
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
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
        Assert.assertEquals(
            listOf(
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
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
        Assert.assertEquals(
            listOf("ASSERT-MyTag-Hello"),
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
        Assert.assertEquals(
            listOf(
                "VERBOSE-MyTag-Hello",
                "VERBOSE-MyTag-Hello2",
                "VERBOSE-MyTag-Hello-$exception",
                "VERBOSE-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.d("Hello")
        logger.d { "Hello2" }
        logger.d(exception, "Hello")
        logger.d(exception) { "Hello2" }
        Assert.assertEquals(
            listOf(
                "DEBUG-MyTag-Hello",
                "DEBUG-MyTag-Hello2",
                "DEBUG-MyTag-Hello-$exception",
                "DEBUG-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.i("Hello")
        logger.i { "Hello2" }
        logger.i(exception, "Hello")
        logger.i(exception) { "Hello2" }
        Assert.assertEquals(
            listOf(
                "INFO-MyTag-Hello",
                "INFO-MyTag-Hello2",
                "INFO-MyTag-Hello-$exception",
                "INFO-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.w("Hello")
        logger.w { "Hello2" }
        logger.w(exception, "Hello")
        logger.w(exception) { "Hello2" }
        Assert.assertEquals(
            listOf(
                "WARN-MyTag-Hello",
                "WARN-MyTag-Hello2",
                "WARN-MyTag-Hello-$exception",
                "WARN-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.e("Hello")
        logger.e { "Hello2" }
        logger.e(exception, "Hello")
        logger.e(exception) { "Hello2" }
        Assert.assertEquals(
            listOf(
                "ERROR-MyTag-Hello",
                "ERROR-MyTag-Hello2",
                "ERROR-MyTag-Hello-$exception",
                "ERROR-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger.log(Logger.Level.Assert, "Hello")
        logger.log(Logger.Level.Assert) { "Hello2" }
        logger.log(Logger.Level.Assert, exception, "Hello")
        logger.log(Logger.Level.Assert, exception) { "Hello2" }
        Assert.assertEquals(
            listOf(
                "ASSERT-MyTag-Hello",
                "ASSERT-MyTag-Hello2",
                "ASSERT-MyTag-Hello-$exception",
                "ASSERT-MyTag-Hello2-$exception",
            ),
            listPipeline.logs
        )
    }

    @Test
    fun testPipeline() {
        val listPipeline = ListPipeline()

        Logger("MyTag").apply {
            Assert.assertNotNull(pipeline.toString().takeIf { it == "AndroidLogPipeline" || it == "LogPipeline" })

            pipeline = listPipeline
            Assert.assertEquals("ListPipeline", pipeline.toString())
        }

    }

    @Test
    fun testFlush() {
        val listPipeline = ListPipeline()

        Logger("MyTag").apply {
            pipeline = listPipeline

            i("Hello")
            Assert.assertEquals(
                listOf("INFO-MyTag-Hello"),
                listPipeline.logs
            )

            flush()
            Assert.assertEquals(
                listOf("INFO-MyTag-Hello", "flush"),
                listPipeline.logs
            )

            flush()
            Assert.assertEquals(
                listOf("INFO-MyTag-Hello", "flush", "flush"),
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

    private fun getThreadName(): String? {
        return Thread.currentThread().name.let {
            // kotlin coroutine thread name 'DefaultDispatcher-worker-1' change to 'worker1'
            if (it.startsWith("DefaultDispatcher-worker-")) {
                "worker${it.substring("DefaultDispatcher-worker-".length)}"
            } else if (it.startsWith("Thread-")) {
                "Thread${it.substring("Thread-".length)}"
            } else {
                it
            }
        }
    }
}