/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.sample

import android.content.Context
import com.githb.panpf.zoomimage.images.AndroidResourceImages
import com.githb.panpf.zoomimage.images.ContentImages
import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.LocalImages
import com.githb.panpf.zoomimage.images.ResourceImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AndroidImages {

    suspend fun saveToExternalFilesDir(context: Context) = withContext(Dispatchers.IO) {
        val assetsDir = File((context.getExternalFilesDir(null) ?: context.filesDir), "assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }
        ResourceImages.values.forEach {
            val file = File(assetsDir, it.resourceName)
            if (!file.exists()) {
                context.assets.open(it.resourceName).use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    val MIXING_PHOTO_ALBUM = listOf(
        ResourceImages.cat,
        ResourceImages.dog,
        ResourceImages.anim,
        ResourceImages.longEnd,
        ContentImages.longWhale,
        ComposeResourceImages.hugeChina,
        AndroidResourceImages.hugeCard,
        LocalImages.hugeLongQmsht,
        HttpImages.hugeLongComic,
    )
}