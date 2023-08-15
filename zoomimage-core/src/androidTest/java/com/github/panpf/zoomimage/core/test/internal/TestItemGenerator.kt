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

data class Item5(
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    override val expected: IntRectCompat
) : A<IntRectCompat> {
    override fun getMessage(containerSize: IntSizeCompat): String {
        return "Item5(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}" +
                ")"
    }

    override fun getBuildExpression(r: IntRectCompat): String {
        return "Item5(" +
                "IntSizeCompat(${contentSize.width}, ${contentSize.height}), " +
                "${contentScale.name}, " +
                "${alignment.name}, " +
                "IntRectCompat(${r.left}, ${r.top}, ${r.right}, ${r.bottom})" +
                ")"
    }
}

data class Item6(
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    override val expected: IntOffsetCompat
) : A<IntOffsetCompat> {
    override fun getMessage(containerSize: IntSizeCompat): String {
        return "Item6(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}" +
                ")"
    }

    override fun getBuildExpression(r: IntOffsetCompat): String {
        return "Item6(" +
                "IntSizeCompat(${contentSize.width}, ${contentSize.height}), " +
                "${contentScale.name}, " +
                "${alignment.name}, " +
                "IntOffsetCompat(${r.x}, ${r.y})" +
                ")"
    }
}

data class Item7(
    val contentSize: IntSizeCompat,
    val contentScale: ContentScaleCompat,
    val alignment: AlignmentCompat,
    val scale: Float,
    override val expected: IntRectCompat
) : A<IntRectCompat> {
    override fun getMessage(containerSize: IntSizeCompat): String {
        return "Item7(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "alignment=${alignment.name}, " +
                "scale=${scale}" +
                ")"
    }

    override fun getBuildExpression(r: IntRectCompat): String {
        return "Item7(" +
                "IntSizeCompat(${contentSize.width}, ${contentSize.height}), " +
                "${contentScale.name}, " +
                "${alignment.name}, " +
                "${scale}f, " +
                "IntRectCompat(${r.left}, ${r.top}, ${r.right}, ${r.bottom})" +
                ")"
    }
}

interface A<R> {
    val expected: R
    fun getMessage(containerSize: IntSizeCompat): String
    fun getBuildExpression(r: R): String
}

fun <R, T : A<R>> List<T>.check(
    containerSize: IntSizeCompat,
    printBatchBuildExpression: Boolean = false,
    computeResult: (T) -> R
) {
    if (printBatchBuildExpression) {
        this.map { item ->
            val result = computeResult(item)
            item.getBuildExpression(result)
        }.apply {
            Assert.fail(joinToString(separator = ", \n", postfix = ","))
        }
    }
    this.forEach { item ->
        val result = computeResult(item)
        Assert.assertEquals(
            /* message = */ item.getMessage(containerSize),
            /* expected = */ item.expected,
            /* actual = */ result,
        )
    }
}

fun <R, T : A<R>> List<T>.printlnBatchBuildExpression(computeExpected: (T) -> R): List<T> {
    this.map { item ->
        val expected = computeExpected(item)
        item.getBuildExpression(expected)
    }.apply {
        Assert.fail(joinToString(separator = ", \n", postfix = ","))
    }
    return this
}

fun <P1, P2, P3, R, T : A<R>> printlnBatchBuildExpression(
    p1s: List<P1>,
    p2s: List<P2>,
    p3s: List<P3>,
    buildItem: (P1, P2, P3) -> T,
    computeExpected: (T) -> R
) {
    val paramList = mutableListOf<T>()
    p1s.forEach { p1 ->
        p2s.forEach { p2 ->
            p3s.forEach { p3 ->
                paramList.add(buildItem(p1, p2, p3))
            }
        }
    }
    val buildExpression =
        paramList.joinToString(separator = ", \n", prefix = "\n", postfix = ",") { item ->
            val expected = computeExpected(item)
            item.getBuildExpression(expected)
        }
    Assert.fail(buildExpression)
}

fun <P1, P2, P3, P4, R, T : A<R>> printlnBatchBuildExpression(
    p1s: List<P1>,
    p2s: List<P2>,
    p3s: List<P3>,
    p4s: List<P4>,
    buildItem: (P1, P2, P3, P4) -> T,
    computeExpected: (T) -> R
) {
    val paramList = mutableListOf<T>()
    p1s.forEach { p1 ->
        p2s.forEach { p2 ->
            p3s.forEach { p3 ->
                p4s.forEach { p4 ->
                    paramList.add(buildItem(p1, p2, p3, p4))
                }
            }
        }
    }
    val buildExpression =
        paramList.joinToString(separator = ", \n", prefix = "\n", postfix = ",") { item ->
            val expected = computeExpected(item)
            item.getBuildExpression(expected)
        }
    Assert.fail(buildExpression)
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