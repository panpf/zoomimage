import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.panpf.zoomimage.sample.ui.Page
import com.github.panpf.zoomimage.sample.ui.navigation.NavigationContainer
import com.github.panpf.zoomimage.sample.ui.util.EventBus
import kotlinx.coroutines.launch

fun main() = application {
    val coroutineScope = rememberCoroutineScope()
    Window(
        title = "ZoomImage",
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1000.dp, 800.dp)),
        onKeyEvent = {
            coroutineScope.launch {
                EventBus.keyEvent.emit(it)
            }
            false
        }
    ) {
        MaterialTheme {
            NavigationContainer(Page.Main)
        }
    }
}


// Kamel 始终加载原图，不支持子采样，所以无法使用
//    val desktopConfig = remember {
//        KamelConfig {
//            takeFrom(KamelConfig.Default)
//            // Available only on Desktop.
//            resourcesFetcher()
//            // Available only on Desktop.
//            // An alternative svg decoder
//            batikSvgDecoder()
//        }
//    }
//    CompositionLocalProvider(LocalKamelConfig provides desktopConfig) {
//    }