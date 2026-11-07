import com.android.build.api.dsl.ApplicationExtension
import jpark.bro.pacenote.convention.addUnitTestDependencies
import jpark.bro.pacenote.convention.configureApplicationBuildTypes
import jpark.bro.pacenote.convention.configureFlavors
import jpark.bro.pacenote.convention.configureKotlinAndroid
import jpark.bro.pacenote.convention.configureStaticAnalysis
import jpark.bro.pacenote.convention.libs
import jpark.bro.pacenote.convention.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.version("projectApplicationId")
                    targetSdk = libs.version("projectTargetSdkVersion").toInt()
                    versionCode = libs.version("projectVersionCode").toInt()
                    versionName = libs.version("projectVersionName")
                }

                buildFeatures {
                    buildConfig = true
                }

                configureKotlinAndroid(this)
                configureFlavors()
                configureApplicationBuildTypes(this)
            }

            addUnitTestDependencies()
            configureStaticAnalysis()
        }
    }
}
