import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.panpf.zoomimage.ZoomImage
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable


fun main() = application {
    Window(
        title = "ZoomImage",
        onCloseRequest = ::exitApplication
    ) {
        App()
//        App2()
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        ZoomImage(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource("sample_huge_china.jpg"),
            contentDescription = "China",
        )
    }
}

@Composable
@Preview
fun App2() {
    MaterialTheme {
        Image(
            modifier = Modifier.fillMaxSize().zoomable(rememberZoomableState()),
            painter = painterResource("sample_huge_china.jpg"),
            contentDescription = "China",
        )
    }
}
