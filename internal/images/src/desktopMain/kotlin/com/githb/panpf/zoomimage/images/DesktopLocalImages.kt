package com.githb.panpf.zoomimage.images

import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.harawata.appdirs.AppDirsFactory
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import java.io.File
import java.net.URI
import java.nio.file.Paths

class DesktopLocalImages private constructor() {

    companion object {

        private var instance: DesktopLocalImages? = null

        suspend fun with(): DesktopLocalImages {
            saveToExternalFilesDir()
            return instance ?: synchronized(this) {
                instance ?: DesktopLocalImages().also { instance = it }
            }
        }

        suspend fun saveToExternalFilesDir() = withContext(Dispatchers.IO) {
            val fileSystem = FileSystem.SYSTEM
            val outDir = appCacheDirectory().resolve("zoomimage-files")
            if (!fileSystem.exists(outDir)) {
                fileSystem.createDirectories(outDir)
            }
            ComposeResImageFiles.values.forEach {
                val file = outDir.resolve(it.name)
                if (!fileSystem.exists(file)) {
                    try {
                        it.toImageSource().openSource()
                            .buffer().use { input ->
                                fileSystem.sink(file).buffer().use { output ->
                                    output.writeAll(input)
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        fileSystem.delete(file)
                    }
                }
            }
        }
    }

    private val cacheDir = File("${appCacheDirectory().resolve("zoomimage-files")}")

    val cat = ComposeResImageFiles.cat.toLocalImageFile(cacheDir)
    val dog = ComposeResImageFiles.dog.toLocalImageFile(cacheDir)
    val anim = ComposeResImageFiles.anim.toLocalImageFile(cacheDir)
    val longEnd = ComposeResImageFiles.longEnd.toLocalImageFile(cacheDir)
    val longWhale = ComposeResImageFiles.longWhale.toLocalImageFile(cacheDir)
    val hugeChina = ComposeResImageFiles.hugeChina.toLocalImageFile(cacheDir)
    val hugeCard = ComposeResImageFiles.hugeCard.toLocalImageFile(cacheDir)
    val hugeLongQmsht = ComposeResImageFiles.hugeLongQmsht.toLocalImageFile(cacheDir)
    val hugeLongComic = ComposeResImageFiles.hugeLongComic.toLocalImageFile(cacheDir)

    val exifFlipHorizontal = ComposeResImageFiles.exifFlipHorizontal.toLocalImageFile(cacheDir)
    val exifFlipVertical = ComposeResImageFiles.exifFlipVertical.toLocalImageFile(cacheDir)
    val exifNormal = ComposeResImageFiles.exifNormal.toLocalImageFile(cacheDir)
    val exifRotate90 = ComposeResImageFiles.exifRotate90.toLocalImageFile(cacheDir)
    val exifRotate180 = ComposeResImageFiles.exifRotate180.toLocalImageFile(cacheDir)
    val exifRotate270 = ComposeResImageFiles.exifRotate270.toLocalImageFile(cacheDir)
    val exifTranspose = ComposeResImageFiles.exifTranspose.toLocalImageFile(cacheDir)
    val exifTransverse = ComposeResImageFiles.exifTransverse.toLocalImageFile(cacheDir)

    val exifs = arrayOf(
        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )

    val all = listOf(
        cat,
        dog,
        anim,
        longEnd,
        longWhale,
        hugeChina,
        hugeCard,
        hugeLongQmsht,
        hugeLongComic,

        exifFlipHorizontal,
        exifFlipVertical,
        exifNormal,
        exifRotate90,
        exifRotate180,
        exifRotate270,
        exifTranspose,
        exifTransverse,
    )
}

class DesktopLocalImageFile(
    override val name: String,
    override val uri: String,
    override val size: IntSizeCompat,
    override val exifOrientation: Int = ExifOrientation.UNDEFINED,
) : ImageFile {

    override fun toString(): String =
        "DesktopLocalImageFile(name='$name', uri='$uri', size=$size, exifOrientation=$exifOrientation)"
}

fun ComposeResImageFile.toLocalImageFile(
    cacheDir: File
): DesktopLocalImageFile = DesktopLocalImageFile(
    name = this.name,
    uri = File(cacheDir, this.name).path,
    size = this.size,
    exifOrientation = this.exifOrientation
)

fun appCacheDirectory(): Path {
    val appName = (getComposeResourcesPath() ?: getJarPath(DesktopLocalImages::class.java))
        ?.md5()
        ?: throw UnsupportedOperationException(
            "Unable to generate application aliases to automatically initialize downloadCache and resultCache, " +
                    "please configure them manually. Documentation address 'https://github.com/panpf/sketch/blob/main/docs/getting_started.md'"
        )
    return requireNotNull(
        AppDirsFactory.getInstance().getUserCacheDir(
            /* appName = */ "SketchImageLoader${File.separator}${appName}",
            /* appVersion = */ null,
            /* appAuthor = */ null,
        )
    ) { "Failed to get the cache directory of the App" }.toPath()
}

/**
 * Returns the path to the app's compose resources directory. Only works in release mode
 *
 * Example:
 * macOs: '/Applications/hellokmp.app/Contents/app/resources'
 * Windows: 'C:\Program Files\hellokmp\app\resources'
 * Linux: '/opt/hellokmp/lib/app/resources'
 * dev: null
 */
fun getComposeResourcesPath(): String? {
    return System.getProperty("compose.application.resources.dir")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

/**
 * Returns the path to the jar file where the specified class is located
 *
 * Example:
 * macOs: '/Applications/hellokmp.app/Contents/app/composeApp-desktop-f6c789dab561fea8ab3a9533b659d11a.jar'
 * Windows: 'C:\Program Files\hellokmp\app\composeApp-desktop-55c1f2d3ee1433be9f95b1912fbd.jar'
 * Linux: '/opt/hellokmp/lib/app/composeApp-desktop-e1e452276759301f909baa97e6a11ff4.jar'
 * dev: '/Users/panpf/Workspace/KotlinProjectDesktop/composeApp/build/classes/kotlin/desktop/main'
 */
fun getJarPath(aclass: Class<*>): String? {
    try {
        val codeSource = aclass.protectionDomain.codeSource
        if (codeSource != null) {
            val location: URI = codeSource.location.toURI()
            return Paths.get(location).toString()
        } else {
            return null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

internal fun String.md5() = encodeUtf8().md5().hex()