pluginManagement {
    repositories {
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        mavenCentral()
        maven { setUrl("https://www.jitpack.io") }
        google()
//        maven { setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots") }
//        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(":sample")
include(":zoomimage-compose")
include(":zoomimage-compose-coil")
include(":zoomimage-compose-glide")
include(":zoomimage-compose-sketch")
include(":zoomimage-core-subsampling")
include(":zoomimage-core-subsampling-android")
include(":zoomimage-core-subsampling-android-coil")
include(":zoomimage-core-subsampling-android-glide")
include(":zoomimage-core-subsampling-android-picasso")
include(":zoomimage-core-subsampling-android-sketch")
include(":zoomimage-core-util")
include(":zoomimage-core-util-android")
include(":zoomimage-core-zoomable")
include(":zoomimage-core-zoomable-android")
include(":zoomimage-view")
include(":zoomimage-view-coil")
include(":zoomimage-view-glide")
include(":zoomimage-view-picasso")
include(":zoomimage-view-sketch")