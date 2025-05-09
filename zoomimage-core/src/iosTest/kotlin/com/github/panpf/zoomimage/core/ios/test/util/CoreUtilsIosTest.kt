package com.github.panpf.zoomimage.core.ios.test.util

import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.requiredWorkThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CoreUtilsIosTest {

    @Test
    fun testRequiredMainThread() = runTest {
        withContext(Dispatchers.IO) {
            assertFailsWith(IllegalStateException::class) {
                requiredMainThread()
            }
        }
        withContext(Dispatchers.Main) {
            requiredMainThread()
        }
    }

    @Test
    fun testRequiredWorkThread() = runTest {
        withContext(Dispatchers.IO) {
            requiredWorkThread()
        }
        withContext(Dispatchers.Main) {
            assertFailsWith(IllegalStateException::class) {
                requiredWorkThread()
            }
        }
    }
}