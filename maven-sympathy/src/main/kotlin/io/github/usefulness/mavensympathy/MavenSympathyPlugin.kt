package io.github.usefulness.mavensympathy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.internal.PublicationInternal
import org.gradle.api.publish.internal.component.ResolutionBackedVariant
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskProvider

public class MavenSympathyPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(ReportingBasePlugin::class.java)
        val pluginExtension = extensions.create("mavenSympathy", MavenSympathyExtension::class.java)
        val reportingExtension = extensions.getByType(ReportingExtension::class.java)
        val task = tasks.register("sympathyForMrMaven", SympathyForMrMavenTask::class.java) {
            outputFile.set(reportingExtension.baseDirectory.map { it.dir(name).file("output.txt") })
        }

        afterEvaluate {
            when (pluginExtension.attachStrategy.orNull) {
                AttachStrategy.ExtractFromMavenPublishComponents -> listenForPublishedConfigurations(task)

                AttachStrategy.WatchAllResolvableConfigurations -> listenForAllConfigurations(task)

                AttachStrategy.Default,
                null,
                -> {
                    if (pluginManager.hasPlugin("maven-publish")) {
                        listenForPublishedConfigurations(task)
                    } else {
                        listenForAllConfigurations(task)
                    }
                }
            }

            pluginExtension.attachStrategy.finalizeValue()
        }

        tasks.named("check") { dependsOn("sympathyForMrMaven") }
    }

    private fun Project.listenForAllConfigurations(task: TaskProvider<SympathyForMrMavenTask>) {
        logger.info("[maven-sympathy] watching all configurations")
        configurations.matching { it.isCanBeResolved }.configureEach {
            task.configure {
                if (!isCanBeResolved) return@configure
                configurationsWithDependencies.put(this@configureEach.name, incoming.resolutionResult.rootComponent)
            }
        }
    }

    private fun Project.listenForPublishedConfigurations(task: TaskProvider<SympathyForMrMavenTask>) {
        pluginManager.withPlugin("maven-publish") {
            logger.info("[maven-sympathy] registering hooks for published configurations")
            extensions.getByType(PublishingExtension::class.java).publications.configureEach {
                if (this !is PublicationInternal<*>) return@configureEach logger.info("[maven-sympathy] Ignoring publication=$name")

                val publicationName = name
                task.configure {
                    val component = component.getOrNull()
                        ?: return@configure logger.info("[maven-sympathy] Missing component for publication=$publicationName")
                    val configurationVariants = component.usages.filterIsInstance<ResolutionBackedVariant>()
                    if (configurationVariants.isEmpty()) {
                        return@configure logger.info("[maven-sympathy] Could not find configurations for publication=$publicationName")
                    }

                    configurationVariants.mapNotNull { it.resolutionConfiguration }
                        .filter { it.isCanBeResolved }
                        .forEach { configurationsWithDependencies.put(it.name, it.incoming.resolutionResult.rootComponent) }
                }
            }
        }
    }
}
