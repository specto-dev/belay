package dev.specto.belay

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class ReturnLastTest : BaseTest() {

    @Test
    fun `runInternal returns default value when the first run fails`() {
        val handler = ReturnLast("default")
        val returnValue = handler.runInternal(false) {
            isTrue(false)
            "success"
        }
        assertEquals("default", returnValue)
    }

    @Test
    fun `runInternal returns last value when an expectation fails`() {
        val handler = ReturnLast("default")
        handler.runInternal(false) { "success" }
        val returnValue = handler.runInternal(false) {
            isTrue(false)
            "another success"
        }
        assertEquals("success", returnValue)
    }

    @Test
    fun `runInternal runs also function when an expectation fails`() {
        var ran = false
        val handler = ReturnLast("default", { ran = true })
        handler.runInternal(false) {
            isTrue(false)
            "success"
        }
        assertTrue(ran)
    }
}
