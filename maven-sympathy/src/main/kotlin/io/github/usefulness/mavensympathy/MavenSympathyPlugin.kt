package io.github.usefulness.mavensympathy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension

public class MavenSympathyPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(ReportingBasePlugin::class.java)
        val reportingExtension = extensions.getByType(ReportingExtension::class.java)
        val task = tasks.register("sympathyForMrMaven", SympathyForMrMavenTask::class.java) {
            outputFile.set(reportingExtension.baseDirectory.map { it.dir(name).file("output.txt") })
        }
        configurations.matching { it.isCanBeResolved }.configureEach {
            task.configure {
                if (!isCanBeResolved) return@configure
                configurationWithDependencies.put(this@configureEach.name, incoming.resolutionResult.rootComponent)
            }
        }
        tasks.named("check") { dependsOn("sympathyForMrMaven") }
    }
}
