package jpark.bro.pacenote.core.domain.update

import jpark.bro.pacenote.core.model.UpdateDecision
import jpark.bro.pacenote.core.model.UpdatePolicy
import java.util.concurrent.TimeUnit

/**
 * 인앱 업데이트 정책 판단 (docs/common/RELEASE_AND_UPDATE.md 6-2).
 * Play에 업데이트가 있는지는 호출하는 쪽(app)이 확인한다.
 *
 * @param lastPromptAt 선택 업데이트를 마지막으로 안내한 시각. 없으면 null
 */
fun decideUpdate(
    currentVersionCode: Int,
    policy: UpdatePolicy,
    lastPromptAt: Long?,
    now: Long,
): UpdateDecision {
    val intervalPassed =
        lastPromptAt == null || now - lastPromptAt >= TimeUnit.DAYS.toMillis(policy.softIntervalDays.toLong())
    return when {
        currentVersionCode < policy.forceMinVersionCode -> UpdateDecision.FORCE
        currentVersionCode < policy.softMinVersionCode && intervalPassed -> UpdateDecision.SOFT
        else -> UpdateDecision.NONE
    }
}
