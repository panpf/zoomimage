import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.fromResource
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.compose.widget.rememberMyDialogState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable


fun main() = application {
    Window(
        title = "ZoomImage",
        onCloseRequest = ::exitApplication
    ) {
        // todo Copy the example from sample-android
        // sample-android and sample-desktop are merged into a single multiplatform sample, which allows compose code to be shared
        App()
//        App2()
//        ImageRegionSample()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val zoomState = rememberZoomState()
        zoomState.logger.level = Logger.DEBUG
        LaunchedEffect(Unit) {
//            zoomState.zoomable.rotate(90)
            zoomState.subsampling.setImageSource(
                ImageSource.fromResource(
                    ResourceLoader.Default,
//                    "sample_huge_china.jpg"
//                    "sample_huge_card.jpg"
                    "sample_huge_world.jpg"
                )
            )
//            zoomState.subsampling.setImageSource(ImageSource.fromFile(File("/Users/panpf/Downloads/sample_huge_china.jpg")))
            zoomState.zoomable.oneFingerScaleSpec = OneFingerScaleSpec.Default
        }
        ZoomImage(
            modifier = Modifier.fillMaxSize(),
//            painter = painterResource("sample_huge_china_thumbnail.jpg"),
//            painter = painterResource("sample_huge_card_thumbnail.jpg"),
            painter = painterResource("sample_huge_world_thumbnail.jpg"),
            contentDescription = "China",
            state = zoomState,
        )
        ZoomImageMinimap(
            imageUri = "sample_huge_world_thumbnail.jpg",
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
        )
        val infoDialogState = rememberMyDialogState()
        ZoomImageTool(
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
            imageUri = "sample_huge_world_thumbnail.jpg",
        )
    }
}

@Composable
@Preview
fun App2() {
    MaterialTheme {
        val zoomableState = rememberZoomableState()
        Image(
            modifier = Modifier.fillMaxSize().zoomable(zoomableState),
            painter = painterResource("sample_huge_china.jpg"),
            contentDescription = "China",
        )
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "scale: ${zoomableState.contentTransformation.scale.toShortString()}",
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                ),
            )
            Text(
                text = "offset: ${zoomableState.contentTransformation.offset.toShortString()}",
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                ),
            )
            Text(
                text = "centroid: ${zoomableState.contentTransformation.centroid?.toShortString()}",
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(offset = Offset(0f, 0f), blurRadius = 10f),
                ),
            )
        }
    }
}
