package jpark.bro.pacenote.core.model

/** Remote Config의 업데이트 정책 (docs/common/RELEASE_AND_UPDATE.md 6장). */
data class UpdatePolicy(
    val forceMinVersionCode: Int,
    val softMinVersionCode: Int,
    val softIntervalDays: Int,
)

enum class UpdateDecision {
    FORCE,
    SOFT,
    NONE,
}
