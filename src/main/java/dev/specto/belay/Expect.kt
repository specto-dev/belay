package dev.specto.belay

import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_OF_TYPE
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

/**
 * The entry point for all expectation calls.
 *
 * The suggested usage is to create a top-level, internal variable, for example named "expect", and
 * use it throughout the project:
 *
 * ```
 * val expect = Expect()
 *
 * fun initApplication() {
 *     expect.onGlobalFail = object : GlobalExpectationHandler {
 *         override fun handleFail(exception: ExpectationException) {
 *             if (DEBUG) throw exception
 *             else log(exception)
 *         }
 *     }
 * }
 *
 * fun eat(banana: Banana) {
 *     expect(banana.isRipe)
 *     expect.isNotNull(banana.plant)
 *     …
 * }
 * ```
 *
 * @param onGlobalFail the default global expectation handler.
 */
public class Expect(
    onGlobalFail: GlobalExpectationHandler = Continue
) : GlobalExpectationReceiver(onGlobalFail) {

    /**
     * Invoked whenever a global expectation fails.
     *
     * This is the ideal place to decide whether expectations should be treated like assertions and
     * fail immediately or not, for example:
     *
     * ```
     * expect.onGlobalFail = object : GlobalExpectationHandler {
     *     override fun handleFail(exception: ExpectationException) {
     *         if (DEBUG) throw exception
     *         else log(exception)
     *     }
     * }
     * ```
     *
     * Called from the same thread as the expectation check.
     */
    public var onGlobalFail: GlobalExpectationHandler = onGlobalFail
        set(value) {
            handler = value
            field = value
        }

    /**
     * If [condition] is false, sends a [FailedExpectationException] to [onGlobalFail], otherwise
     * does nothing.
     *
     * @param condition the condition to check.
     * @param message the error message.
     */
    public operator fun invoke(
        condition: Boolean,
        message: String = MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
    ) {
        isTrue(condition, message)
    }

    /**
     * If [condition] is false, sends a [FailedExpectationException] to [onGlobalFail] and then
     * calls [onFail], otherwise does nothing.
     *
     * @param condition the condition to check.
     * @param message the error message.
     * @param onFail the function to call in place if condition is false.
     */
    public inline operator fun invoke(
        condition: Boolean,
        message: String = MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE,
        onFail: () -> Unit
    ) {
        if (!condition) {
            this.onGlobalFail.handleFail(FailedExpectationException(message))
            onFail()
        }
    }

    /**
     * Runs [block] in place. If any of its expectations fail, sends a [FailedExpectationException]
     * to [onGlobalFail] and then to [onFail], otherwise returns the value from [block].
     *
     * @param onFail the expectation handler.
     * @param block the block of code to call in place.
     */
    public inline operator fun <T> invoke(
        onFail: ContinueExpectationHandler,
        block: ContinueExpectationReceiver.() -> T
    ): T {
        val handler = object : ContinueExpectationHandler() {
            override fun handleFail(exception: ExpectationException) {
                this@Expect.onGlobalFail.handleFail(exception)
                onFail.handleFail(exception)
            }
        }
        return handler.runInternal(block)
    }

    /**
     * Runs [block] in place. If any of its expectations fail, sends a [FailedExpectationException]
     * to [onGlobalFail] and then to [onFail], otherwise returns the value from [block].
     *
     * When an expectation fails it is guaranteed to exit from [block], either through an early
     * return or a thrown exception. This enables the expectation calls to smart cast.
     *
     * @param onFail the expectation handler.
     * @param catchExceptions if true, all exceptions thrown by [block] will be caught and treated
     *   as expectation failures.
     * @param block the block of code to call in place.
     */
    public inline operator fun <T> invoke(
        onFail: ExitExpectationHandler<T>,
        catchExceptions: Boolean = false,
        block: ExitExpectationReceiver<T>.() -> T
    ): T {
        val handler = object : ExitExpectationHandler<T>() {
            override fun handleFail(exception: ExpectationException): Nothing {
                this@Expect.onGlobalFail.handleFail(exception)
                onFail.handleFail(exception)
            }
        }
        return handler.runInternal(catchExceptions, block)
    }
}

