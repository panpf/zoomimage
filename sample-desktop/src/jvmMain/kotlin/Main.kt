import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.panpf.zoomimage.ZoomImage


fun main() = application {
    Window(
        title = "ZoomImage",
        onCloseRequest = ::exitApplication
    ) {
        App()
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
