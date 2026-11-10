import io.github.takahirom.roborazzi.RoborazziExtension
import jpark.bro.pacenote.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * 스크린샷 테스트 (TESTING.md 5-10). 같은 파일의 private Preview를 찾아 Robolectric으로 촬영한다.
 * 기준 이미지는 모듈의 `src/test/screenshots/`에 둔다.
 *
 * - `./gradlew recordRoborazziDevDebug`: 기준 이미지 생성 · 갱신 (의도한 UI 변경 후)
 * - `./gradlew verifyRoborazziDevDebug`: 기준 이미지와 비교, 다르면 실패
 */
class RoborazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
                generateComposePreviewRobolectricTests {
                    enable.set(true)
                    // 이 모듈의 Preview만 찍는다 (의존하는 모듈의 Preview를 중복으로 찍지 않게)
                    packages.set(listOf(modulePackage()))
                    includePrivatePreviews.set(true)
                }
            }

            dependencies {
                add("testImplementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("testImplementation", libs.findBundle("screenshot-test").get())
            }
        }
    }
}

/** `:feature:home:impl` → `jpark.bro.pacenote.feature.home.impl` (모듈 namespace 규칙과 같다) */
private fun Project.modulePackage(): String = "jpark.bro.pacenote" + path.replace(':', '.')
