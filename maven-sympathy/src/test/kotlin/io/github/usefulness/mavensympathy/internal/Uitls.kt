package io.github.usefulness.mavensympathy.internal

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.io.InputStream

internal fun runGradle(
    projectDir: File,
    tasksToRun: List<String> = listOf("check"),
    configurationCacheEnabled: Boolean = true,
    shouldFail: Boolean = false,
): BuildResult = GradleRunner.create().apply {
    forwardOutput()
    withPluginClasspath()
    withProjectDir(projectDir)

    withArguments(
        buildList {
            addAll(tasksToRun)
            if (configurationCacheEnabled) {
                add("--configuration-cache")
            }
            add("--stacktrace")
        },
    )

    // https://docs.gradle.org/8.1.1/userguide/configuration_cache.html#config_cache:not_yet_implemented:testkit_build_with_java_agent
    if (!configurationCacheEnabled) {
        withJaCoCo()
    }
}.run {
    if (shouldFail) {
        buildAndFail()
    } else {
        build()
    }
}

private fun GradleRunner.withJaCoCo(): GradleRunner {
    javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")
        ?.toFile(File(projectDir, "gradle.properties"))
    return this
}

private fun InputStream.toFile(file: File) {
    use { input ->
        file.outputStream().use { input.copyTo(it) }
    }
}

internal fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
    parentFile.mkdirs()
    receiver()
}
