///*
// * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.github.panpf.zoomimage.internal
//
//import android.content.Context
//import android.net.wifi.WifiConfiguration.Status.ENABLED
//import androidx.annotation.WorkerThread
//import com.github.panpf.zoomimage.ImageSource
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.InputStream
//
//class GlideImageSource(
//    private val context: Context,
//    private val sketch: Sketch,
//    private val imageUri: String,
//) : ImageSource {
//
//    override val key: String = imageUri
//
//    @WorkerThread
//    override suspend fun openInputStream(): Result<InputStream> {
//        val request = LoadRequest(context, imageUri) {
//            downloadCachePolicy(ENABLED)
//        }
//        val fetcher = try {
//            sketch.components.newFetcherOrThrow(request)
//        } catch (e: Exception) {
//            return Result.failure(e)
//        }
//        val fetchResult = withContext(Dispatchers.IO) {
//            fetcher.fetch()
//        }.let {
//            it.getOrNull() ?: return Result.failure(it.exceptionOrNull()!!)
//        }
//        val dataSource = fetchResult.dataSource
//        if (dataSource !is BasedStreamDataSource) {
//            return Result.failure(IllegalStateException("DataSource is not BasedStreamDataSource. imageUri='$imageUri'"))
//        }
//        return kotlin.runCatching { dataSource.newInputStream() }
//    }
//}