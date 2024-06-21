package com.github.panpf.zoomimage.core.test.subsampling.internal

class BitmapFactoryDecodeHelperTest {

    // TODO test
//    @Test
//    fun test() {
//        val context = InstrumentationRegistry.getInstrumentation().context
//        val logger = Logger("Test")
//
//        val imageSource = ImageSource.fromAsset(context, "sample_exif_girl_normal.jpg")
//        val exifOrientation = imageSource.decodeExifOrientation()
//            .getOrThrow().let { it as AndroidExifOrientation }
//            .apply {
//                Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, this.exifOrientation)
//            }
//        val imageInfo = imageSource.decodeImageInfo().getOrThrow().let {
//            exifOrientation.applyToImageInfo(it)
//        }
//        val tileDecoder = AndroidTileDecoder(
//            logger = logger,
//            imageSource = imageSource,
//            imageInfo = imageInfo,
//            exifOrientation = exifOrientation,
//            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
//        )
//        val bitmap: Bitmap
//        try {
//            bitmap = tileDecoder
//                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
//                .let { it as AndroidTileBitmap }
//                .bitmap!!
//            bitmap.apply {
//                Assert.assertEquals(200, width)
//                Assert.assertEquals(100, height)
//            }
//
//            val bitmap1 = tileDecoder
//                .decode(IntRectCompat(100, 200, 300, 300), 4)!!
//                .let { it as AndroidTileBitmap }
//                .bitmap!!
//            bitmap1.apply {
//                Assert.assertEquals(50, width)
//                Assert.assertEquals(25, height)
//            }
//        } finally {
//            runBlocking(Dispatchers.Main) {
//                tileDecoder.destroy("test")
//            }
//        }
//
//        val imageSource2 = ImageSource.fromAsset(context, "sample_exif_girl_rotate_90.jpg")
//        val exifOrientation2 = imageSource2.decodeExifOrientation()
//            .getOrThrow().let { it as AndroidExifOrientation }
//            .apply {
//                Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_90, this.exifOrientation)
//            }
//        val imageInfo2 = imageSource2.decodeImageInfo().getOrThrow().let {
//            exifOrientation2.applyToImageInfo(it)
//        }
//        val tileDecoder2 = AndroidTileDecoder(
//            logger = logger,
//            imageSource = imageSource2,
//            imageInfo = imageInfo2,
//            exifOrientation = exifOrientation2,
//            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
//        )
//        val bitmap2: Bitmap
//        try {
//            bitmap2 = tileDecoder2
//                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
//                .let { it as AndroidTileBitmap }
//                .bitmap!!
//            bitmap2.apply {
//                Assert.assertEquals(200, width)
//                Assert.assertEquals(100, height)
//            }
//        } finally {
//            runBlocking(Dispatchers.Main) {
//                tileDecoder2.destroy("test")
//            }
//        }
//        val bitmapFinger = produceFingerPrint(bitmap)
//        val bitmap2Finger = produceFingerPrint(bitmap2)
//        val hanming2 = hammingDistance(bitmapFinger, bitmap2Finger)
//        Assert.assertTrue(hanming2 <= 2)
//
//        val exifOrientation3 = ExifInterface.ORIENTATION_UNDEFINED.apply {
//            Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, this)
//        }.let { AndroidExifOrientation(it) }
//        val imageInfo3 = imageSource2.decodeImageInfo().getOrThrow().let {
//            exifOrientation3.applyToImageInfo(it)
//        }
//        val tileDecoder3 = AndroidTileDecoder(
//            logger = logger,
//            imageSource = imageSource2,
//            imageInfo = imageInfo3,
//            exifOrientation = exifOrientation3,
//            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
//        )
//        val bitmap3: Bitmap
//        try {
//            bitmap3 = tileDecoder3
//                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
//                .let { it as AndroidTileBitmap }
//                .bitmap!!
//            bitmap3.apply {
//                Assert.assertEquals(200, width)
//                Assert.assertEquals(100, height)
//            }
//        } finally {
//            runBlocking(Dispatchers.Main) {
//                tileDecoder3.destroy("test")
//            }
//        }
//        val bitmap3Finger = produceFingerPrint(bitmap3)
//        val hanming3 = hammingDistance(bitmapFinger, bitmap3Finger)
//        Assert.assertTrue(hanming3 > 2)
//    }
}