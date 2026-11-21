import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class ConventionsPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            extensions.configure(KotlinJvmProjectExtension::class.java) {
                jvmToolchain(libs.findVersion("java-compilation").get().requiredVersion.toInt())
            }
        }

        pluginManager.withPlugin("java") {
            if (hasProperty("skipJarVersion")) {
                val projectName = target.name
                tasks.named("jar", Jar::class.java) {
                    archiveFileName.set("$projectName.jar")
                }
            }
        }
    }
}
