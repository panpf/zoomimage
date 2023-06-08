@file:Suppress("PackageDirectoryMismatch")

package com.squareup.picasso

internal val Picasso.downloader: Downloader
    get() = dispatcher.downloader

internal val Picasso.cache: Cache
    get() = dispatcher.cache

internal fun isDisallowMemoryCache(memoryPolicy: Int): Boolean {
    return !MemoryPolicy.shouldReadFromMemoryCache(memoryPolicy)
            || !MemoryPolicy.shouldWriteToMemoryCache(memoryPolicy)
}