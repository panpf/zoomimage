package com.github.panpf.zoomimage.sample.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.savedstate.serialization.SavedStateConfiguration
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoPagerScreen
import com.github.panpf.zoomimage.sample.ui.gallery.PhotoPagerScreenParams
import com.github.panpf.zoomimage.sample.ui.test.CoilBigStartCrossfadeTestScreen
import com.github.panpf.zoomimage.sample.ui.test.ExifOrientationTestScreen
import com.github.panpf.zoomimage.sample.ui.test.GraphicsLayerTestScreen
import com.github.panpf.zoomimage.sample.ui.test.ImageSourceTestScreen
import com.github.panpf.zoomimage.sample.ui.test.KeyTestScreen
import com.github.panpf.zoomimage.sample.ui.test.ModifierZoomTestScreen
import com.github.panpf.zoomimage.sample.ui.test.MouseTestScreen
import com.github.panpf.zoomimage.sample.ui.test.OverlayTestScreen
import com.github.panpf.zoomimage.sample.ui.test.TempTestScreen
import com.github.panpf.zoomimage.sample.ui.test.ZoomImageSwitchTestScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

interface MyNavKey : NavKey {
    val lightModeSystemBars: Boolean
        get() = true
}

@Serializable
sealed interface Route : MyNavKey

@Serializable
data object VerHomeRoute : Route

@Serializable
data object HorHomeRoute : Route

@Serializable
data object ImageSourceTestRoute : Route

@Serializable
data object ExifOrientationTestRoute : Route

@Serializable
data object GraphicsLayerTestRoute : Route

@Serializable
data object ModifierZoomTestRoute : Route

@Serializable
data object MouseTestRoute : Route

@Serializable
data object KeyTestRoute : Route

@Serializable
data object ZoomImageSwitchTestRoute : Route

@Serializable
data object CoilBigStartCrossfadeTestRoute : Route

@Serializable
data object OverlayTestRoute : Route

@Serializable
data object TempTestRoute : Route

@Serializable
data class PhotoPagerRoute(val params: PhotoPagerScreenParams) : Route {
    override val lightModeSystemBars: Boolean
        get() = false
}

val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
//            subclassesOfSealed<Route>()
            subclass(VerHomeRoute::class, VerHomeRoute.serializer())
            subclass(HorHomeRoute::class, HorHomeRoute.serializer())
            subclass(PhotoPagerRoute::class, PhotoPagerRoute.serializer())
            subclass(ImageSourceTestRoute::class, ImageSourceTestRoute.serializer())
            subclass(ExifOrientationTestRoute::class, ExifOrientationTestRoute.serializer())
            subclass(GraphicsLayerTestRoute::class, GraphicsLayerTestRoute.serializer())
            subclass(ModifierZoomTestRoute::class, ModifierZoomTestRoute.serializer())
            subclass(MouseTestRoute::class, MouseTestRoute.serializer())
            subclass(KeyTestRoute::class, KeyTestRoute.serializer())
            subclass(ZoomImageSwitchTestRoute::class, ZoomImageSwitchTestRoute.serializer())
            subclass(
                CoilBigStartCrossfadeTestRoute::class,
                CoilBigStartCrossfadeTestRoute.serializer()
            )
            subclass(OverlayTestRoute::class, OverlayTestRoute.serializer())
            subclass(TempTestRoute::class, TempTestRoute.serializer())

            platformSerializersModule()
        }
    }
}

expect fun PolymorphicModuleBuilder<NavKey>.platformSerializersModule()

@Suppress("RemoveExplicitTypeArguments")
val navEntryProvider = entryProvider<NavKey> {
    entry<VerHomeRoute> { VerHomeScreen() }
    entry<HorHomeRoute> { HorHomeScreen() }
    entry<PhotoPagerRoute> { PhotoPagerScreen(it.params) }
    entry<ImageSourceTestRoute> { ImageSourceTestScreen() }
    entry<ExifOrientationTestRoute> { ExifOrientationTestScreen() }
    entry<GraphicsLayerTestRoute> { GraphicsLayerTestScreen() }
    entry<ModifierZoomTestRoute> { ModifierZoomTestScreen() }
    entry<MouseTestRoute> { MouseTestScreen() }
    entry<KeyTestRoute> { KeyTestScreen() }
    entry<ZoomImageSwitchTestRoute> { ZoomImageSwitchTestScreen() }
    entry<CoilBigStartCrossfadeTestRoute> { CoilBigStartCrossfadeTestScreen() }
    entry<OverlayTestRoute> { OverlayTestScreen() }
    entry<TempTestRoute> { TempTestScreen() }

    platformEntryProvider()
}

expect fun EntryProviderScope<NavKey>.platformEntryProvider()

val LocalNavBackStack: ProvidableCompositionLocal<NavBackStack<NavKey>> =
    staticCompositionLocalOf { error("No NavStack provided") }