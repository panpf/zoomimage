package com.github.panpf.zoomimage.core.test.internal

import com.github.panpf.zoomimage.util.AlignmentCompat
import com.github.panpf.zoomimage.util.ContentScaleCompat
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.name
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert

data class Item(
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val expected: IntRectCompat
) {
    fun getMessage(containerSize: IntSizeCompat, contentSize: IntSizeCompat): String {
        return "Item(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}" +
                ")"
    }

    fun getMessage(containerSize: IntSizeCompat, contentSize: IntSizeCompat, scale: Float): String {
        return "Item(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "scale=${scale}" +
                ")"
    }
}

fun List<Item>.printlnExpectedMessage(computeExpected: (Item) -> IntRectCompat): List<Item> {
    this.map {
        val expected = computeExpected(it)
        "Item(" +
                "ContentScaleCompat.${it.contentScale.name}, " +
                "AlignmentCompat.${it.alignment.name}, " +
                "IntRectCompat(${expected.run { "${left},${top},${right},${bottom}f" }})" +
                ")"
    }.apply {
        Assert.fail(joinToString(separator = ", \n", postfix = ","))
    }
    return this
}

data class Item2(
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val expected: TransformOriginCompat
) {
    fun getMessage(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        containerOrigin: TransformOriginCompat
    ): String {
        return "Item2(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "containerOrigin=${containerOrigin.toShortString()}" +
                ")"
    }
}

fun List<Item2>.printlnExpectedMessage2(computeExpected: (Item2) -> TransformOriginCompat): List<Item2> {
    this.map {
        val expected = computeExpected(it)
        "Item2(" +
                "ContentScaleCompat.${it.contentScale.name}, " +
                "AlignmentCompat.${it.alignment.name}, " +
                "Origin(${expected.run { "${pivotFractionX},${pivotFractionY}f" }})" +
                ")"
    }.apply {
        Assert.fail(joinToString(separator = ", \n", postfix = ","))
    }
    return this
}

data class Item3(
    val offset: IntOffsetCompat,
    val expected: IntRectCompat
) {
    fun getMessage(
        containerSize: IntSizeCompat,
        contentSize: IntSizeCompat,
        contentScale: ContentScaleCompat,
        alignment: AlignmentCompat,
        scale: Float
    ): String {
        return "Item3(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "scale=$scale, " +
                "offset=${offset.toShortString()}" +
                ")"
    }
}

fun List<Item3>.printlnExpectedMessage3(computeExpected: (Item3) -> IntRectCompat): List<Item3> {
    this.map {
        val visibleRect = computeExpected(it)
        "IntOffsetCompat(" +
                "${it.offset.x}, ${it.offset.y}) to IntRectCompat(${visibleRect.left}, " +
                "${visibleRect.top}, " +
                "${visibleRect.right}, " +
                "${visibleRect.bottom}" +
                ")"
    }.apply {
        Assert.fail(joinToString(separator = ", \n"))
    }
    return this
}