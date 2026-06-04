/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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

package com.github.panpf.zoomimage.subsampling

import com.github.panpf.zoomimage.util.fetchPhotosAsset
import com.github.panpf.zoomimage.util.resolveMimeType
import com.github.panpf.zoomimage.util.selectPrimaryResource
import com.github.panpf.zoomimage.util.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.Buffer
import okio.IOException
import okio.Source
import platform.Photos.PHAssetResource
import platform.Photos.PHAssetResourceManager
import platform.Photos.PHAssetResourceRequestOptions
import kotlin.coroutines.resumeWithException

/**
 * Create an image source from a PhotoAsset path.
 *
 * @see com.github.panpf.zoomimage.core.ios.test.subsampling.PhotoAssetImageSourceTest.testFromPhotoAsset
 */
fun ImageSource.Companion.fromPhotoAsset(
    localIdentifier: String,
    preferredThumbnail: Boolean = false,
    allowNetworkAccess: Boolean = false,
): PhotoAssetImageSource.Factory = PhotoAssetImageSource.Factory(
    localIdentifier = localIdentifier,
    preferredThumbnail = preferredThumbnail,
    allowNetworkAccess = allowNetworkAccess,
)

/**
 * An image source that reads images from a photo asset.
 *
 * @see com.github.panpf.zoomimage.core.ios.test.subsampling.PhotoAssetImageSourceTest
 */
class PhotoAssetImageSource(
    val localIdentifier: String,
    val preferredThumbnail: Boolean,
    val allowNetworkAccess: Boolean,
    val mimeType: String?,
    val data: ByteArray,
) : ImageSource {

    override val key: String =
        "file:///photos_asset/$localIdentifier?preferredThumbnail=$preferredThumbnail&allowNetworkAccess=$allowNetworkAccess"

    override fun openSource(): Source {
        return Buffer().apply { write(data) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PhotoAssetImageSource
        if (localIdentifier != other.localIdentifier) return false
        if (preferredThumbnail != other.preferredThumbnail) return false
        if (allowNetworkAccess != other.allowNetworkAccess) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = localIdentifier.hashCode()
        result = 31 * result + preferredThumbnail.hashCode()
        result = 31 * result + allowNetworkAccess.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }

    override fun toString(): String {
        return "PhotoAssetsImageSource(" +
                "localIdentifier='$localIdentifier', " +
                "preferredThumbnail=$preferredThumbnail, " +
                "allowNetworkAccess=$allowNetworkAccess, " +
                "mimeType=$mimeType)"
    }

    class Factory(
        val localIdentifier: String,
        val preferredThumbnail: Boolean = false,
        val allowNetworkAccess: Boolean = false,
    ) : ImageSource.Factory {

        override val key: String =
            "file:///photos_asset/$localIdentifier?preferredThumbnail=$preferredThumbnail&allowNetworkAccess=$allowNetworkAccess"

        override suspend fun create(): PhotoAssetImageSource {
            val asset = fetchPhotosAsset(localIdentifier)
                ?: throw IOException("Not found PHAsset: '$localIdentifier'")
            val resource = selectPrimaryResource(asset, preferredThumbnail)
                ?: throw IOException("Not found PHAssetResource: '$localIdentifier'")
            val mimeType = resolveMimeType(resource) ?: resolveMimeType(asset)
            if (mimeType?.startsWith("image/") != true) {
                throw IOException("The PHAssetResource is not an image: '$localIdentifier'")
            }
            val data = readBytes(resource)
            return PhotoAssetImageSource(
                localIdentifier = localIdentifier,
                preferredThumbnail = preferredThumbnail,
                allowNetworkAccess = allowNetworkAccess,
                mimeType = mimeType,
                data = data,
            )
        }

        @OptIn(ExperimentalForeignApi::class)
        private suspend fun readBytes(resource: PHAssetResource): ByteArray {
            return suspendCancellableCoroutine { continuation ->
                val buffer = Buffer()
                val options = PHAssetResourceRequestOptions().apply {
                    this.networkAccessAllowed = allowNetworkAccess
                }
                PHAssetResourceManager.defaultManager().requestDataForAssetResource(
                    resource = resource,
                    options = options,
                    dataReceivedHandler = { chunk ->
                        val byteArray = chunk?.toByteArray()
                        if (byteArray != null) {
                            buffer.write(byteArray)
                        }
                    },
                    completionHandler = { error ->
                        val byteArray = buffer.readByteArray()
                        if (byteArray.isNotEmpty()) {
                            continuation.resumeWith(Result.success(byteArray))
                        } else {
                            val message =
                                "Failed get bytes for PHAssetResource '${resource.originalFilename}': ${error?.localizedDescription}"
                            continuation.resumeWithException(IOException(message))
                        }
                    },
                )
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Factory
            if (localIdentifier != other.localIdentifier) return false
            if (preferredThumbnail != other.preferredThumbnail) return false
            if (allowNetworkAccess != other.allowNetworkAccess) return false
            return true
        }

        override fun hashCode(): Int {
            var result = localIdentifier.hashCode()
            result = 31 * result + preferredThumbnail.hashCode()
            result = 31 * result + allowNetworkAccess.hashCode()
            return result
        }

        override fun toString(): String {
            return "PhotoAssetsImageSource.Factory(" +
                    "localIdentifier='$localIdentifier', " +
                    "preferredThumbnail=$preferredThumbnail, " +
                    "allowNetworkAccess=$allowNetworkAccess)"
        }
    }
}