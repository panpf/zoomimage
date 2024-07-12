# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# ---------------Begain: OkHttp
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
# ---------------End: OkHttp


# ---------------Begain: Okio
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# ---------------End: Okio


# ---------------Begain: kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Application rules

# Change here com.github.panpf.sketch.sample
-keepclassmembers @kotlinx.serialization.Serializable class com.github.panpf.sketch.sample.** {
    # lookup for plugin generated serializable classes
    *** Companion;
    # lookup for serializable objects
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
# lookup for plugin generated serializable classes
-if @kotlinx.serialization.Serializable class com.github.panpf.sketch.sample.**
-keepclassmembers class com.github.panpf.sketch.sample.<1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# ---------------End: kotlin serialization


# --------------- Begain: createViewBinding
-keep class com.github.panpf.zoomimage.sample.ui.base.BaseBindingActivity
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.BaseBindingActivity
-keep class com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.BaseBindingDialogFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.BaseBindingFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.BaseToolbarBindingFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.BaseBindingItemFactory
-keep class * implements androidx.viewbinding.ViewBinding{
    public *;
}
# --------------- End: createViewBinding

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder