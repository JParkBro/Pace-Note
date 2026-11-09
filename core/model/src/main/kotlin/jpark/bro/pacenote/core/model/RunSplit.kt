package jpark.bro.pacenote.core.model

/** km 구간 하나. 마지막 구간은 1km보다 짧을 수 있다. */
data class RunSplit(
    val index: Int,
    val distanceM: Double,
    val movingMs: Long,
)
