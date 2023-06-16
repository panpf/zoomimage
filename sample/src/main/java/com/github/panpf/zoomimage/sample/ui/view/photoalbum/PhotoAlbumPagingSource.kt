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
package com.github.panpf.zoomimage.sample.ui.view.photoalbum

import android.content.Context
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.panpf.tools4k.coroutines.withToIO
import com.github.panpf.zoomimage.sample.SampleImages

class PhotoAlbumPagingSource(private val context: Context) : PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val startPosition = params.key ?: 0
        val pageSize = params.loadSize

        val assetPhotos = if (startPosition == 0) readAssetPhotos() else emptyList()
        val localPhotos = readLocalPhotos(startPosition, pageSize)
            .let { it.getOrNull() ?: return LoadResult.Error(it.exceptionOrNull()!!) }
        val photos = assetPhotos.plus(localPhotos)
        val nextKey = if (localPhotos.isNotEmpty()) startPosition + pageSize else null
        return LoadResult.Page(photos, null, nextKey)
    }

    private suspend fun readAssetPhotos(): List<Photo> = withToIO {
        SampleImages.Asset.ALL.map { Photo(it.uri) }
    }

    private suspend fun readLocalPhotos(startPosition: Int, pageSize: Int): Result<List<Photo>> =
        withToIO {
            kotlin.runCatching {
                val cursor = context.contentResolver.query(
                    /* uri = */
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    /* projection = */
                    arrayOf(
                        MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.MIME_TYPE
                    ),
                    /* selection = */
                    null,
                    /* selectionArgs = */
                    null,
                    /* sortOrder = */
                    MediaStore.Images.Media.DATE_TAKEN + " DESC" + " limit " + startPosition + "," + pageSize
                )
                val list = ArrayList<Photo>(cursor?.count ?: 0)
                cursor?.use {
                    while (cursor.moveToNext()) {
                        list.add(Photo(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))))
                    }
                }
                list
            }
        }
}