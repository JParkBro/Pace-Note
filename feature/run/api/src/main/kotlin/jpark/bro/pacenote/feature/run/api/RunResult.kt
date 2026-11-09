package jpark.bro.pacenote.feature.run.api

/** 러닝 흐름이 끝났을 때 알리는 결과. 다음 목적지는 app이 정한다 (module-map 4-3). */
sealed interface RunResult {
    /** 결과(P6)에서 "완료" */
    data object Finished : RunResult
}

/** 구현은 app에 있다. run:impl은 홈 등 다른 기능을 모른다. */
fun interface RunResultHandler {
    fun handle(result: RunResult)
}
