package com.github.panpf.zoomimage.images

import okio.FileSystem
import okio.SYSTEM

/**
 * Get default file system.
 */
actual fun defaultFileSystem(): FileSystem = FileSystem.SYSTEM
