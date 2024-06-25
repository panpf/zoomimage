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

package com.github.panpf.zoomimage.sample.ui.test.view

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.sketch.asSketchImage
import com.github.panpf.sketch.decode.internal.AndroidExifOrientationHelper
import com.github.panpf.sketch.decode.internal.calculateSampleSize
import com.github.panpf.sketch.getBitmapOrThrow
import com.github.panpf.sketch.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ExifOrientationTestContentViewModel(private val application: Application) :
    AndroidViewModel(application) {

    private val _showContentState = MutableStateFlow<List<Pair<String, File>>>(emptyList())
    val showContentState: StateFlow<List<Pair<String, File>>> = _showContentState

    init {
        viewModelScope.launch {
            _showContentState.value = exportExifOrientationImages()
        }
    }

    private suspend fun exportExifOrientationImages(): List<Pair<String, File>> {
        val originImageName = "sample_long_comic"
        val originImageAssetFileName = "sample_long_comic.jpg"
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val originBitmap by lazy {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    application.assets.open(originImageAssetFileName).use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    val inSampleSize = calculateSampleSize(
                        imageSize = Size(width = options.outWidth, height = options.outHeight),
                        targetSize = Size(
                            width = application.resources.displayMetrics.widthPixels * 2,
                            height = application.resources.displayMetrics.heightPixels * 2
                        ),
                        mimeType = options.outMimeType
                    )
                    application.assets.open(originImageAssetFileName).use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                        })
                    }!!
                }
                val cacheDir: File = application.getExternalFilesDir("exif_orientation")!!
                arrayOf(
                    Config("ROTATE_90", ExifInterface.ORIENTATION_ROTATE_90),
                    Config("TRANSVERSE", ExifInterface.ORIENTATION_TRANSVERSE),
                    Config("ROTATE_180", ExifInterface.ORIENTATION_ROTATE_180),
                    Config("FLIP_VER", ExifInterface.ORIENTATION_FLIP_VERTICAL),
                    Config("ROTATE_270", ExifInterface.ORIENTATION_ROTATE_270),
                    Config("TRANSPOSE", ExifInterface.ORIENTATION_TRANSPOSE),
                    Config("FLIP_HOR", ExifInterface.ORIENTATION_FLIP_HORIZONTAL),
                ).map { config ->
                    val cacheFile = File(cacheDir, "${originImageName}_${config.name}.jpeg")
                    if (!cacheFile.exists()) {
                        generatorTestFile(cacheFile, originBitmap, config.orientation)
                    }
                    config.name to cacheFile
                }
            }
        }.getOrNull() ?: emptyList()
    }

    private fun generatorTestFile(
        file: File,
        sourceBitmap: Bitmap,
        orientation: Int
    ) {
        val newBitmap = AndroidExifOrientationHelper(orientation)
            .applyToImage(image = sourceBitmap.asSketchImage(), reverse = true)
            ?.getBitmapOrThrow()
            ?: sourceBitmap
        FileOutputStream(file).use {
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        newBitmap.recycle()

        val exifInterface: ExifInterface
        try {
            exifInterface = ExifInterface(file.path)
        } catch (e: IOException) {
            e.printStackTrace()
            file.delete()
            return
        }

        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
        try {
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
            file.delete()
        }
    }

    private class Config(
        val name: String,
        val orientation: Int,
    )
}