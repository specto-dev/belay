package dev.specto.belay

import dev.specto.belay.CaughtExpectationException.Companion.MESSAGE_EXCEPTION_OCCURRED

public interface ExpectationHandler<T> {
    public fun handleFail(exception: ExpectationException): T
}

/**
 * Unlike [ExitExpectationHandler], this base handler class is meant for handlers that do not
 * necessarily interrupt execution when an expectation fails.
 */
public abstract class ContinueExpectationHandler : ExpectationHandler<Unit> {
    internal inline fun <T> runInternal(block: ContinueExpectationReceiver.() -> T): T {
        return ContinueExpectationReceiver(this).block()
    }
}

public typealias GlobalExpectationHandler = ContinueExpectationHandler

/** Experimental. */
public open class BaseContinue(
    private val also: ((exception: ExpectationException) -> Unit)? = null
) : ContinueExpectationHandler() {
    override fun handleFail(exception: ExpectationException) {
        also?.invoke(exception)
    }
}

/**
 * Continues execution even when expectations fail.
 */
public object Continue : BaseContinue() {

    /**
     * Immediately executes [also] and then continues execution when an expectation fails.
     *
     * @param also the block of code to execute in place when an expectation fails.
     */
    public operator fun invoke(
        also: (exception: ExpectationException) -> Unit
    ): ContinueExpectationHandler = BaseContinue(also)
}

/**
 * Unlike [ContinueExpectationHandler], this base handler class is meant for handlers that always
 * interrupt execution when an expectation fails. This enables the expectation calls to smart cast.
 */
public abstract class ExitExpectationHandler<T> : ExpectationHandler<Nothing> {

    internal class ExpectationReturnExceptionInternal(val value: Any?) : Exception()

    internal open val onRun: ((returnValue: T) -> Unit)? = null

    @Suppress("TooGenericExceptionCaught")
    internal inline fun runInternal(
        catchExceptions: Boolean,
        block: ExitExpectationReceiver<T>.() -> T
    ): T {
        return catchReturnExceptionInternal {
            try {
                ExitExpectationReceiver(this).block()
            } catch (e: Exception) {
                if (e is ExpectationReturnExceptionInternal || !catchExceptions) throw e
                val exception = CaughtExpectationException(MESSAGE_EXCEPTION_OCCURRED, e)
                handleFail(exception)
            }
        }.also { returnValue ->
            onRun?.invoke(returnValue)
        }
    }

    internal inline fun catchReturnExceptionInternal(block: () -> T): T {
        return try {
            block()
        } catch (e: ExpectationReturnExceptionInternal) {
            @Suppress("UNCHECKED_CAST")
            e.value as T
        }
    }

    /**
     * Immediately returns [value] from the block of code executed by [Expect.invoke].
     */
    protected fun returnFromBlock(value: T): Nothing {
        throw ExpectationReturnExceptionInternal(value as Any?)
    }
}

/** Experimental. */
public open class BaseReturn<T>(
    private val value: T,
    private val also: ((exception: ExpectationException) -> Unit)? = null
) : ExitExpectationHandler<T>() {
    override fun handleFail(exception: ExpectationException): Nothing {
        also?.invoke(exception)
        returnFromBlock(value)
    }
}

/**
 * Immediately returns when an expectation fails.
 */
public object Return : BaseReturn<Unit>(Unit) {

    /**
     * Immediately executes [also] and then returns when an expectation fails.
     *
     * @param also the block of code to execute in place when an expectation fails.
     */
    public operator fun invoke(
        also: (exception: ExpectationException) -> Unit
    ): ExitExpectationHandler<Unit> = BaseReturn(Unit, also)

    /**
     * Immediately returns [value] when an expectation fails. Optionally executes a block of code
     * first.
     *
     * @param value the value to return when an expectation fails.
     * @param also the block of code to execute in place when an expectation fails.
     */
    public operator fun <T> invoke(
        value: T,
        also: ((exception: ExpectationException) -> Unit)? = null
    ): ExitExpectationHandler<T> = BaseReturn(value, also)
}

/**
 * Immediately returns when an expectation fails. Returns the last value returned through this
 * handler, or [defaultValue] if no value has been returned yet.
 *
 * @param value the value to return when an expectation fails and no other value has been returned
 *   yet.
 * @param also the block of code to execute in place when an expectation fails.
 */
public class ReturnLast<T>(
    private val defaultValue: T,
    private val also: ((exception: ExpectationException) -> Unit)? = null
) : ExitExpectationHandler<T>() {

    private var last: T? = null
    private var hasLast: Boolean = false

    override val onRun: ((returnValue: T) -> Unit)? = {
        last = it
        hasLast = true
    }

    override fun handleFail(exception: ExpectationException): Nothing {
        also?.invoke(exception)
        @Suppress("UNCHECKED_CAST")
        returnFromBlock(if (hasLast) last as T else defaultValue)
    }
}

/** Experimental. */
public open class BaseThrow<T>(
    private val exceptionFactory: ((exception: ExpectationException) -> Exception)? = null,
    private val also: ((exception: ExpectationException) -> Unit)? = null
) : ExitExpectationHandler<T>() {
    override fun handleFail(exception: ExpectationException): Nothing {
        also?.invoke(exception)
        throw exceptionFactory?.invoke(exception) ?: exception
    }
}

/**
 * Throws an [ExpectationException] when an expectation fails.
 */
public object Throw : BaseThrow<Unit>() {

    /**
     * Immediately executes [also] and then throws an [ExpectationException] when an expectation
     * fails.
     *
     * @param also the block of code to execute in place when an expectation fails.
     */
    public operator fun invoke(
        also: (exception: ExpectationException) -> Unit
    ): ExitExpectationHandler<Unit> = BaseThrow(also = also)

    /**
     * Throws an exception when an expectation fails. Optionally executes a block of code first.
     *
     * @param exceptionFactory a function to generate the exception to throw when an expectation
     *   fails.
     * @param also the block of code to execute in place when an expectation fails.
     */
    public operator fun <T> invoke(
        exceptionFactory: ((exception: ExpectationException) -> Exception)? = null,
        also: ((exception: ExpectationException) -> Unit)? = null
    ): ExitExpectationHandler<T> = BaseThrow(exceptionFactory, also)
}
