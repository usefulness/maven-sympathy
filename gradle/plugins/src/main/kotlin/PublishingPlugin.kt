import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import kotlin.jvm.java

class PublishingPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.vanniktech.maven.publish")
        pluginManager.apply("org.jetbrains.dokka")

        pluginManager.withPlugin("java") {
            tasks.named("processResources", ProcessResources::class.java) {
                from(rootProject.file("LICENSE"))
            }
        }

        extensions.configure(MavenPublishBaseExtension::class.java) {
            publishToMavenCentral()
            coordinates(group.toString(), name, version.toString())

            signAllPublications()

            configureBasedOnAppliedPlugins()

            pom {
                afterEvaluate {
                    this@pom.name.set("${project.group}:${project.name}")
                    this@pom.description.set(project.description)
                }
                url.set("https://github.com/usefulness/maven-sympathy")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/usefulness/maven-sympathy/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("mateuszkwiecinski")
                        name.set("Mateusz Kwiecinski")
                        email.set("36954793+mateuszkwiecinski@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/usefulness/maven-sympathy.git")
                    developerConnection.set("scm:git:ssh://github.com/usefulness/maven-sympathy.git")
                    url.set("https://github.com/usefulness/maven-sympathy/tree/master")
                }
            }
        }

        extensions.configure<PublishingExtension> {
            repositories.maven {
                name = "github"
                setUrl("https://maven.pkg.github.com/usefulness/maven-sympathy")
                with(credentials) {
                    username = "usefulness"
                    password = findConfig("GITHUB_TOKEN")
                }
            }

            tasks.named { it == "javadocJar" }.configureEach {
                this as Jar
                from(tasks.named("dokkaGeneratePublicationHtml"))
            }
        }
        pluginManager.withPlugin("com.gradle.plugin-publish") {
            extensions.configure<GradlePluginDevelopmentExtension>("gradlePlugin") {
                website.set("https://github.com/usefulness/maven-sympathy")
                vcsUrl.set("https://github.com/usefulness/maven-sympathy")
            }
        }
    }

    private inline fun <reified T : Any> ExtensionContainer.configure(crossinline receiver: T.() -> Unit) {
        configure(T::class.java) { receiver() }
    }
}

private fun Project.findConfig(key: String): String = findProperty(key)?.toString() ?: System.getenv(key) ?: ""
