package dev.specto.belay

import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test

class ThrowTest : BaseTest() {

    @Test
    fun `runInternal throws an ExpectationException when an expectation fails`() {
        assertFailsWith<ExpectationException> {
            Throw.runInternal(false) {
                isTrue(false)
            }
        }
    }

    @Test
    fun `runInternal runs also function when an expectation fails`() {
        var ran = false
        assertFailsWith<ExpectationException> {
            Throw { ran = true }.runInternal(false) {
                isTrue(false)
            }
        }
        assertTrue(ran)
    }

    @Test
    fun `runInternal throws exception from factory when an expectation fails`() {
        val handler = Throw<String>(exceptionFactory = { IllegalStateException(it.message) })
        assertFailsWith<IllegalStateException> {
            handler.runInternal(false) {
                isTrue(false)
                "success"
            }
        }
    }
}
