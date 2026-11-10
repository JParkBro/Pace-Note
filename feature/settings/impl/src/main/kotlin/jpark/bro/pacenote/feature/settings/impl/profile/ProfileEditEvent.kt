package jpark.bro.pacenote.feature.settings.impl.profile

internal sealed interface ProfileEditEvent {
    data object Close : ProfileEditEvent

    data object ShowError : ProfileEditEvent
}
