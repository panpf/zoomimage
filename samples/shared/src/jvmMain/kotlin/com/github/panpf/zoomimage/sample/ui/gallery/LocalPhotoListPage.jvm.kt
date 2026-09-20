package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.ui.LocalNavBackStack
import com.github.panpf.zoomimage.sample.ui.PhotoPagerRoute
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun LocalPhotoListPage() {
    val navBackStack = LocalNavBackStack.current
    val appSettings: AppSettings = koinInject()
    val localPhotoListViewModel: LocalPhotoListViewModel = koinViewModel()
    PagingPhotoList(
        photoPagingFlow = localPhotoListViewModel.pagingFlow,
        gridCellsMinSize = gridCellsMinSize,
        refreshWhen = appSettings.localPhotosDirPath,
        onClick = { photos, _, index ->
            val params = buildPhotoPagerScreenParams(photos, index)
            navBackStack.add(PhotoPagerRoute(params))
        },
    )
}