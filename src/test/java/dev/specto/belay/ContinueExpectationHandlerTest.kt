package dev.specto.belay

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ContinueExpectationHandlerTest : BaseTest() {

    @Test
    fun `no failed expectation does not trigger fail`() {
        var ran = false
        var fail = false
        val handler = object : ContinueExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                fail = true
            }
        }

        val returnValue = handler.runInternal {
            ran = true
            "hello"
        }

        assertTrue(ran)
        assertFalse(fail)
        assertEquals("hello", returnValue)
    }

    @Test
    fun `failed expectation triggers fail and continues`() {
        var fail = false
        val handler = object : ContinueExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                fail = true
            }
        }

        assertDoesNotExit {
            val returnValue = handler.runInternal {
                isTrue(false)
                "hello"
            }
            assertEquals("hello", returnValue)
        }

        assertTrue(fail)
    }
}
