package dev.specto.belay

import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_OF_TYPE
import dev.specto.belay.TestExpectationHandlerProvider.TestExitExpectationHandler
import org.junit.Before
import org.junit.Test

class ExitExpectationReceiverTest : BaseTest() {

    private lateinit var testHandler: TestExitExpectationHandler<String>

    @Before
    fun before() {
        testHandler = testExpectationHandlerProvider.exitHandler("error")
    }

    @Test
    fun `fail triggers fail`() {
        assertExits {
            ExitExpectationReceiver(testHandler).fail("fail")
        }
        testHandler.assertFailedWith<FailedExpectationException>("fail")
    }

    @Test
    fun `true condition does not trigger fail for isTrue`() {
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isTrue(true)
        }
        testHandler.assertNoFailures()
    }

    @Test
    fun `false condition triggers fail for isTrue`() {
        assertExits {
            ExitExpectationReceiver(testHandler).isTrue(false)
        }
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
        )
    }

    @Test
    fun `isTrue smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isTrue(variable != null)
            variable.capitalize()
        }
    }

    @Test
    fun `false condition does not trigger fail for isFalse`() {
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isFalse(false)
        }
        testHandler.assertNoFailures()
    }

    @Test
    fun `true condition triggers fail for isFalse`() {
        assertExits {
            ExitExpectationReceiver(testHandler).isFalse(true)
        }
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
        )
    }

    @Test
    fun `isFalse smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isFalse(variable == null)
            variable.capitalize()
        }
    }

    @Test
    fun `non-null value does not trigger fail for isNotNull`() {
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isNotNull("not null")
        }
        testHandler.assertNoFailures()
    }

    @Test
    fun `null value triggers fail for isNotNull`() {
        assertExits {
            ExitExpectationReceiver(testHandler).isNotNull(null as String?)
        }
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
        )
    }

    @Test
    fun `isNotNull smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isNotNull(variable)
            variable.capitalize()
        }
    }

    @Test
    fun `isNotNull return value is cast to non-null type`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            val cast = ExitExpectationReceiver(testHandler).isNotNull(variable)
            cast.capitalize()
        }
    }

    @Test
    fun `null value does not trigger fail for isNull`() {
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isNull(null)
        }
        testHandler.assertNoFailures()
    }

    @Test
    fun `non-null value triggers fail for isNull`() {
        assertExits {
            ExitExpectationReceiver(testHandler).isNull("not null")
        }
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
        )
    }

    @Test
    fun `correct type does not trigger fail for isType`() {
        assertDoesNotExit {
            ExitExpectationReceiver(testHandler).isType<String>("hello")
        }
        testHandler.assertNoFailures()
    }

    @Test
    fun `wrong type triggers fail for isType`() {
        assertExits {
            ExitExpectationReceiver(testHandler).isType<String>(false)
        }
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_OF_TYPE
        )
    }

    @Test
    fun `isType return value is cast to type`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            val cast = ExitExpectationReceiver(testHandler).isType<String>(variable)
            cast.capitalize()
        }
    }
}
