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

# okhttp3（5.0 版本已经默认添加以下规则）
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# App

# ################### for createViewBinding - start ###################
-keep class com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingActivity
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingActivity
-keep class com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingDialogFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingDialogFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.view.BaseToolbarBindingFragment
-keep class com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingItemFactory
-keep class * extends com.github.panpf.zoomimage.sample.ui.base.view.BaseBindingItemFactory
-keep class * implements androidx.viewbinding.ViewBinding{
    public *;
}
# ################### for createViewBinding - end ###################
