package com.github.panpf.zoomimage.test

import java.io.Closeable


/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [Closeable] resource.
 * @return the result of [block] function invoked on this resource.
 */
inline fun <T : Closeable?, R> T.useApply(block: T.() -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            apiVersionIsAtLeast(1, 1, 0) -> this.closeFinally(exception)
            this == null -> {}
            exception == null -> close()
            else ->
                try {
                    close()
                } catch (closeException: Throwable) {
                    // cause.addSuppressed(closeException) // ignored here
                }
        }
    }
}

/**
 * Closes this [Closeable], suppressing possible exception or error thrown by [Closeable.close] function when
 * it's being closed due to some other [cause] exception occurred.
 *
 * The suppressed exception is added to the list of suppressed exceptions of [cause] exception, when it's supported.
 */
@SinceKotlin("1.1")
@PublishedApi
internal fun Closeable?.closeFinally(cause: Throwable?) = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

/**
 * Constant check of api version used during compilation
 *
 * This function is evaluated at compile time to a constant value,
 * so there should be no references to it in other modules.
 *
 * The function usages are validated to have literal argument values.
 */
@PublishedApi
@SinceKotlin("1.2")
internal fun apiVersionIsAtLeast(major: Int, minor: Int, patch: Int) =
    KotlinVersion.CURRENT.isAtLeast(major, minor, patch)