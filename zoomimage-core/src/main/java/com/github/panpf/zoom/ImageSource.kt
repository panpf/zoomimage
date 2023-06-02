package com.github.panpf.zoom

import androidx.annotation.WorkerThread
import java.io.InputStream

interface ImageSource {

    val key: String

    @WorkerThread
    suspend fun openInputStream(): Result<InputStream>
}