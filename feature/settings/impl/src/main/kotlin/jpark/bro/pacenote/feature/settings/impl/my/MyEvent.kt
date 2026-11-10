package jpark.bro.pacenote.feature.settings.impl.my

internal sealed interface MyEvent {
    data object OpenGoal : MyEvent

    data object OpenRunSettings : MyEvent

    data object OpenProfile : MyEvent

    /** 메일 앱으로 문의 (앱 버전 · 기기 정보는 Root가 붙인다) */
    data object OpenContact : MyEvent

    /** 오픈소스 라이선스 목록 (oss-licenses 화면) */
    data object OpenOssLicenses : MyEvent

    data object ShowError : MyEvent
}
