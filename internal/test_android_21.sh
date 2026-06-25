#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

rootDir="${PWD%/internal/*}"
cd $rootDir

# The following modules support running tests on Android 21
./gradlew \
  zoomimage-compose-coil2:connectedAndroidTest \
  zoomimage-compose-coil2-core:connectedAndroidTest \
  zoomimage-compose-glide:connectedAndroidTest \
  zoomimage-compose-sketch3:connectedAndroidTest \
  zoomimage-compose-sketch3-core:connectedAndroidTest \
  zoomimage-core:connectedAndroidTest \
  zoomimage-core-coil2:connectedAndroidTest \
  zoomimage-core-coil3:connectedAndroidTest \
  zoomimage-core-glide:connectedAndroidTest \
  zoomimage-core-picasso:connectedAndroidTest \
  zoomimage-core-sketch3:connectedAndroidTest \
  zoomimage-core-sketch4:connectedAndroidTest \
  zoomimage-view:connectedAndroidTest \
  zoomimage-view-coil2:connectedAndroidTest \
  zoomimage-view-coil2-core:connectedAndroidTest \
  zoomimage-view-coil3:connectedAndroidTest \
  zoomimage-view-coil3-core:connectedAndroidTest \
  zoomimage-view-glide:connectedAndroidTest \
  zoomimage-view-picasso:connectedAndroidTest \
  zoomimage-view-sketch3:connectedAndroidTest \
  zoomimage-view-sketch3-core:connectedAndroidTest \
  zoomimage-view-sketch4:connectedAndroidTest \
  zoomimage-view-sketch4-core:connectedAndroidTest \
  zoomimage-view-sketch4-koin:connectedAndroidTest \
  --continue

echo "✅  Android tests are passed successfully."