/**
 * If [condition] is false, sends a [FailedExpectationException] to [Expect.onGlobalFail] and then
 * calls [onFail], otherwise does nothing.
 *
 * If [condition] is false this function is guaranteed to exit early, either through an early return
 * or a thrown exception. This enables smart casting.
 *
 * @param condition the condition to check.
 * @param message the error message.
 * @param onFail the function to call in place if condition is false.
 */
public inline fun Expect.isTrue(
    condition: Boolean,
    message: String = MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE,
    onFail: () -> Nothing
) {
    contract {
        callsInPlace(onFail, AT_MOST_ONCE)
        returns() implies condition
    }

    if (!condition) {
        this.onGlobalFail.handleFail(FailedExpectationException(message))
        onFail()
    }
}

/**
 * If [condition] is true, sends a [FailedExpectationException] to [Expect.onGlobalFail] and then
 * calls [onFail], otherwise does nothing.
 *
 * If [condition] is true this function is guaranteed to exit early, either through an early return
 * or a thrown exception. This enables smart casting.
 *
 * @param condition the condition to check.
 * @param message the error message.
 * @param onFail the function to call in place if condition is false.
 */
public inline fun Expect.isFalse(
    condition: Boolean,
    message: String = MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE,
    onFail: () -> Nothing
) {
    contract {
        callsInPlace(onFail, AT_MOST_ONCE)
        returns() implies !condition
    }

    isTrue(!condition, message, onFail)
}

/**
 * If [value] is null, sends a [FailedExpectationException] to [Expect.onGlobalFail] and then calls
 * [onFail], otherwise returns [value] cast to its non-null type.
 *
 * If [value] is null this function is guaranteed to exit early, either through an early return or
 * a thrown exception. As a result, [value] is smart cast to its non-null type.
 *
 * @param value the value to check.
 * @param message the error message.
 * @param onFail the function to call in place if value is null.
 */
public inline fun <T : Any> Expect.isNotNull(
    value: T?,
    message: String = MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL,
    onFail: () -> Nothing
): T {
    contract {
        callsInPlace(onFail, AT_MOST_ONCE)
        returns() implies (value != null)
    }

    isTrue(value != null, message, onFail)

    return value
}

/**
 * If [value] is not null, sends a [FailedExpectationException] to [Expect.onGlobalFail] and then
 * calls [onFail], otherwise does nothing.
 *
 * If [value] is not null this function is guaranteed to exit early, either through an early return
 * or a thrown exception.
 *
 * @param value the value to check.
 * @param message the error message.
 * @param onFail the function to call in place if value is not null.
 */
public inline fun Expect.isNull(
    value: Any?,
    message: String = MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL,
    onFail: () -> Nothing
) {
    contract {
        callsInPlace(onFail, AT_MOST_ONCE)
        returns() implies (value == null)
    }

    isTrue(value == null, message, onFail)
}

/**
 * If [value] is not of type [T], sends a [FailedExpectationException] to [Expect.onGlobalFail] and
 * then calls [onFail], otherwise returns [value] cast to [T].
 *
 * If [value] is not of type [T] this function is guaranteed to exit early, either through an early
 * return or a thrown exception.
 *
 * @param value the value to check.
 * @param message the error message.
 * @param onFail the function to call in place if value is not null.
 */
public inline fun <reified T> Expect.isType(
    value: Any?,
    message: String = MESSAGE_EXPECTED_VALUE_OF_TYPE,
    onFail: () -> Nothing
): T {
    contract {
        callsInPlace(onFail, AT_MOST_ONCE)
    }

    isTrue(value is T, message, onFail)

    return value
}
