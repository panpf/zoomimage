//import androidx.compose.foundation.Image
//import androidx.compose.material.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.toComposeImageBitmap
//import androidx.compose.ui.res.ResourceLoader
//import com.github.panpf.zoomimage.subsampling.ImageInfo
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.awt.Rectangle
//import java.awt.image.BufferedImage
//import java.io.IOException
//import java.io.InputStream
//import javax.imageio.ImageIO
//import javax.imageio.ImageReadParam
//import javax.imageio.ImageReader
//import javax.imageio.stream.ImageInputStream
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun ImageRegionSample() {
//    MaterialTheme {
//        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//        LaunchedEffect(Unit) {
//            withContext(Dispatchers.IO) {
//                val stream = ResourceLoader.Default.load("sample_huge_china.jpg")
//                val rect = Rectangle(0, 0, 500, 500)
//                val image = readFragment(stream, rect)
//                imageBitmap = image.toComposeImageBitmap()
//            }
//        }
//
//        val imageBitmap2 = imageBitmap
//        if (imageBitmap2 != null) {
//            Image(imageBitmap2, contentDescription = "")
//        }
//    }
//}
//
//@Throws(IOException::class)
//fun readFragment(stream: InputStream?, rect: Rectangle?): BufferedImage {
//    val imageStream = ImageIO.createImageInputStream(stream)
//    val reader: ImageReader = ImageIO.getImageReaders(imageStream).next()
//    val param: ImageReadParam = reader.defaultReadParam.apply {
//        sourceRegion = rect
////        setSourceSubsampling(1, 1, 0, 0)
//        setSourceSubsampling(2, 2, 0, 0)
////        setSourceSubsampling(4, 4, 0, 0)
//    }
//    reader.setInput(imageStream, true, true)
//    val image: BufferedImage = reader.read(0, param)
//    reader.dispose()
//    imageStream.close()
//    return image
//}
//
//@Throws(IOException::class)
//fun readImageInfo(stream: InputStream): ImageInfo {
//    var imageStream: ImageInputStream? = null
//    var reader: ImageReader? = null
//    try {
//        imageStream = ImageIO.createImageInputStream(stream)
//        reader = ImageIO.getImageReaders(imageStream).next().apply {
//            setInput(imageStream, true, true)
//        }
//        val width = reader.getWidth(0)
//        val height = reader.getHeight(0)
//        val mimeType = "image/${reader.formatName.lowercase()}"
//        return ImageInfo(width = width, height = height, mimeType = mimeType)
//    } finally {
//        reader?.dispose()
//        imageStream?.close()
//    }
//}