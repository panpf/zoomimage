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

# ####################### for glides - start #######################
-keepclassmembers class com.bumptech.glide.Glide {
    com.bumptech.glide.load.engine.Engine engine;
}
-keepclassmembers class com.bumptech.glide.load.engine.Engine {
    com.bumptech.glide.load.engine.Engine$LazyDiskCacheProvider diskCacheProvider;
    com.bumptech.glide.load.engine.cache.MemoryCache cache;
    com.bumptech.glide.load.engine.ActiveResources activeResources;
}
-keepclassmembers class com.bumptech.glide.request.SingleRequest {
    java.lang.Object model;
    com.bumptech.glide.request.BaseRequestOptions requestOptions;
}
-keepclassmembers class com.bumptech.glide.RequestBuilder {
    java.lang.Object model;
}
# ####################### for glides - end #######################