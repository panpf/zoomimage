package com.github.panpf.zoomimage.sample.util

import coil3.ImageLoader
import coil3.Uri
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.github.panpf.sketch.fetch.HttpUriFetcher
import com.github.panpf.sketch.util.MimeTypeMap
import okio.Buffer
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

class CoilComposeResourceUriFetcher(
    val fileSystem: FileSystem,
    val resourcePath: String,
) : Fetcher {

    companion object {
        const val SCHEME = "compose.resource"
    }

    @OptIn(InternalResourceApi::class)
    override suspend fun fetch(): FetchResult {
        val bytes = readResourceBytes(resourcePath)
        val source = ImageSource(
            source = Buffer().write(bytes),
            fileSystem = fileSystem,
            metadata = null
        )
        val mimeType = getMimeType(resourcePath, null)
        return SourceFetchResult(
            source = source,
            mimeType = mimeType,
            dataSource = DataSource.DISK
        )
    }

    /**
     * Parse the response's `content-type` header.
     *
     * "text/plain" is often used as a default/fallback MIME type.
     * Attempt to guess a better MIME type from the file extension.
     */
    private fun getMimeType(url: String, contentType: String?): String? {
        if (contentType == null
            || contentType.trim().isEmpty()
            || contentType.startsWith(HttpUriFetcher.MIME_TYPE_TEXT_PLAIN)
        ) {
            MimeTypeMap.getMimeTypeFromUrl(url)?.let { return it }
        }
        return contentType?.substringBefore(';')
    }

    class Factory : Fetcher.Factory<Uri> {

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader
        ): CoilComposeResourceUriFetcher? {
            return if (SCHEME.equals(data.scheme, ignoreCase = true)) {
                val resourcePath = "${data.authority.orEmpty()}${data.path.orEmpty()}"
                CoilComposeResourceUriFetcher(
                    fileSystem = imageLoader.diskCache?.fileSystem ?: ThrowingFileSystem,
                    resourcePath = resourcePath
                )
            } else {
                null
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Factory
        }

        override fun hashCode(): Int {
            return this@Factory::class.hashCode()
        }

        override fun toString(): String = "CoilComposeResourceUriFetcher"
    }
}

/** A file system that throws if any of its methods are called. */
private object ThrowingFileSystem : FileSystem() {

    override fun atomicMove(source: Path, target: Path) {
        throwReadWriteIsUnsupported()
    }

    override fun canonicalize(path: Path): Path {
        throwReadWriteIsUnsupported()
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        throwReadWriteIsUnsupported()
    }

    override fun createSymlink(source: Path, target: Path) {
        throwReadWriteIsUnsupported()
    }

    override fun delete(path: Path, mustExist: Boolean) {
        throwReadWriteIsUnsupported()
    }

    override fun list(dir: Path): List<Path> {
        throwReadWriteIsUnsupported()
    }

    override fun listOrNull(dir: Path): List<Path>? {
        throwReadWriteIsUnsupported()
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        throwReadWriteIsUnsupported()
    }

    override fun openReadOnly(file: Path): FileHandle {
        throwReadWriteIsUnsupported()
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        throwReadWriteIsUnsupported()
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        throwReadWriteIsUnsupported()
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        throwReadWriteIsUnsupported()
    }

    override fun source(file: Path): Source {
        throwReadWriteIsUnsupported()
    }

    private fun throwReadWriteIsUnsupported(): Nothing {
        throw UnsupportedOperationException(
            "Javascript does not have access to the device's file system and cannot read from or " +
                    "write to it. If you are running on Node.js use 'NodeJsFileSystem' instead."
        )
    }
}