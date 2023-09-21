package com.github.panpf.zoomimage.core.test.internal

import com.github.panpf.zoomimage.util.IntSizeCompat
import org.junit.Assert

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

fun <P1, P2, R, T : A<R>> printlnBatchBuildExpression(
    p1s: List<P1>,
    p2s: List<P2>,
    buildItem: (P1, P2) -> T,
    computeExpected: (T) -> R
) {
    val paramList = mutableListOf<T>()
    p1s.forEach { p1 ->
        p2s.forEach { p2 ->
            paramList.add(buildItem(p1, p2))
        }
    }
    val buildExpression =
        paramList.joinToString(separator = ", \n", prefix = "\n", postfix = ",") { item ->
            val expected = computeExpected(item)
            item.getBuildExpression(expected)
        }
    Assert.fail(buildExpression)
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

fun <P1, P2, P3, P4, P5, R, T : A<R>> printlnBatchBuildExpression(
    p1s: List<P1>,
    p2s: List<P2>,
    p3s: List<P3>,
    p4s: List<P4>,
    p5s: List<P5>,
    buildItem: (P1, P2, P3, P4, P5) -> T,
    computeExpected: (T) -> R
) {
    val paramList = mutableListOf<T>()
    p1s.forEach { p1 ->
        p2s.forEach { p2 ->
            p3s.forEach { p3 ->
                p4s.forEach { p4 ->
                    p5s.forEach { p5 ->
                        paramList.add(buildItem(p1, p2, p3, p4, p5))
                    }
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

fun <P1, P2, P3, P4, P5, P6, R, T : A<R>> printlnBatchBuildExpression(
    p1s: List<P1>,
    p2s: List<P2>,
    p3s: List<P3>,
    p4s: List<P4>,
    p5s: List<P5>,
    p6s: List<P6>,
    buildItem: (P1, P2, P3, P4, P5, P6) -> T,
    computeExpected: (T) -> R
) {
    val paramList = mutableListOf<T>()
    p1s.forEach { p1 ->
        p2s.forEach { p2 ->
            p3s.forEach { p3 ->
                p4s.forEach { p4 ->
                    p5s.forEach { p5 ->
                        p6s.forEach { p6 ->
                            paramList.add(buildItem(p1, p2, p3, p4, p5, p6))
                        }
                    }
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