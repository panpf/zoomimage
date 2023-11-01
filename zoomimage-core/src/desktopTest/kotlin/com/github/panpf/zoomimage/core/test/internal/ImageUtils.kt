package com.github.panpf.zoomimage.core.test.internal

import java.awt.image.BufferedImage
import javax.imageio.ImageIO


fun readImage(resourcePath: String): BufferedImage {
    return useResource(resourcePath) {
        val imageInputStream = ImageIO.createImageInputStream(it)
        val imageReader = ImageIO.getImageReaders(imageInputStream).next().apply {
            input = imageInputStream
        }
        imageReader.read(0)
    }
}