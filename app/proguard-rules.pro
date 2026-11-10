# 앱 전용 R8 규칙. 라이브러리(Room, Koin, kotlinx.serialization, Navigation 3 등)는 각자 consumer 규칙을 함께 배포한다.

# Parcelable 클래스가 삭제되거나 난독화되지 않도록 유지
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# @Keep 애노테이션이 적용된 클래스 유지
-keep @androidx.annotation.Keep class *

# Room 데이터베이스 관련 클래스 유지
-keep class androidx.room.* { *; }
-keep @androidx.room.* class *

# 크래시 스택 트레이스에 파일명 · 줄 번호 유지
-keepattributes SourceFile, LineNumberTable
