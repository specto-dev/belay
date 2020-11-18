package dev.specto.belay

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ExitExpectationHandlerTest : BaseTest() {

    @Test
    fun `no failed expectation does not trigger fail`() {
        var ran = false
        var fail = false
        val handler = object : ExitExpectationHandler<String>() {
            override fun handleFail(exception: ExpectationException): Nothing {
                fail = true
                returnFromBlock("error")
            }
        }

        val returnValue = handler.runInternal(false) {
            ran = true
            "hello"
        }

        assertTrue(ran)
        assertFalse(fail)
        assertEquals("hello", returnValue)
    }

    @Test
    fun `failed expectation triggers fail and exits`() {
        val handler = object : ExitExpectationHandler<String>() {
            override fun handleFail(exception: ExpectationException): Nothing {
                returnFromBlock("error")
            }
        }

        val returnValue = handler.runInternal(false) {
            isTrue(false)
            "hello"
        }

        assertEquals("error", returnValue)
    }
}
