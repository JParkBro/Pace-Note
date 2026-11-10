package jpark.bro.pacenote.core.data.repository

/**
 * 기능 플래그 · 차단 스위치 (roadmap.md 5장, infra-and-billing-protection.md 2-5).
 * 앱에서 플래그를 읽는 곳은 이 인터페이스 한 곳이다.
 *
 * 새 기능 플래그(`feature_{name}_enabled`)는 기본값 false로 추가한다.
 * 값을 못 받아도 미완성 기능이 열리지 않아야 한다.
 */
interface FeatureFlags {
    /** `backup_enabled`. false면 백업 Push · Pull을 모두 건너뛴다 (기록은 PENDING으로 남음) */
    val isBackupEnabled: Boolean

    /** `force_update_min_version_code` · `soft_update_min_version_code` · `soft_update_interval_days` */
    val updatePolicy: UpdatePolicy
}

/** 업데이트 안내 기준 (RELEASE_AND_UPDATE.md 6장). versionCode가 기준 미만이면 안내한다. */
data class UpdatePolicy(
    val forceMinVersionCode: Long,
    val softMinVersionCode: Long,
    val softIntervalDays: Int,
)

/**
 * Remote Config(module-map 9-2 후반) 전까지 쓰는 코드 기본값 구현.
 * Remote Config 구현도 같은 기본값을 쓴다.
 */
internal class LocalFeatureFlags : FeatureFlags {
    override val isBackupEnabled: Boolean = true

    override val updatePolicy: UpdatePolicy =
        UpdatePolicy(
            forceMinVersionCode = 1,
            softMinVersionCode = 1,
            softIntervalDays = 3,
        )
}
