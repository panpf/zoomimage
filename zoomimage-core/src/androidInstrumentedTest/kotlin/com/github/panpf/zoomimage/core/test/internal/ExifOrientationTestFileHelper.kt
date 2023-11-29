/*
 * Copyright (C) 2022 panpf <panpfpanpf@outlook.com>
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
package com.github.panpf.zoomimage.core.test.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

class ExifOrientationTestFileHelper(
    private val context: Context,
    private val assetFileName: String,
    private val inSampleSize: Int = 1
) {

    private val cacheDir: File = File(
        context.getExternalFilesDir(null) ?: context.filesDir,
        "exif_files" + "/" + File(assetFileName).nameWithoutExtension + "_${inSampleSize}"
    )
    private val configs = arrayOf(
        Config("NORMAL", ExifInterface.ORIENTATION_NORMAL, cacheDir),
        Config("ROTATE_90", ExifInterface.ORIENTATION_ROTATE_90, cacheDir),
        Config("TRANSVERSE", ExifInterface.ORIENTATION_TRANSVERSE, cacheDir),
        Config("ROTATE_180", ExifInterface.ORIENTATION_ROTATE_180, cacheDir),
        Config("FLIP_VER", ExifInterface.ORIENTATION_FLIP_VERTICAL, cacheDir),
        Config("ROTATE_270", ExifInterface.ORIENTATION_ROTATE_270, cacheDir),
        Config("TRANSPOSE", ExifInterface.ORIENTATION_TRANSPOSE, cacheDir),
        Config("FLIP_HOR", ExifInterface.ORIENTATION_FLIP_HORIZONTAL, cacheDir),
    )

    fun files(thumbnail: Boolean = false, forceReset: Boolean = false): List<TestFile> {
        val needReset = configs.any { !it.file.exists() }
        if (needReset || forceReset) {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            val originBitmap = context.assets.open(assetFileName).use {
                BitmapFactory.decodeStream(it, null, Options().apply {
                    inSampleSize = this@ExifOrientationTestFileHelper.inSampleSize
                })
            }!!
            val thumbnailBitmap: Bitmap? = if (thumbnail) {
                val scale = max(
                    originBitmap.width / 1000f,
                    originBitmap.height / 1000f
                )
                Bitmap.createScaledBitmap(
                    /* src = */ originBitmap,
                    /* dstWidth = */ (originBitmap.width / scale).roundToInt(),
                    /* dstHeight = */ (originBitmap.height / scale).roundToInt(),
                    /* filter = */ false
                )
            } else {
                null
            }
            for (config in configs) {
                val file = config.file
                if (!file.exists()) {
                    generatorTestFile(
                        file = file,
                        sourceBitmap = originBitmap,
                        orientation = config.orientation
                    )
                }
                if (thumbnailBitmap != null) {
                    val thumbnailFile = File(
                        file.parentFile,
                        file.nameWithoutExtension + "_thumbnail." + file.extension
                    )
                    if (!thumbnailFile.exists()) {
                        generatorTestFile(
                            file = thumbnailFile,
                            sourceBitmap = thumbnailBitmap,
                            orientation = config.orientation
                        )
                    }
                }
            }
//            originBitmap.recycle()
        }

        return configs.map {
            TestFile(it.name, it.file, it.orientation)
        }
    }

    private fun generatorTestFile(
        file: File,
        sourceBitmap: Bitmap,
        orientation: Int
    ) {
        val tileBitmap = AndroidTileBitmap(sourceBitmap)
        val newBitmap =
            AndroidExifOrientation(orientation)
                .applyToTileBitmap(tileBitmap, reverse = true)
                .let { it as AndroidTileBitmap }.bitmap!!
        file.parentFile?.mkdirs()
        file.createNewFile()
        FileOutputStream(file).use {
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        if (newBitmap !== sourceBitmap) {
            newBitmap.recycle()
        }

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
        cacheDir: File,
    ) {
        val file = File(cacheDir, "${name.lowercase()}.jpg")
    }

    class TestFile(
        @Suppress("unused") val title: String,
        @Suppress("unused") val file: File,
        @Suppress("unused") val exifOrientation: Int
    )
}