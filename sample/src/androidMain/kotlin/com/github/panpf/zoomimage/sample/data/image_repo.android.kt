package com.github.panpf.zoomimage.sample.data

import android.provider.MediaStore.Images.Media
import androidx.core.content.PermissionChecker
import com.githb.panpf.zoomimage.images.AndroidLocalImages
import com.githb.panpf.zoomimage.images.AndroidResourceImages
import com.githb.panpf.zoomimage.images.ContentImages
import com.githb.panpf.zoomimage.images.HttpImages
import com.githb.panpf.zoomimage.images.ImageFile
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.tools4k.coroutines.withToIO

actual suspend fun builtinImages(context: PlatformContext): List<ImageFile> {
    return listOf(
        ResourceImages.cat,
        ResourceImages.dog,
        ResourceImages.anim,
        ResourceImages.longEnd,
        ContentImages.create(context).longWhale,
        ComposeResourceImages.hugeChina,
        AndroidResourceImages.hugeCard,
        AndroidLocalImages.with(context).hugeLongQmsht,
        HttpImages.hugeLongComic,
    ).plus(ResourceImages.exifs)
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