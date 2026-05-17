package com.github.panpf.zoomimage.sample.data

import android.provider.MediaStore.Images.Media
import androidx.core.content.PermissionChecker
import com.githb.panpf.zoomimage.images.AndroidLocalImageFiles
import com.githb.panpf.zoomimage.images.AndroidResourceImageFiles
import com.githb.panpf.zoomimage.images.ComposeResImageFiles
import com.githb.panpf.zoomimage.images.ContentImageFiles
import com.githb.panpf.zoomimage.images.HttpImageFiles
import com.githb.panpf.zoomimage.images.ImageFile
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.tools4k.coroutines.withToIO

actual suspend fun builtinImages(context: PlatformContext): List<ImageFile> {
    return listOf(
        ComposeResImageFiles.cat,
        ComposeResImageFiles.dog,
        ComposeResImageFiles.anim,
        ComposeResImageFiles.longEnd,
        ContentImageFiles.create(context).longWhale,
        ComposeResImageFiles.hugeChina,
        AndroidResourceImageFiles.hugeCard,
        AndroidLocalImageFiles.with(context).hugeLongQmsht,
        HttpImageFiles.hugeLongComic,
    ).plus(ComposeResImageFiles.exifs)
}

actual suspend fun localImages(
    context: PlatformContext,
    startPosition: Int,
    pageSize: Int
): List<String> {
    val checkSelfPermission = PermissionChecker
        .checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    if (checkSelfPermission != PermissionChecker.PERMISSION_GRANTED) {
        return emptyList()
    }
    return withToIO {
        val cursor = context.contentResolver.query(
            /* uri = */ Media.EXTERNAL_CONTENT_URI,
            /* projection = */
            arrayOf(
                Media.TITLE,
                Media.DATA,
                Media.SIZE,
                Media.DATE_TAKEN,
            ),
            /* selection = */
            null,
            /* selectionArgs = */
            null,
            /* sortOrder = */
            Media.DATE_TAKEN + " DESC" + " limit " + startPosition + "," + pageSize
        )
        ArrayList<String>(cursor?.count ?: 0).apply {
            cursor?.use {
                while (cursor.moveToNext()) {
                    val uri =
                        cursor.getString(cursor.getColumnIndexOrThrow(Media.DATA))
                    add(uri)
                }
            }
        }
    }
}