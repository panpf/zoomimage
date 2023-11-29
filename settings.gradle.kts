pluginManagement {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public") }  // central、jcenter
//        maven { setUrl("https://maven.aliyun.com/repository/google") }  // google
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }    // central、google、jcenter
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public") }  // central、jcenter
//        maven { setUrl("https://maven.aliyun.com/repository/google") }  // google
//        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }    // central、google、jcenter
        mavenCentral()
        google()
        maven { setUrl("https://www.jitpack.io") }
//        maven { setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots") }
//        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(":sample-android")
include(":sample-common")
include(":sample-desktop")
include(":zoomimage-compose")
include(":zoomimage-compose-coil")
include(":zoomimage-compose-coil-base")
include(":zoomimage-compose-glide")
include(":zoomimage-compose-sketch")
include(":zoomimage-compose-sketch-core")
include(":zoomimage-core")
include(":zoomimage-core-coil")
include(":zoomimage-core-glide")
include(":zoomimage-core-picasso")
include(":zoomimage-core-sketch")
include(":zoomimage-resources")
include(":zoomimage-view")
include(":zoomimage-view-coil")
include(":zoomimage-view-glide")
include(":zoomimage-view-picasso")
include(":zoomimage-view-sketch")