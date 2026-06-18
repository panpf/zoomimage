package com.github.panpf.zoomimage.test

actual fun isGitHubActions(): Boolean {
    return System.getenv("GITHUB_ACTIONS") == "true"
}