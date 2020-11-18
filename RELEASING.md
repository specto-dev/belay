# Prerequisites

- The [Gradle signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html) must be configured.
- The following Gradle properties must be set:
  - `specto.sonatype.user`
  - `specto.sonatype.password`
  
# Releasing a new version

1. Update the version in `build.gradle`
2. Update the version in `README.md`
3. Update the `CHANGELOG.md`
4. `git commit -am "Prepare for release X.Y.Z"` (where X.Y.Z is the version set in step 1)
5. `git push`
6. Create a new release on GitHub
    1. Tag version `vX.Y.Z`
    2. Release title `vX.Y.Z`
    3. Paste the content from `CHANGELOG.md` as the description
7. `./gradlew publishBelayPublicationToSonatypeStagingRepository`
8. Visit [Sonatype Nexus Repository Manager](https://oss.sonatype.org/) and promote the artifact
