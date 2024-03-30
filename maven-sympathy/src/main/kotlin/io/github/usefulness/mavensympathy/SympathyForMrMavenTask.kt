package io.github.usefulness.mavensympathy

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public open class SympathyForMrMavenTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {

    @Input
    public val configurationWithDependencies: MapProperty<String, ResolvedComponentResult> = objectFactory.mapProperty(
        String::class.java,
        ResolvedComponentResult::class.java,
    )

    @OutputFile
    public val outputFile: RegularFileProperty = objectFactory.fileProperty()

    @Input
    public val behaviorOnMismatch: Property<BehaviorOnMismatch> = objectFactory.property(BehaviorOnMismatch::class.java)
        .value(BehaviorOnMismatch.Fail)

    public fun behaviorOnMismatch(value: String) {
        behaviorOnMismatch.set(BehaviorOnMismatch.entries.first { it.name.equals(value, ignoreCase = true) })
    }

    @TaskAction
    public fun run() {
        var fail = false
        val errorMessages = mutableListOf<String>()

        configurationWithDependencies.get().forEach { (name, root) ->
            root.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach perDependency@{ rdr ->
                val requested = rdr.requested as? ModuleComponentSelector ?: return@perDependency
                val selected = rdr.selected
                val requestedVersion = requested.version
                val selectedVersion = selected.moduleVersion?.version
                if (!requestedVersion.isNullOrBlank() && requestedVersion != selectedVersion) {
                    val errorMessage = "[$name] dependency $requested version changed $requestedVersion -> $selectedVersion"
                    errorMessages.add(errorMessage)
                    logger.error(errorMessage)
                    fail = true
                }
            }
        }
        val report = outputFile.get().asFile
        if (fail) {
            report.writeText(errorMessages.joinToString(separator = "\n"))
            val failureMessage = "Declared dependencies were upgraded transitively. See task output above. Please update their versions."

            when (behaviorOnMismatch.get()) {
                BehaviorOnMismatch.Warn -> logger.error(failureMessage)
                BehaviorOnMismatch.Fail, null -> error(failureMessage)
            }
        } else {
            report.writeText("OK")
        }
    }
}
