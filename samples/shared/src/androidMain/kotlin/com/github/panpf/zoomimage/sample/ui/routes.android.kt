package com.github.panpf.zoomimage.sample.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.panpf.zoomimage.sample.ui.test.TelephotoSwitchTestScreen
import com.github.panpf.zoomimage.sample.ui.test.TempAndroidTestScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

@Serializable
sealed interface AndroidRoute : MyNavKey

@Serializable
data object TelephotoSwitchTestRoute : AndroidRoute

@Serializable
data object TempAndroidTestRoute : AndroidRoute

actual fun PolymorphicModuleBuilder<NavKey>.platformSerializersModule() {
    subclass(TelephotoSwitchTestRoute::class, TelephotoSwitchTestRoute.serializer())
    subclass(TempAndroidTestRoute::class, TempAndroidTestRoute.serializer())
}

actual fun EntryProviderScope<NavKey>.platformEntryProvider() {
    entry<TelephotoSwitchTestRoute> { TelephotoSwitchTestScreen() }
    entry<TempAndroidTestRoute> { TempAndroidTestScreen() }
}
