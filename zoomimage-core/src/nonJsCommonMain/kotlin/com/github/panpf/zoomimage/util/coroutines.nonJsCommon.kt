package com.github.panpf.zoomimage.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.FileSystem
import okio.SYSTEM

actual fun ioCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO

val d = FileSystem.SYSTEM