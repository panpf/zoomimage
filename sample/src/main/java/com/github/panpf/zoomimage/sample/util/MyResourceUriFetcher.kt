package com.github.panpf.zoomimage.sample.util

import android.annotation.SuppressLint
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.datasource.DrawableDataSource
import com.github.panpf.sketch.datasource.ResourceDataSource
import com.github.panpf.sketch.fetch.FetchResult
import com.github.panpf.sketch.fetch.Fetcher
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.util.ResDrawable
import com.github.panpf.sketch.util.ifOrNull

/**
 * Support the following uri:
 *
 * 'android.resource://resource?resType=drawable&resName=ic_launcher'
 * 'android.resource://resource?resId=1031232'
 * 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resType=drawable&resName=ic_launcher'
 * 'android.resource://resource?packageName=com.github.panpf.sketch.sample&resId=1031232'
 */
// todo 更新 sketch 2.3.1-rc03 版本后就可以删除了
class MyResourceUriFetcher(
    val sketch: Sketch,
    val request: ImageRequest,
    val contentUri: Uri,
) : Fetcher {

    companion object {
        const val SCHEME = "android.resource"
    }

    @SuppressLint("DiscouragedApi")
    @WorkerThread
    override suspend fun fetch(): Result<FetchResult> = kotlin.runCatching {
        val packageName = contentUri.getQueryParameters("packageName")
            .firstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: request.context.packageName
        val resources: Resources = try {
            request.context.packageManager.getResourcesForApplication(packageName)
        } catch (ex: NameNotFoundException) {
            throw Resources.NotFoundException("Not found Resources by packageName: $contentUri")
        }

        val resId = contentUri.getQueryParameters("resId").firstOrNull()?.toIntOrNull()
        val finalResId = if (resId != null) {
            resId
        } else {
            val resType =
                contentUri.getQueryParameters("resType").firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
            val resName =
                contentUri.getQueryParameters("resName").firstOrNull()
                    ?.takeIf { it.isNotEmpty() }
            if (resType == null || resName == null) {
                throw Resources.NotFoundException("Invalid resource uri: $contentUri")
            }
            resources.getIdentifier(resName, resType, packageName).takeIf { it != 0 }
                ?: throw Resources.NotFoundException("No found resource identifier by resType, resName: $contentUri")
        }

        val path =
            TypedValue().apply { resources.getValue(finalResId, this, true) }.string ?: ""
        val entryName = path.lastIndexOf('/').takeIf { it != -1 }
            ?.let { path.substring(it + 1) }
            ?: path.toString()
        val mimeType = getMimeTypeFromUrl(entryName)
        val dataSource = if (resources.getResourceTypeName(finalResId) == "raw") {
                ResourceDataSource(
                    sketch = sketch,
                    request = request,
                    drawableId = finalResId,
                    packageName = packageName,
                    resources = resources
                )
            } else {
                DrawableDataSource(
                    sketch = sketch,
                    request = request,
                    dataFrom = DataFrom.LOCAL,
                    drawableFetcher = ResDrawable(packageName, resources, finalResId)
                )
            }
        FetchResult(dataSource, mimeType)
    }

    class Factory : Fetcher.Factory {

        override fun create(sketch: Sketch, request: ImageRequest): MyResourceUriFetcher? {
            val uri = request.uriString.toUri()
            return ifOrNull(SCHEME.equals(uri.scheme, ignoreCase = true)) {
                MyResourceUriFetcher(sketch, request, uri)
            }
        }

        override fun toString(): String = "ResourceUriFetcher"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}

/**
 * Modified from [MimeTypeMap.getFileExtensionFromUrl] to be more permissive
 * with special characters.
 */
internal fun getMimeTypeFromUrl(url: String?): String? =
    MimeTypeMap.getSingleton().getMimeTypeFromUrl(url)

/**
 * Modified from [MimeTypeMap.getFileExtensionFromUrl] to be more permissive
 * with special characters.
 */
internal fun MimeTypeMap.getMimeTypeFromUrl(url: String?): String? {
    if (url.isNullOrBlank()) {
        return null
    }

    val extension = url
        .substringBeforeLast('#') // Strip the fragment.
        .substringBeforeLast('?') // Strip the query.
        .substringAfterLast('/') // Get the last path segment.
        .substringAfterLast('.', missingDelimiterValue = "") // Get the file extension.

    return getMimeTypeFromExtension(extension)
}