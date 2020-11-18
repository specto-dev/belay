package dev.specto.belay

import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Asserts that [block] exits with a non-local return.
 */
inline fun assertReturnsEarly(block: () -> Unit) {
    val (exited, threw) = runForExitResult(block)
    assertTrue(exited, "The block did not exit.")
    assertFalse(threw, "The block threw an exception.")
}

/**
 * Asserts that [block] exits, either with a non-local return or by throwing an exception.
 */
inline fun assertExits(block: () -> Unit) {
    val (exited) = runForExitResult(block)
    assertTrue(exited, "The block did not exit.")
}

/**
 * Asserts that [block] does not exit with an exception or with a return.
 */
inline fun assertDoesNotExit(block: () -> Unit) {
    val (exited) = runForExitResult(block)
    assertFalse(exited, "The block exited.")
}

data class ExitResult(val exited: Boolean, val threw: Boolean)

/**
 * Runs [block] and returns whether it exited prematurely and whether it threw an exception. A
 * premature exit without an exception thrown is assumed to be a non-local return.
 */
inline fun runForExitResult(block: () -> Unit): ExitResult {
    var exited = true
    var threw = false
    try {
        block()
        exited = false
    } catch (e: Throwable) {
        threw = true
    } finally {
        return ExitResult(exited, threw)
    }
}
