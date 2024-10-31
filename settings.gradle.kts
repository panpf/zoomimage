// The name of the root project cannot be changed because the sample app needs to rely on
// it when generating compose resources.
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
include(":zoomimage-compose-coil2")
include(":zoomimage-compose-coil2-core")
include(":zoomimage-compose-coil3")
include(":zoomimage-compose-coil3-core")
include(":zoomimage-compose-glide")
include(":zoomimage-compose-resources")
include(":zoomimage-compose-sketch3")
include(":zoomimage-compose-sketch3-core")
include(":zoomimage-compose-sketch4")
include(":zoomimage-compose-sketch4-core")
include(":zoomimage-core")
include(":zoomimage-core-coil2")
include(":zoomimage-core-coil3")
include(":zoomimage-core-glide")
include(":zoomimage-core-picasso")
include(":zoomimage-core-sketch3")
include(":zoomimage-core-sketch4")
include(":zoomimage-view")
include(":zoomimage-view-coil2")
include(":zoomimage-view-coil2-core")
include(":zoomimage-view-coil3")
include(":zoomimage-view-coil3-core")
include(":zoomimage-view-glide")
include(":zoomimage-view-picasso")
include(":zoomimage-view-sketch3")
include(":zoomimage-view-sketch3-core")
include(":zoomimage-view-sketch4")
include(":zoomimage-view-sketch4-core")


/*
 * Private
 */
include(":sample")
include(":internal:images")
include(":internal:test-coil2")
include(":internal:test-coil3")
include(":internal:test-core")
include(":internal:test-sketch3")
include(":internal:test-sketch4")
include(":internal:test-view")
include(":internal:test-compose")
include(":internal:utils-coil3")
include(":internal:utils-coil3-compose")
