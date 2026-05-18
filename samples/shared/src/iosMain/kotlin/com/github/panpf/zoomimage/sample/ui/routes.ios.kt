package com.github.panpf.zoomimage.sample.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder

@Serializable
sealed interface IosRoute : NavKey

actual fun PolymorphicModuleBuilder<NavKey>.platformSerializersModule() {
//    subclassesOfSealed<IosRoute>()
}

actual fun EntryProviderScope<NavKey>.platformEntryProvider() {
}
