package com.github.panpf.zoomimage.core.jscommon.test.util

import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.requiredWorkThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test

class CoreUtilsJsCommonTest {

    @Test
    fun testRequiredMainThread() = runTest {
        withContext(Dispatchers.Default) {
            requiredMainThread()
        }
        withContext(Dispatchers.Main) {
            requiredMainThread()
        }
    }

    @Test
    fun testRequiredWorkThread() = runTest {
        withContext(Dispatchers.Default) {
            requiredWorkThread()
        }
        withContext(Dispatchers.Main) {
            requiredWorkThread()
        }
    }
}