package com.github.panpf.zoomimage.core.ios.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.PhotoAssetImageSource
import com.github.panpf.zoomimage.subsampling.fromPhotoAsset
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PhotoAssetImageSourceTest {

    @Test
    fun testFromPhotoAsset() {
        ImageSource.fromPhotoAsset(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
        ).apply {
            assertEquals("DB16113B-984A-4D12-B4D0-50FC46066781/L0/", this.localIdentifier)
            assertFalse(this.preferredThumbnail)
            assertFalse(this.allowNetworkAccess)
        }

        ImageSource.fromPhotoAsset(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
        ).apply {
            assertEquals("DB16113B-984A-4D12-B4D0-50FC46066781/L0/", this.localIdentifier)
            assertTrue(this.preferredThumbnail)
            assertTrue(this.allowNetworkAccess)
        }
    }

    @Test
    fun testConstructor() {
        PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )

        PhotoAssetImageSource(
            "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            true,
            true,
            "image/jpeg",
            ByteArray(0)
        )
    }

    @Test
    fun testKey() {
        assertEquals(
            expected = "file:///photos_asset/DB16113B-984A-4D12-B4D0-50FC46066781/L0/?preferredThumbnail=true&allowNetworkAccess=true",
            actual = PhotoAssetImageSource(
                localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
                preferredThumbnail = true,
                allowNetworkAccess = true,
                mimeType = "image/jpeg",
                data = ByteArray(0)
            ).key
        )
    }

    @Test
    fun testOpenSource() {
        PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        ).openSource().buffer().use {
            it.readByteArray()
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val source1 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )
        val source11 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )
        val source2 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L1/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )
        val source3 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = false,
            allowNetworkAccess = true,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )
        val source4 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = false,
            mimeType = "image/jpeg",
            data = ByteArray(0)
        )
        val source5 = PhotoAssetImageSource(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
            mimeType = "image/png",
            data = ByteArray(0)
        )

        assertEquals(expected = source1, actual = source1)
        assertEquals(expected = source1, actual = source11)
        assertNotEquals(illegal = source1, actual = source2)
        assertNotEquals(illegal = source1, actual = source3)
        assertNotEquals(illegal = source1, actual = source4)
        assertNotEquals(illegal = source1, actual = source5)
        assertNotEquals(illegal = source2, actual = source3)
        assertNotEquals(illegal = source2, actual = source4)
        assertNotEquals(illegal = source2, actual = source5)
        assertNotEquals(illegal = source3, actual = source4)
        assertNotEquals(illegal = source3, actual = source5)
        assertNotEquals(illegal = source4, actual = source5)
        assertNotEquals(illegal = source1, actual = null as Any?)
        assertNotEquals(illegal = source1, actual = Any())

        assertEquals(expected = source1.hashCode(), actual = source11.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source2.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source3.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source4.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source5.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source3.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source4.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source5.hashCode())
        assertNotEquals(illegal = source3.hashCode(), actual = source4.hashCode())
        assertNotEquals(illegal = source3.hashCode(), actual = source5.hashCode())
        assertNotEquals(illegal = source4.hashCode(), actual = source5.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "PhotoAssetsImageSource(" +
                    "localIdentifier='DB16113B-984A-4D12-B4D0-50FC46066781/L0/', " +
                    "preferredThumbnail=true, " +
                    "allowNetworkAccess=true, " +
                    "mimeType=image/jpeg)",
            actual = PhotoAssetImageSource(
                localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
                preferredThumbnail = true,
                allowNetworkAccess = true,
                mimeType = "image/jpeg",
                data = ByteArray(0)
            ).toString()
        )
    }

    @Test
    fun testFactoryConstructor() {
        PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
        )

        PhotoAssetImageSource.Factory(
            "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            true,
            true,
        )
    }

    @Test
    fun testFactoryKey() {
        assertEquals(
            expected = "file:///photos_asset/DB16113B-984A-4D12-B4D0-50FC46066781/L0/?preferredThumbnail=true&allowNetworkAccess=true",
            actual = PhotoAssetImageSource.Factory(
                localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
                preferredThumbnail = true,
                allowNetworkAccess = true,
            ).key
        )
    }

    @Test
    fun testFactoryOpenSource() = runTest {
        assertFails {
            PhotoAssetImageSource.Factory(
                localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
                preferredThumbnail = true,
                allowNetworkAccess = true,
            ).create()
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() {
        val source1 = PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
        )
        val source11 = PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
        )
        val source2 = PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L1/",
            preferredThumbnail = true,
            allowNetworkAccess = true,
        )
        val source3 = PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = false,
            allowNetworkAccess = true,
        )
        val source4 = PhotoAssetImageSource.Factory(
            localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
            preferredThumbnail = true,
            allowNetworkAccess = false,
        )

        assertEquals(expected = source1, actual = source1)
        assertEquals(expected = source1, actual = source11)
        assertNotEquals(illegal = source1, actual = source2)
        assertNotEquals(illegal = source1, actual = source3)
        assertNotEquals(illegal = source1, actual = source4)
        assertNotEquals(illegal = source2, actual = source3)
        assertNotEquals(illegal = source2, actual = source4)
        assertNotEquals(illegal = source3, actual = source4)
        assertNotEquals(illegal = source1, actual = null as Any?)
        assertNotEquals(illegal = source1, actual = Any())

        assertEquals(expected = source1.hashCode(), actual = source11.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source2.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source3.hashCode())
        assertNotEquals(illegal = source1.hashCode(), actual = source4.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source3.hashCode())
        assertNotEquals(illegal = source2.hashCode(), actual = source4.hashCode())
        assertNotEquals(illegal = source3.hashCode(), actual = source4.hashCode())
    }

    @Test
    fun testFactoryToString() {
        assertEquals(
            expected = "PhotoAssetsImageSource.Factory(" +
                    "localIdentifier='DB16113B-984A-4D12-B4D0-50FC46066781/L0/', " +
                    "preferredThumbnail=true, " +
                    "allowNetworkAccess=true)",
            actual = PhotoAssetImageSource.Factory(
                localIdentifier = "DB16113B-984A-4D12-B4D0-50FC46066781/L0/",
                preferredThumbnail = true,
                allowNetworkAccess = true,
            ).toString()
        )
    }
}