plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.koin)
}

android {
    namespace = "jpark.bro.pacenote.core.data"
}

dependencies {
    api(projects.core.model)
    // RunRepository.observeRunPages()가 PagingData를 돌려준다
    api(libs.androidx.paging.common)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)

    // Repository 테스트: Robolectric + in-memory Room (TESTING.md 2-1)
    testImplementation(projects.core.testing)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.room.runtime)
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.androidx.paging.testing)
}
