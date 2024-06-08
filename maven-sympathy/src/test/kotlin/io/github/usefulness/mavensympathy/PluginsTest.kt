package io.github.usefulness.mavensympathy

import io.github.usefulness.mavensympathy.internal.resolve
import io.github.usefulness.mavensympathy.internal.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PluginsTest {

    @TempDir
    lateinit var rootDirectory: File

    @BeforeEach
    fun setUp() {
        rootDirectory.resolve("settings.gradle") {
            // language=groovy
            writeText(
                """
                    pluginManagement {
                        repositories { 
                             mavenCentral()
                             gradlePluginPortal()
                        }
                    }
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
    fun gradlePlugin() {
        rootDirectory.resolve("build.gradle") {
            // language=groovy
            writeText(
                """
                plugins {
                    id("java-gradle-plugin")
                    id("maven-publish")
                    id("io.github.usefulness.maven-sympathy")
                }
                
                gradlePlugin {
                    plugins {
                        fooPlugin {
                            id = 'io.github.foo.plugin'
                            implementationClass = 'FixturImplementationClass'
                        }
                    }
                }
                
                dependencies {
                    implementation("com.squareup.retrofit2:retrofit:2.11.0")
                    implementation("com.squareup.okhttp3:okhttp:3.14.8")
                }
                """.trimIndent(),
            )
        }
        val result = runGradle(projectDir = rootDirectory, shouldFail = true)

        assertThat(result.output).contains("com.squareup.okhttp3:okhttp:3.14.8 version changed: 3.14.8 → 3.14.9")
        assertThat(rootDirectory.resolve("build/reports/sympathyForMrMaven/output.txt")).content()
            .isEqualToIgnoringWhitespace(
                """
                [compileClasspath] dependency com.squareup.okhttp3:okhttp:3.14.8 version changed: 3.14.8 → 3.14.9
                [runtimeClasspath] dependency com.squareup.okhttp3:okhttp:3.14.8 version changed: 3.14.8 → 3.14.9
                """.trimIndent(),
            )
    }
}
