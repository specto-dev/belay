package dev.specto.belay

import org.junit.Before

abstract class BaseTest {

    protected lateinit var testExpectationHandlerProvider: TestExpectationHandlerProvider

    @Before
    fun baseBefore() {
        testExpectationHandlerProvider = TestExpectationHandlerProvider()
    }
}
