package dev.specto.belay

import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_OF_TYPE
import dev.specto.belay.TestExpectationHandlerProvider.TestContinueExpectationHandler
import org.junit.Before
import org.junit.Test

class ContinueExpectationReceiverTest : BaseTest() {

    private lateinit var testHandler: TestContinueExpectationHandler

    @Before
    fun before() {
        testHandler = testExpectationHandlerProvider.continueHandler()
    }

    @Test
    fun `fail triggers fail`() {
        ContinueExpectationReceiver(testHandler).fail("error")
        testHandler.assertFailedWith<FailedExpectationException>("error")
    }

    @Test
    fun `true condition does not trigger fail for isTrue`() {
        ContinueExpectationReceiver(testHandler).isTrue(true)
        testHandler.assertNoFailures()
    }

    @Test
    fun `false condition triggers fail for isTrue`() {
        ContinueExpectationReceiver(testHandler).isTrue(false)
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
        )
    }

    @Test
    fun `false condition does not trigger fail for isFalse`() {
        ContinueExpectationReceiver(testHandler).isFalse(false)
        testHandler.assertNoFailures()
    }

    @Test
    fun `true condition triggers fail for isFalse`() {
        ContinueExpectationReceiver(testHandler).isFalse(true)
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
        )
    }

    @Test
    fun `non-null value does not trigger fail for isNotNull`() {
        ContinueExpectationReceiver(testHandler).isNotNull("not null")
        testHandler.assertNoFailures()
    }

    @Test
    fun `null value triggers fail for isNotNull`() {
        ContinueExpectationReceiver(testHandler).isNotNull(null)
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
        )
    }

    @Test
    fun `null value does not trigger fail for isNull`() {
        ContinueExpectationReceiver(testHandler).isNull(null)
        testHandler.assertNoFailures()
    }

    @Test
    fun `non-null value triggers fail for isNull`() {
        ContinueExpectationReceiver(testHandler).isNull("not null")
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
        )
    }

    @Test
    fun `correct type does not trigger fail for isType`() {
        ContinueExpectationReceiver(testHandler).isType<String>("hello")
        testHandler.assertNoFailures()
    }

    @Test
    fun `wrong type triggers fail for isType`() {
        ContinueExpectationReceiver(testHandler).isType<String>(false)
        testHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_OF_TYPE
        )
    }
}
