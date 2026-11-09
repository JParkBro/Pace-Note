package jpark.bro.pacenote.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * 읽기 중 [IOException]은 빈 값(= 기본값)으로 대신한다. 쓰기 예외는 호출하는 Repository가 `suspendRunCatching`으로 감싼다.
 */
internal fun <T> DataStore<Preferences>.observe(read: (Preferences) -> T): Flow<T> =
    data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(read)
        .distinctUntilChanged()

/** enum은 이름으로 저장하고, 모르는 이름이면 [default]로 읽는다. */
internal inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
    this?.let { name -> enumValues<E>().find { it.name == name } } ?: default
