package io.github.usefulness.mavensympathy

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
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

    @TaskAction
    public fun run() {
        var fail = false
        configurationWithDependencies.get().forEach { (name, root) ->
            root.dependencies.asSequence()
                .filterIsInstance<ResolvedDependencyResult>()
                .filter { it.requested is ModuleComponentSelector }
                .forEach { rdr ->
                    val requested = rdr.requested as? ModuleComponentSelector
                    val selected = rdr.selected
                    val requestedVersion = requested?.version
                    val selectedVersion = selected.moduleVersion?.version
                    if (!requestedVersion.isNullOrBlank() && requestedVersion != selectedVersion) {
                        logger.error("[$name] dependency $requested version changed $requestedVersion -> $selectedVersion")
                        fail = true
                    }
                }
        }
        val report = outputFile.get().asFile
        if (fail) {
            report.writeText("NOT OK")
            error("Declared dependencies were upgraded transitively. See task output above. Please update their versions.")
        } else {
            report.writeText("OK")
        }
    }
}
