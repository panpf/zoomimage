package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.zoomimage.sample.ui.components.PermissionContainer
import com.github.panpf.zoomimage.sample.ui.gridCellsMinSize
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.storage.STORAGE
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun LocalPhotoListPage() {
    PermissionContainer(
        permission = Permission.STORAGE,    // TODO Change to GALLERY
        permissionRequired = false,
    ) {
        val navigator = LocalNavigator.current!!
        val localPhotoListViewModel: LocalPhotoListViewModel = koinViewModel()
        PagingPhotoList(
            photoPagingFlow = localPhotoListViewModel.pagingFlow,
            gridCellsMinSize = gridCellsMinSize,
            onClick = { photos, _, index ->
                val params = buildPhotoPagerScreenParams(photos, index)
                navigator.push(PhotoPagerScreen(params))
            },
        )
    }
}