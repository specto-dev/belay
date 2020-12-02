# ![Belay — Robust Error-Handling for Kotlin and Android](banner.png)

[![CI](https://img.shields.io/github/workflow/status/specto-dev/belay/CI/main)](https://github.com/specto-dev/belay/actions?query=workflow%3ACI) [![Maven Central](https://img.shields.io/maven-central/v/dev.specto/belay)](https://search.maven.org/artifact/dev.specto/belay) ![code size](https://img.shields.io/github/languages/code-size/specto-dev/belay) ![Kind Speech](https://api.kindspeech.org/v1/badge?color=4e65d2)

*Code Complete: A Practical Handbook of Software Construction*, on error-handling techniques:

> **Consumer applications tend to favor robustness to correctness. Any result whatsoever is usually better than the software shutting down.** The word processor I'm using occasionally displays a fraction of a line of text at the bottom of the screen. If it detects that condition, do I want the word processor to shut down? No. I know that the next time I hit Page Up or Page Down, the screen will refresh and the display will be back to normal.

Belay is a Kotlin error-handling library which favors robustness. It serves two purposes:

- Detect errors early during development using assertions.
- Gracefully recover from errors when they occur in production.

## Installation

In your project's `build.gradle`:

```kotlin
dependencies {
    implementation("dev.specto:belay:0.3.0")
}
```

Declare a top level variable to run expectations:

```kotlin
val expect = Expect()
```

It can be named anything, but for the purposes of this documentation we'll assume it's named "expect".

Next, set the top level expectation handler as early as possible during your program's initialization:

```kotlin
fun main() {
    expect.onGlobalFail = object : GlobalExpectationHandler() {
        override fun handleFail(exception: ExpectationException) {
            if (DEBUG) throw exception
            else log(exception.stackTraceToString())
        }
    }
    
    // …
}
```

The global handler will be invoked when any expectations fail. Of course it's possible, and often desirable, to handle expectations individually or by subgroups, but the global handler is the ideal place to throw exceptions during development—effectively turning expectations into assertions—so that failures can be noticed and addressed immediately. In production it can be used to log all errors, regardless of how they are ultimately handled, so that they can be fixed at a later date.

:warning: Do not call `expect` from the global handler, it could cause an infinite loop if that expectation fails.

## Writing Expectations

The `expect` variable provides a host of utilities to write expectations and specify how they should be handled. They fall in 3 main categories.

### Global Expectations

Global expectations only invoke the global handler when they fail.

```kotlin
expect(condition, optionalErrorMessage) // shorthand for expect.isTrue(…)
expect.fail("error message", optionalCause)
expect.isTrue(condition, optionalErrorMessage)
expect.isFalse(condition, optionalErrorMessage)
expect.isNotNull(value, optionalErrorMessage)
expect.isNull(value, optionalErrorMessage)
expect.isType<Type>(value, optionalErrorMessage)
```

Unless the global handler interrupts the program, it will proceed even when these expectations fail. Therefore, they are meant to be used when the program can proceed even when the expectations fail. For example:

```kotlin
fun cleanup() {
    expect.isNotNull(configuration)
    configuration = null
}
```

### Locally-Handled Expectations

Locally-handled expectations invoke the global handler when they fail, and then a locally-defined error-handling function.

```kotlin
expect(condition, optionalErrorMessage) {
    // Handle the expectation failing.
    // Does not need to return or throw an exception.
}

expect.isTrue(condition, optionalErrorMessage) {
    // Handle the expectation failing.
    // Must return or throw an exception which enables smart casting.
    return
}
expect.isFalse(condition, optionalErrorMessage) { … }
val nonNullValue = expect.isNotNull(value, optionalErrorMessage) { … }
expect.isNull(value, optionalErrorMessage) { … }
val valueCastToType = expect.isType<Type>(value, optionalErrorMessage) { … }
```

A custom error message can be provided to all these functions.

This is great for one-off error-handling:

```kotlin
fun startTime(): Long {
    // …
    
    expect(startTime >= 0, "startTime was negative") {
        startTime = 0
    }
    
    return startTime
}

fun animate() {
    expect.isNotNull(animator) { return }
    
    // …
    animator.animate(…)
}
```

### Expectation Blocks

Often the same error-handling strategy can be used across individual functions or blocks of code. Expectation blocks make this easy.

```kotlin
expect(blockHandler) {
    fail("error message", optionalCause)
    isTrue(condition, optionalErrorMessage)
    isFalse(condition, optionalErrorMessage)
    isNotNull(value, optionalErrorMessage)
    isNull(value, optionalErrorMessage)
    isType<Type>(value, optionalErrorMessage)
}
```

A custom error message can be provided to all these functions.

Several block handlers are offered out of the box.

`Continue` does nothing when an expectation fails (besides invoking the global handler):

```kotlin
fun stop() = expect(onFail = Continue) {
    isTrue(isActive, "time machine was not active when stop was called")
    isNotNull(configuration)
    isNotNull(controller)
    
    isActive = false
    configuration = null
    controller = null
    // …
}
```

`Return` immediately returns a default value when an expectation fails:

```kotlin
fun startTime(): Long = expect(Return(0)) {
    // …
}
```

`ReturnLast` returns the last value returned or a default if no value has been returned yet:

```kotlin
fun pixelColor(x: Int, y: Int): Color = expect(ReturnLast(Color.BLACK)) {
    // …
}
```

`Throw` throws an exception when an expectation fails:

```kotlin
fun startRadiationTreatment() = expect(Throw) {
    // …
}
```

All the provided block handlers allow an arbitrary function to be executed when an expectation fails:

```kotlin
expect(Return { disableController() }) {
    // …
}
```

Block handlers which interrupt the program, like `Return`, `ReturnLast` and `Throw`, can also treat *exceptions* as failed expectations:

```kotlin
fun startTime(): Long = expect(Return(0), catchExceptions = true) {
    // All exceptions thrown by this function will be automatically caught
    // and handled by the expectation handler as a failed expectation.
}
```

It's also possible, and easy, to write your own expectation handler.

## Writing Expectation Handlers

Writing custom expectation handlers is particularly useful when the same custom logic needs to be reused across a program. There are two types of expectation handlers: those that may interrupt the program when an expectation fails, and those that definitely do.

Handlers who may interrupt the program when an expectation fails, like `Continue`, must extend `ContinueExpectationHandler`. Handlers that definitely interrupt the program, for example by returning early or throwing an exception, like `Return`, `ReturnLast` or `Throw`, should extend `ExitExpectationHandler`. This distinction serves to enable smart casting for `ExitExpectationHandler` expectations.

You've actually already implemented a handler which uses the same interface as `ContinueExpectationHandler`, the global handler. The `ExitExpectationHandler` interface is very similar, here's an example implementation:

```kotlin
class DisableControllerAndReturn<T>(
    returnValue: T,
    also: ((exception: ExpectationException) -> Unit)? = null
) : ExitExpectationHandler<T>() {

    private val controller: Controller by dependencyGraph

    override fun handleFail(exception: ExpectationException): Nothing {
        controller.disable(exception.message)
        also?.invoke(exception)
        returnFromBlock(returnValue)
    }
}
```

## Contributing

We love contributions! Check out our [contributing guidelines](CONTRIBUTING.md) and be sure to follow our [code of conduct](CODE_OF_CONDUCT.md).
