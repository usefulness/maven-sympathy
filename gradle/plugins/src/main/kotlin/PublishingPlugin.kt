import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")
        if (findConfig("SIGNING_PASSWORD").isNotEmpty()) {
            pluginManager.apply("signing")
        }

        extensions.configure<PublishingExtension> {
            with(repositories) {
                maven { maven ->
                    maven.name = "github"
                    maven.setUrl("https://maven.pkg.github.com/usefulness/maven-sympathy")
                    with(maven.credentials) {
                        username = "usefulness"
                        password = findConfig("GITHUB_TOKEN")
                    }
                }
                maven { maven ->
                    maven.name = "mavenCentral"
                    maven.setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    maven.mavenContent { it.releasesOnly() }
                    with(maven.credentials) {
                        username = findConfig("OSSRH_USERNAME")
                        password = findConfig("OSSRH_PASSWORD")
                    }
                }
            }
        }
        pluginManager.withPlugin("com.gradle.plugin-publish") {
            extensions.configure<GradlePluginDevelopmentExtension>("gradlePlugin") { gradlePlugin ->
                gradlePlugin.apply {
                    website.set("https://github.com/usefulness/maven-sympathy")
                    vcsUrl.set("https://github.com/usefulness/maven-sympathy")
                }
            }
        }

        pluginManager.withPlugin("signing") {
            with(extensions.extraProperties) {
                set("signing.keyId", findConfig("SIGNING_KEY_ID"))
                set("signing.password", findConfig("SIGNING_PASSWORD"))
                set("signing.secretKeyRingFile", findConfig("SIGNING_SECRET_KEY_RING_FILE"))
            }

            extensions.configure<SigningExtension>("signing") { signing ->
                signing.sign(extensions.getByType(PublishingExtension::class.java).publications)
            }
        }

        pluginManager.withPlugin("java") {
            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
                withJavadocJar()
            }

            tasks.named("processResources", ProcessResources::class.java) { processResources ->
                processResources.from(rootProject.file("LICENSE"))
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                pluginManager.apply("org.jetbrains.dokka")

                tasks.withType(DokkaTask::class.java).configureEach { dokkaTask ->
                    dokkaTask.notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/1217")
                }
                tasks.named("javadocJar", Jar::class.java) { javadocJar ->
                    javadocJar.from(tasks.named("dokkaJavadoc"))
                }
            }

            extensions.configure<PublishingExtension> {
                if (!pluginManager.hasPlugin("com.gradle.plugin-publish")) {
                    publications.register("mavenJava", MavenPublication::class.java) { publication ->
                        publication.from(components.getByName("java"))
                    }
                }
                publications.configureEach { publication ->
                    (publication as? MavenPublication)?.pom { pom ->
                        afterEvaluate {
                            pom.name.set("${project.group}:${project.name}")
                            pom.description.set(project.description)
                        }
                        pom.url.set("https://github.com/usefulness/maven-sympathy")
                        pom.licenses { licenses ->
                            licenses.license { license ->
                                license.name.set("MIT")
                                license.url.set("https://github.com/usefulness/maven-sympathy/blob/master/LICENSE")
                            }
                        }
                        pom.developers { developers ->
                            developers.developer { developer ->
                                developer.id.set("mateuszkwiecinski")
                                developer.name.set("Mateusz Kwiecinski")
                                developer.email.set("36954793+mateuszkwiecinski@users.noreply.github.com")
                            }
                        }
                        pom.scm { scm ->
                            scm.connection.set("scm:git:github.com/usefulness/maven-sympathy.git")
                            scm.developerConnection.set("scm:git:ssh://github.com/usefulness/maven-sympathy.git")
                            scm.url.set("https://github.com/usefulness/maven-sympathy/tree/master")
                        }
                    }
                }
            }
        }
    }

    private inline fun <reified T> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver(it) }
    }
}

private fun Project.findConfig(key: String): String = findProperty(key)?.toString() ?: System.getenv(key) ?: ""
