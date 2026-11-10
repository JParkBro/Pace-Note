package jpark.bro.pacenote.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 화면 ViewModel의 기반 (ANDROID_ARCHITECTURE 6-3). 공개 멤버는 [state] · [event] · [onAction]뿐이다.
 * Event는 `Channel`로 보내 한 번만 소비된다. Event가 없는 화면은 [E]에 `Nothing`을 쓴다.
 */
abstract class MviViewModel<S, A, E>(
    initialState: S,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val events = Channel<E>(Channel.BUFFERED)
    val event: Flow<E> = events.receiveAsFlow()

    protected val currentState: S get() = mutableState.value

    abstract fun onAction(action: A)

    protected fun setState(reduce: S.() -> S) {
        mutableState.update(reduce)
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch { events.send(event) }
    }
}
