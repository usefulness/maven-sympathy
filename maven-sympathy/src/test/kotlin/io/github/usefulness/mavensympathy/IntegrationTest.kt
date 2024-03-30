package io.github.usefulness.mavensympathy

import io.github.usefulness.mavensympathy.internal.resolve
import io.github.usefulness.mavensympathy.internal.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class IntegrationTest {

    @TempDir
    lateinit var rootDirectory: File

    @BeforeEach
    fun setUp() {
        rootDirectory.resolve("settings.gradle") {
            // language=groovy
            writeText(
                """
                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                    repositories {
                        mavenCentral()
                    }
                }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun successfulRun() {
        rootDirectory.resolve("build.gradle") {
            // language=groovy
            writeText(
                """
                plugins {
                    id("java-library")
                    id("io.github.usefulness.maven-sympathy")
                }
                """.trimIndent(),
            )
        }
        val result = runGradle(projectDir = rootDirectory)
        assertThat(result.output).doesNotContain("changed to")
        assertThat(result.task(":sympathyForMrMaven")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val reRunResult = runGradle(projectDir = rootDirectory)
        assertThat(reRunResult.output).doesNotContain("changed to")
        assertThat(reRunResult.task(":sympathyForMrMaven")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun ownUpgrade() {
        rootDirectory.resolve("build.gradle") {
            // language=groovy
            writeText(
                """
                plugins {
                    id("java-library")
                    id("io.github.usefulness.maven-sympathy")
                }
                
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.11.0")
                    implementation("com.squareup.okhttp3:okhttp:4.12.0")
                }
                """.trimIndent(),
            )
        }
        val result = runGradle(projectDir = rootDirectory)

        assertThat(result.output).doesNotContain("changed to")
    }

    @Test
    fun automaticBump() {
        rootDirectory.resolve("build.gradle") {
            // language=groovy
            writeText(
                """
                plugins {
                    id("java-library")
                    id("io.github.usefulness.maven-sympathy")
                }
                
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.11.0")
                    implementation("com.squareup.okhttp3:okhttp:3.14.8")
                }
                """.trimIndent(),
            )
        }
        val result = runGradle(projectDir = rootDirectory, shouldFail = true)

        assertThat(result.output).contains("com.squareup.okhttp3:okhttp:3.14.8 changed to 3.14.9")
    }
}
