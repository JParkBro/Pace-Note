plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.room)
    alias(libs.plugins.pacenote.koin)
}

android {
    namespace = "jpark.bro.pacenote.core.database"
}

dependencies {
    // 기록 목록(R1) 무한 스크롤
    api(libs.room.paging)
    // DAO 테스트: Robolectric + in-memory Room (TESTING.md 2-1)
    testImplementation(libs.bundles.robolectric)
}
