package dev.specto.belay

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// TODO Add this to open source project once more fleshed out.
interface TestExpectationHandler {

    val exceptions: List<ExpectationException>

    fun assertNoFailures()

    fun assertFailed()
}

inline fun <reified T> TestExpectationHandler.assertFailedWith(cause: Throwable? = null) {
    val exception = exceptions.singleOrNull()
    assertNotNull(exception, "One exception expected but got ${exceptions.size}.")
    assertTrue(
        exception is T,
        "Expected exception of type ${T::class.java.canonicalName} but got" +
            " ${exception::class.java.canonicalName}"
    )
    assertEquals(cause, exception.cause)
}

inline fun <reified T> TestExpectationHandler.assertFailedWith(
    message: String,
    cause: Throwable? = null
) {
    assertFailedWith<T>(cause)
    assertEquals(message, exceptions.single().message)
}

class TestExpectationHandlerProvider {

    val exceptions: MutableList<ExpectationException> = mutableListOf()

    val testExpectationHandler = object : TestExpectationHandler {

        override val exceptions: List<ExpectationException> =
            this@TestExpectationHandlerProvider.exceptions

        override fun assertNoFailures() {
            assertTrue(
                exceptions.isEmpty(),
                "No expectation failures were expected but go ${exceptions.size}"
            )
        }

        override fun assertFailed() {
            assertNotNull(
                exceptions.singleOrNull(),
                "One exception expected but got ${exceptions.size}."
            )
        }
    }

    inner class TestContinueExpectationHandler : ContinueExpectationHandler(),
        TestExpectationHandler by testExpectationHandler {
        override fun handleFail(exception: ExpectationException) {
            this@TestExpectationHandlerProvider.exceptions.add(exception)
        }
    }

    inner class TestExitExpectationHandler<T>(val returnValue: T) : ExitExpectationHandler<T>(),
        TestExpectationHandler by testExpectationHandler {
        override fun handleFail(exception: ExpectationException): Nothing {
            this@TestExpectationHandlerProvider.exceptions.add(exception)
            returnFromBlock(returnValue)
        }
    }

    fun continueHandler() = TestContinueExpectationHandler()

    fun <T> exitHandler(returnValue: T) = TestExitExpectationHandler(returnValue)
}
