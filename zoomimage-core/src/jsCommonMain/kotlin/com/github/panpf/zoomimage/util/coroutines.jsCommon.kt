package com.github.panpf.zoomimage.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun ioCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default