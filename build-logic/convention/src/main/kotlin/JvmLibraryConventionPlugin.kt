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

            // Android 모듈과 같은 이름으로 실행되게 해서 전체 단위 테스트 명령에 JVM 모듈을 포함한다
            tasks.register("testDevDebugUnitTest") {
                group = "verification"
                dependsOn("test")
            }
        }
    }
}
