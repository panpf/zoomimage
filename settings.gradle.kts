// After turning on TYPESAFE_PROJECT_ACCESSORS, the root directory name and sketch module name cannot be the same.
rootProject.name = "zoomimage"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

/*
 * Release
 */
include(":zoomimage-compose")
include(":zoomimage-compose-coil")
include(":zoomimage-compose-coil-core")
include(":zoomimage-compose-glide")
include(":zoomimage-compose-sketch")
include(":zoomimage-compose-sketch-core")
include(":zoomimage-core")
include(":zoomimage-core-coil")
include(":zoomimage-core-glide")
include(":zoomimage-core-picasso")
include(":zoomimage-core-sketch")
include(":zoomimage-view")
include(":zoomimage-view-coil")
include(":zoomimage-view-glide")
include(":zoomimage-view-picasso")
include(":zoomimage-view-sketch")


/*
 * Private
 */
include(":sample")
include(":internal:images")