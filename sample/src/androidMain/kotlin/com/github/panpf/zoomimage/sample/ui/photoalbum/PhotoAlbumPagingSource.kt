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

package com.github.panpf.zoomimage.sample.ui.photoalbum

import android.content.Context
import android.provider.MediaStore.Images.Media
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.githb.panpf.zoomimage.images.AndroidResourceImages
import com.githb.panpf.zoomimage.images.ContentImages
import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.LocalImages
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4k.coroutines.withToIO
import com.github.panpf.zoomimage.sample.ComposeResourceImages

class PhotoAlbumPagingSource(private val context: Context) : PagingSource<Int, Photo2>() {

    override fun getRefreshKey(state: PagingState<Int, Photo2>): Int = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo2> {
        val startPosition = params.key ?: 0
        val pageSize = params.loadSize

        val assetPhotos = if (startPosition == 0) readAssetPhotos() else emptyList()
        val localPhotos = readLocalPhotos(startPosition, pageSize)
            .let { it.getOrNull() ?: return LoadResult.Error(it.exceptionOrNull()!!) }
        val photos = assetPhotos.plus(localPhotos)
        val nextKey = if (localPhotos.isNotEmpty()) startPosition + pageSize else null
        return LoadResult.Page(photos, null, nextKey)
    }

    private suspend fun readAssetPhotos(): List<Photo2> = withToIO {
        listOf(
            ResourceImages.cat,
            ResourceImages.dog,
            ResourceImages.anim,
            ResourceImages.longEnd,
            ContentImages.with(context).longWhale,
            ComposeResourceImages.hugeChina,
            AndroidResourceImages.hugeCard,
            LocalImages.with(context).hugeLongQmsht,
            HttpImages.hugeLongComic,
        ).map { Photo2(it.uri) }
    }

    private suspend fun readLocalPhotos(startPosition: Int, pageSize: Int): Result<List<Photo2>> =
        withToIO {
            kotlin.runCatching {
                val cursor = context.contentResolver.query(
                    /* uri = */
                    Media.EXTERNAL_CONTENT_URI,
                    /* projection = */
                    arrayOf(
                        Media.TITLE,
                        Media.DATA,
                        Media.SIZE,
                        Media.DATE_TAKEN,
                        Media.MIME_TYPE
                    ),
                    /* selection = */
                    null,
                    /* selectionArgs = */
                    null,
                    /* sortOrder = */
                    Media.DATE_TAKEN + " DESC" + " limit " + startPosition + "," + pageSize
                )
                val list = ArrayList<Photo2>(cursor?.count ?: 0)
                cursor?.use {
                    while (cursor.moveToNext()) {
                        list.add(
                            Photo2(
                                "file://${
                                    cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                            Media.DATA
                                        )
                                    )
                                }"
                            )
                        )
                    }
                }
                list
            }
        }
}