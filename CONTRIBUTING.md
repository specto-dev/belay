# How to Contribute to Belay

Thank you for considering making a contribution! :partying_face:

## Testing

Belay has an extensive suite of tests. It uses *ktlint* to format code and *detekt* for static code analysis.

Run all checks:

```
./gradlew check
```

Please make sure that all checks are passing before proposing changes, and add or update tests whenever possible.

## Reporting bugs / making requests / asking questions

* Make sure your topic is not already covered by searching on GitHub under [Issues](https://github.com/specto-dev/belay/issues).

* If there is no existing issue on this topic, [open a new one](https://github.com/specto-dev/belay/issues/new). Be sure to include a title and clear description with as much relevant information as possible.

* For bugs, include a code sample or an executable test case demonstrating the expected behavior that is not occurring.

## Submitting changes

* Make sure all checks are passing by running `./gradlew check`.

* If helpful, include tests and code documentation for your changes.

* Open a GitHub pull request with the patch. Be sure to include a title and clear description with as much relevant information as possible. Include the relevant issue number if applicable.
