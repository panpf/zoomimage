package com.github.panpf.zoomimage.util

import okio.FileSystem
import okio.SYSTEM

internal actual fun defaultFileSystem(): FileSystem = FileSystem.SYSTEM