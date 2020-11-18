package dev.specto.belay

import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_OF_TYPE
import dev.specto.belay.TestExpectationHandlerProvider.TestContinueExpectationHandler
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test

class ExpectTest : BaseTest() {

    private lateinit var testGlobalHandler: TestContinueExpectationHandler
    private lateinit var expect: Expect

    @Before
    fun before() {
        testGlobalHandler = testExpectationHandlerProvider.continueHandler()
        expect = Expect(testGlobalHandler)
    }

    @Test
    fun `invoke true condition does not trigger fail`() {
        expect(true)
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `invoke false condition triggers fail`() {
        expect(false)
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
        )
    }

    @Test
    fun `invoke false condition triggers fail with custom message`() {
        expect(false, "foobar")
        testGlobalHandler.assertFailedWith<FailedExpectationException>("foobar")
    }

    @Test
    fun `invoke true condition does not run handlers`() {
        var onFailFunctionRan = false
        expect(true) {
            onFailFunctionRan = true
        }
        assertFalse(onFailFunctionRan)
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `invoke false condition runs handlers`() {
        var onFailFunctionRan = false
        expect(false) {
            onFailFunctionRan = true
        }
        assertTrue(onFailFunctionRan)
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
        )
    }

    @Test
    fun `invoke false condition runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false
        var onFailFunctionRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
                assertFalse(onFailFunctionRan)
            }
        }

        expect(false) {
            assertTrue(globalExpectationHandlerRan)
            onFailFunctionRan = true
        }

        assertTrue(onFailFunctionRan)
    }

    @Test
    fun `invoke onFail function can return early`() {
        assertReturnsEarly {
            expect(false) {
                return
            }
        }
    }

    @Test
    fun `invoke for continue runs receiver block once`() {
        var blockRan = false
        val testHandler = testExpectationHandlerProvider.continueHandler()
        expect(testHandler) {
            assertFalse(blockRan)
            blockRan = true
        }
        assertTrue(blockRan)
    }

    @Test
    fun `invoke for exit runs receiver block once`() {
        var blockRan = false
        val testHandler = testExpectationHandlerProvider.exitHandler("error")
        expect(testHandler) {
            assertFalse(blockRan)
            blockRan = true
            "success"
        }
        assertTrue(blockRan)
    }

    @Test
    fun `invoke for exit returns block value when no expectations fail`() {
        val testHandler = testExpectationHandlerProvider.exitHandler("error")
        val returnValue = expect(testHandler) {
            isTrue(true)
            "success"
        }
        assertEquals("success", returnValue)
    }

    @Test
    fun `invoke for exit returns handler default value when expectations fail`() {
        val testHandler = testExpectationHandlerProvider.exitHandler("error")
        val returnValue = expect(testHandler) {
            isTrue(false)
            "success"
        }
        assertEquals("error", returnValue)
    }

    @Test
    fun `invoke for continue runs global expectation handler before local handler`() {
        var globalExpectationHandlerRan = false
        var localHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
                assertFalse(localHandlerRan)
            }
        }

        val localHandler = object : ContinueExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                assertTrue(globalExpectationHandlerRan)
                localHandlerRan = true
            }
        }

        expect(localHandler) {
            isTrue(false)
        }

        assertTrue(localHandlerRan)
    }

    @Test
    fun `invoke for exit runs global expectation handler before local handler`() {
        var globalExpectationHandlerRan = false
        var localHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
                assertFalse(localHandlerRan)
            }
        }

        val localHandler = object : ExitExpectationHandler<String>() {
            override fun handleFail(exception: ExpectationException): Nothing {
                assertTrue(globalExpectationHandlerRan)
                localHandlerRan = true
                returnFromBlock("error")
            }
        }

        expect(localHandler) {
            isTrue(false)
            "success"
        }

        assertTrue(localHandlerRan)
    }

    @Test
    fun `isTrue true condition does not run handlers`() {
        assertDoesNotExit {
            expect.isTrue(true) {
                return
            }
        }
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `isTrue false condition runs handlers`() {
        assertReturnsEarly {
            expect.isTrue(false) {
                return
            }
        }
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
        )
    }

    @Test
    fun `isTrue false condition runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
            }
        }

        assertReturnsEarly {
            expect.isTrue(false) {
                assertTrue(globalExpectationHandlerRan)
                return
            }
        }
    }

    @Test
    fun `isTrue smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            expect.isTrue(variable != null) {
                return
            }
            variable.capitalize()
        }
    }

    @Test
    fun `isFalse false condition does not run handlers`() {
        assertDoesNotExit {
            expect.isFalse(false) {
                return
            }
        }
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `isFalse true condition runs handlers`() {
        assertReturnsEarly {
            expect.isFalse(true) {
                return
            }
        }
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
        )
    }

    @Test
    fun `isFalse true condition runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
            }
        }

        assertReturnsEarly {
            expect.isFalse(true) {
                assertTrue(globalExpectationHandlerRan)
                return
            }
        }
    }

    @Test
    fun `isFalse smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            expect.isFalse(variable == null) {
                return
            }
            variable.capitalize()
        }
    }

    @Test
    fun `isNotNull not null value does not run handlers`() {
        assertDoesNotExit {
            expect.isNotNull("not null") {
                return
            }
        }
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `isNotNull null value runs handlers`() {
        assertReturnsEarly {
            expect.isNotNull(null as String?) {
                return
            }
        }
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
        )
    }

    @Test
    fun `isNotNull null value runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
            }
        }

        assertReturnsEarly {
            expect.isNotNull(null as String?) {
                assertTrue(globalExpectationHandlerRan)
                return
            }
        }
    }

    @Test
    fun `isNotNull smart cast`() {
        val variable: String? = "hello"
        assertDoesNotExit {
            expect.isNotNull(variable) {
                return
            }
            variable.capitalize()
        }
    }

    @Test
    fun `isNotNull return value is cast to non-null type`() {
        assertDoesNotExit {
            val cast = expect.isNotNull("hello") {
                return
            }
            cast.capitalize()
        }
    }

    @Test
    fun `isNull not null value does not run handlers`() {
        assertDoesNotExit {
            expect.isNull(null) {
                return
            }
        }
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `isNull not null value runs handlers`() {
        assertReturnsEarly {
            expect.isNull("not null") {
                return
            }
        }
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
        )
    }

    @Test
    fun `isNull not null value runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
            }
        }

        assertReturnsEarly {
            expect.isNull("not null") {
                assertTrue(globalExpectationHandlerRan)
                return
            }
        }
    }

    @Test
    fun `isType correct type does not run handlers`() {
        assertDoesNotExit {
            expect.isType<String>("hello") {
                return
            }
        }
        testGlobalHandler.assertNoFailures()
    }

    @Test
    fun `isType wrong type runs handlers`() {
        assertReturnsEarly {
            expect.isType<String>(false) {
                return
            }
        }
        testGlobalHandler.assertFailedWith<FailedExpectationException>(
            MESSAGE_EXPECTED_VALUE_OF_TYPE
        )
    }

    @Test
    fun `isType wrong type runs global expectation handler before onFail function`() {
        var globalExpectationHandlerRan = false

        expect.onGlobalFail = object : GlobalExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                globalExpectationHandlerRan = true
            }
        }

        assertReturnsEarly {
            expect.isType<String>(false) {
                assertTrue(globalExpectationHandlerRan)
                return
            }
        }
    }

    @Test
    fun `isType return value is cast to correct type`() {
        assertDoesNotExit {
            val cast = expect.isType<String>("hello" as String?) {
                return
            }
            cast.capitalize()
        }
    }
}
