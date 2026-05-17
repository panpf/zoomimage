package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import org.koin.compose.viewmodel.koinViewModel

actual fun localPhotoListPermission(): Any? = null

@Composable
actual fun LocalPhotoListPage(screen: Screen) {
    val navigator = LocalNavigator.current!!
    val localPhotoListViewModel: LocalPhotoListViewModel = koinViewModel()
    PhotoList(
        photoPaging = localPhotoListViewModel.photoPaging,
        modifier = Modifier.fillMaxSize(),
        gridCellsMinSize = gridCellsMinSize,
        onClick = { photos1, _, index ->
            val params = buildPhotoPagerScreenParams(photos1, index)
            navigator.push(PhotoPagerScreen(params))
        }
    )
}