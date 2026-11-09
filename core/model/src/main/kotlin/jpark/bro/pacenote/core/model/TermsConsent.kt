package jpark.bro.pacenote.core.model

/**
 * 온보딩 약관 동의(O1) 기록. 기기에 저장하고 로그인하면 `users/{uid}`에 올린다 (product-spec.md 6장).
 * 각 버전은 동의한 시점의 정책 문서 버전, [agreedAt] · [marketingUpdatedAt]은 epoch millis.
 */
data class TermsConsent(
    val termsVersion: String,
    val privacyVersion: String,
    val locationTermsVersion: String,
    val ageOver14: Boolean,
    val marketing: Boolean,
    val agreedAt: Long,
    val marketingUpdatedAt: Long,
)
