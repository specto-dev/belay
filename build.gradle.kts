import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.72"

    // Checks
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.1"

    // Code coverage
    jacoco

    // Publishing
    id("org.jetbrains.dokka") version "1.4.10.2"
    id("maven-publish")
    id("signing")
}

group = "dev.specto"
version = "0.3.0"

repositories {
    jcenter()
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

ktlint {
    version.set("0.36.0")
}

detekt {
    reports {
        html.enabled = true
        txt.enabled = true
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
        preferProjectModules()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka/javadoc")
    dependsOn(tasks.getByName("dokkaJavadoc"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = "SonatypeSnapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            credentials {
                username = findProperty("specto.sonatype.user") as String?
                password = findProperty("specto.sonatype.password") as String?
            }
        }
        maven {
            name = "SonatypeStaging"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = findProperty("specto.sonatype.user") as String?
                password = findProperty("specto.sonatype.password") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("belay") {
            from(components["java"])

            artifact(dokkaJavadocJar)
            artifact(sourcesJar)

            pom {
                name.set("Belay")
                description.set("Robust error-handling for Kotlin and Android")
                url.set("https://github.com/specto-dev/belay")
                licenses {
                    license {
                        name.set("MIT")
                        distribution.set("repo")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    url.set("https://github.com/specto-dev/belay")
                }
                developers {
                    developer {
                        id.set("nathanael")
                        name.set("Nathanael Silverman")
                        email.set("nathanael@specto.dev")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
