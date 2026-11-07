import jpark.bro.pacenote.convention.addUnitTestDependencies
import jpark.bro.pacenote.convention.configureKotlinJvm
import jpark.bro.pacenote.convention.configureStaticAnalysis
import org.gradle.api.Plugin
import org.gradle.api.Project

class JvmLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            configureKotlinJvm()
            addUnitTestDependencies()
            configureStaticAnalysis()
        }
    }
}
