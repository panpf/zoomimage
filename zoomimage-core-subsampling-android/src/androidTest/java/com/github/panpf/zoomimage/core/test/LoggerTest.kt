package com.github.panpf.zoomimage.core.test

import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.Logger
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
    fun testShowThreadName() {
        val threadName = getThreadName()
        val listPipeline = ListPipeline()

        val logger = Logger(tag = "MyTag").apply {
            pipeline = listPipeline
        }
        Assert.assertEquals(false, logger.showThreadName)
        logger.i("Hello")
        Assert.assertEquals("INFO-MyTag-Hello", listPipeline.logs[0])

        val logger2 = Logger(tag = "MyTag2", showThreadName = true).apply {
            listPipeline.logs.clear()
            pipeline = listPipeline
        }
        Assert.assertEquals(true, logger2.showThreadName)
        logger2.i("Hello2")
        Assert.assertEquals("INFO-MyTag2-${threadName} - Hello2", listPipeline.logs[0])

        val logger3 = logger.newLogger(showThreadName = true).apply {
            listPipeline.logs.clear()
            pipeline = listPipeline
        }
        Assert.assertEquals(true, logger3.showThreadName)
        logger3.i("Hello3")
        Assert.assertEquals("INFO-MyTag-${threadName} - Hello3", listPipeline.logs[0])
    }

    @Test
    fun testLevel() {
        val logger1 = Logger(tag = "MyTag")
        Assert.assertEquals(Logger.INFO, logger1.level)

        val logger2 = Logger(tag = "MyTag2").apply {
            level = Logger.DEBUG
        }
        Assert.assertEquals(Logger.DEBUG, logger2.level)

        /*
         * rootLogger
         */
        val logger11 = logger1.newLogger()
        val logger111 = logger1.newLogger()
        Assert.assertEquals(Logger.INFO, logger1.level)
        Assert.assertEquals(Logger.INFO, logger11.level)
        Assert.assertEquals(Logger.INFO, logger111.level)

        logger1.level = Logger.ERROR
        Assert.assertEquals(Logger.ERROR, logger1.level)
        Assert.assertEquals(Logger.ERROR, logger11.level)
        Assert.assertEquals(Logger.ERROR, logger111.level)

        logger11.level = Logger.WARN
        Assert.assertEquals(Logger.WARN, logger1.level)
        Assert.assertEquals(Logger.WARN, logger11.level)
        Assert.assertEquals(Logger.WARN, logger111.level)

        logger111.level = Logger.VERBOSE
        Assert.assertEquals(Logger.VERBOSE, logger1.level)
        Assert.assertEquals(Logger.VERBOSE, logger11.level)
        Assert.assertEquals(Logger.VERBOSE, logger111.level)


        val listPipeline = ListPipeline()
        logger1.pipeline = listPipeline

        listPipeline.logs.clear()
        logger1.level = Logger.VERBOSE
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
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

        listPipeline.logs.clear()
        logger1.level = Logger.DEBUG
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
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

        listPipeline.logs.clear()
        logger1.level = Logger.INFO
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
        Assert.assertEquals(
            listOf(
                "INFO-MyTag-Hello",
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger1.level = Logger.WARN
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
        Assert.assertEquals(
            listOf(
                "WARN-MyTag-Hello",
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger1.level = Logger.ERROR
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
        Assert.assertEquals(
            listOf(
                "ERROR-MyTag-Hello",
                "ASSERT-MyTag-Hello"
            ),
            listPipeline.logs
        )

        listPipeline.logs.clear()
        logger1.level = Logger.ASSERT
        logger1.v("Hello")
        logger1.d("Hello")
        logger1.i("Hello")
        logger1.w("Hello")
        logger1.e("Hello")
        logger1.log(Logger.ASSERT, "Hello")
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
            level = Logger.VERBOSE
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
        logger.log(Logger.ASSERT, "Hello")
        logger.log(Logger.ASSERT) { "Hello2" }
        logger.log(Logger.ASSERT, exception, "Hello")
        logger.log(Logger.ASSERT, exception) { "Hello2" }
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
            Assert.assertEquals("AndroidLogPipeline", pipeline.toString())

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

    @Test
    fun testLevelName() {
        Assert.assertEquals("VERBOSE", Logger.levelName(Logger.VERBOSE))
        Assert.assertEquals("DEBUG", Logger.levelName(Logger.DEBUG))
        Assert.assertEquals("INFO", Logger.levelName(Logger.INFO))
        Assert.assertEquals("WARN", Logger.levelName(Logger.WARN))
        Assert.assertEquals("ERROR", Logger.levelName(Logger.ERROR))
        Assert.assertEquals("ASSERT", Logger.levelName(Logger.ASSERT))
        Assert.assertEquals("UNKNOWN", Logger.levelName(8))
        Assert.assertEquals("UNKNOWN", Logger.levelName(1))

        Assert.assertEquals(Logger.VERBOSE, Logger.level("VERBOSE"))
        Assert.assertEquals(Logger.DEBUG, Logger.level("DEBUG"))
        Assert.assertEquals(Logger.INFO, Logger.level("INFO"))
        Assert.assertEquals(Logger.WARN, Logger.level("WARN"))
        Assert.assertEquals(Logger.ERROR, Logger.level("ERROR"))
        Assert.assertEquals(Logger.ASSERT, Logger.level("ASSERT"))
        assertThrow(IllegalArgumentException::class) {
            Logger.level("UNKNOWN")
        }
    }

    private class ListPipeline : Logger.Pipeline {

        var logs = mutableListOf<String>()

        override fun log(level: Int, tag: String, msg: String, tr: Throwable?) {
            val finalMsg = if (tr != null) {
                "${Logger.levelName(level)}-$tag-$msg-$tr"
            } else {
                "${Logger.levelName(level)}-$tag-$msg"
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