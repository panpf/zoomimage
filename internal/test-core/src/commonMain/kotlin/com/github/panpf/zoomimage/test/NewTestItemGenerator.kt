package com.github.panpf.zoomimage.test

import kotlin.test.fail

interface Item<R> {
    val expected: R
    fun getMessage(): String
    fun getBuildExpression(r: R): String
}

fun <R, T : Item<R>> List<T>.printlnBatchBuildExpression(computeExpected: (T) -> R) {
    this.map { item ->
        val expected = computeExpected(item)
        item.getBuildExpression(expected)
    }.apply {
        fail(joinToString(separator = ", \n", postfix = ","))
    }
}

fun <P1, P2, R, T : Item<R>> printlnBatchBuildExpression(
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
    fail(buildExpression)
}

fun <P1, P2, P3, R, T : Item<R>> printlnBatchBuildExpression(
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
    fail(buildExpression)
}

fun <P1, P2, P3, P4, R, T : Item<R>> printlnBatchBuildExpression(
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
    fail(buildExpression)
}

fun <P1, P2, P3, P4, P5, R, T : Item<R>> printlnBatchBuildExpression(
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
    fail(buildExpression)
}

fun <P1, P2, P3, P4, P5, P6, R, T : Item<R>> printlnBatchBuildExpression(
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
    fail(buildExpression)
}