package dev.specto.belay

/**
 * The base class for expectation exceptions.
 */
public abstract class ExpectationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    override val message: String = super.message!!
}

/**
 * Indicates that an expectation call (not an automatically caught exception) failed.
 */
public class FailedExpectationException(
    message: String,
    cause: Throwable? = null
) : ExpectationException(message, cause) {
    public companion object {
        public const val MESSAGE_EXPECTATION_FAILED: String = "An expectation failed."
        public const val MESSAGE_EXPECTED_CONDITION_FALSE_BUT_TRUE: String =
            "Expected condition to be false but was true."
        public const val MESSAGE_EXPECTED_CONDITION_TRUE_BUT_FALSE: String =
            "Expected condition to be true but was false."
        public const val MESSAGE_EXPECTED_VALUE_NON_NULL_BUT_NULL: String =
            "Expected value to be non-null but was null."
        public const val MESSAGE_EXPECTED_VALUE_NULL_BUT_NON_NULL: String =
            "Expected value to be null but was non-null."
        public const val MESSAGE_EXPECTED_VALUE_OF_TYPE: String =
            "Expected value to be of a different type than is was."
    }
}

/**
 * Indicates that an exception was caught and treated as an expectation failure.
 */
public class CaughtExpectationException(
    message: String,
    cause: Throwable? = null
) : ExpectationException(message, cause) {
    public companion object {
        public const val MESSAGE_EXCEPTION_OCCURRED: String = "An exception occurred."
    }
}
