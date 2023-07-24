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
include(":zoomimage-core")
include(":zoomimage-core-coil")
include(":zoomimage-core-glide")
include(":zoomimage-core-sketch")
include(":zoomimage-view")
include(":zoomimage-view-coil")
include(":zoomimage-view-glide")
include(":zoomimage-view-picasso")
include(":zoomimage-view-sketch")