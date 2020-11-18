package dev.specto.belay

import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTATION_FAILED
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
import dev.specto.belay.FailedExpectationException.Companion.MESSAGE_EXPECTED_VALUE_OF_TYPE
import kotlin.contracts.contract

/**
 * For handlers that do not necessarily interrupt execution when an expectation fails.
 */
public open class ContinueExpectationReceiver internal constructor(
    protected var handler: ContinueExpectationHandler
) {

    /**
     * Sends a [FailedExpectationException] to [handler].
     *
     * This is particularly useful to funnel caught exceptions through the framework:
     *
     * ```
     * try {
     *     // …
     * } catch (e: SomeException) {
     *     expect.fail("some error occurred", e)
     *     return
     * }
     * ```
     *
     * @param cause the cause of this failure.
     * @param message the error message.
     */
    public fun fail(message: String, cause: Throwable? = null) {
        val exception = FailedExpectationException(message, cause)
        handler.handleFail(exception)
    }

    /**
     * If [condition] is false, sends a [FailedExpectationException] to [handler], otherwise does
     * nothing.
     *
     * @param condition the condition to check.
     * @param message the error message.
     */
    public fun isTrue(
        condition: Boolean,
        message: String = MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
    ) {
        if (!condition) fail(message = message)
    }

    /**
     * If [condition] is true, sends a [FailedExpectationException] to [handler], otherwise does
     * nothing.
     *
     * @param condition the condition to check.
     * @param message the error message.
     */
    public fun isFalse(
        condition: Boolean,
        message: String = MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
    ) {
        if (condition) fail(message = message)
    }

    /**
     * If [value] is not null, sends a [FailedExpectationException] to [handler], otherwise does
     * nothing.
     *
     * @param value the value to check.
     * @param message the error message.
     */
    public fun isNotNull(
        value: Any?,
        message: String = MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
    ) {
        if (value == null) fail(message = message)
    }

    /**
     * If [value] is null, sends a [FailedExpectationException] to [handler], otherwise does
     * nothing.
     *
     * @param value the value to check.
     * @param message the error message.
     */
    public fun isNull(
        value: Any?,
        message: String = MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
    ) {
        if (value != null) fail(message = message)
    }

    /**
     * If [value] is not of type [T], sends a [FailedExpectationException] to [handler], otherwise
     * does nothing.
     *
     * @param value the value to check.
     * @param message the error message.
     */
    public inline fun <reified T> isType(value: Any?, message: String = MESSAGE_EXPECTED_VALUE_OF_TYPE) {
        if (value !is T) fail(message = message)
    }
}

public typealias GlobalExpectationReceiver = ContinueExpectationReceiver

/**
 * For handlers that always interrupt execution when an expectation fails. This enables the
 * expectation calls to smart cast.
 */
public class ExitExpectationReceiver<T>(private val handler: ExitExpectationHandler<T>) {

    /**
     * Sends a [FailedExpectationException] to [handler].
     *
     * @param cause the cause of this failure.
     * @param message the error message.
     */
    public fun fail(
        message: String = MESSAGE_EXPECTATION_FAILED,
        cause: Throwable? = null
    ): Nothing {
        val exception = FailedExpectationException(message, cause)
        handler.handleFail(exception)
    }

    /**
     * If [value] is not of type [T], sends a [FailedExpectationException] to [handler], otherwise
     * does nothing.
     *
     * @param value the value to check.
     * @param message the error message.
     */
    public inline fun <reified T> isType(
        value: Any?,
        message: String = MESSAGE_EXPECTED_VALUE_OF_TYPE
    ): T {
        if (value !is T) fail(message = message)

        return value
    }
}

/**
 * If [condition] is false, sends a [FailedExpectationException] to
 * [ExitExpectationReceiver.handler], otherwise does nothing.
 *
 * @param condition the condition to check.
 * @param message the error message.
 */
public fun ExitExpectationReceiver<*>.isTrue(
    condition: Boolean,
    message: String = MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE
) {
    contract {
        returns() implies condition
    }

    if (!condition) fail(message = message)
}

/**
 * If [condition] is true, sends a [FailedExpectationException] to
 * [ExitExpectationReceiver.handler], otherwise does nothing.
 *
 * @param condition the condition to check.
 * @param message the error message.
 */
public fun ExitExpectationReceiver<*>.isFalse(
    condition: Boolean,
    message: String = MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE
) {
    contract {
        returns() implies !condition
    }

    if (condition) fail(message = message)
}

/**
 * If [value] is not null, sends a [FailedExpectationException] to
 * [ExitExpectationReceiver.handler], otherwise does nothing.
 *
 * @param value the value to check.
 * @param message the error message.
 */
public fun <T : Any> ExitExpectationReceiver<*>.isNotNull(
    value: T?,
    message: String = MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL
): T {
    contract {
        returns() implies (value != null)
    }

    if (value == null) fail(message = message)

    return value
}

/**
 * If [value] is null, sends a [FailedExpectationException] to [ExitExpectationReceiver.handler],
 * otherwise does nothing.
 *
 * @param value the value to check.
 * @param message the error message.
 */
public fun ExitExpectationReceiver<*>.isNull(
    value: Any?,
    message: String = MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL
) {
    contract {
        returns() implies (value == null)
    }

    if (value != null) fail(message = message)
}
