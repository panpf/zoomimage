import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.panpf.zoomimage.sample.ui.Page
import com.github.panpf.zoomimage.sample.ui.navigation.NavigationContainer
import com.github.panpf.zoomimage.sample.ui.navigation.rememberNavigation

fun main() = application {
    val navigation = rememberNavigation(Page.Main)
    Window(
        title = "ZoomImage",
        onCloseRequest = ::exitApplication,
//        onKeyEvent = {
////            if (it.type == KeyEventType.KeyUp) {  // invalid
////                when (it.key) {
////                    Key.Escape -> navigation.back()
////                }
////            }
//            false
//        }
    ) {
        MaterialTheme {
            NavigationContainer(Page.Main)
        }
    }
}


// 不支持子采样，始终加载原图
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