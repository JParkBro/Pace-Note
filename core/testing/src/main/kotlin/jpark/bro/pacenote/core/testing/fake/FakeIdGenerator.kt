package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.common.IdGenerator

/** "{prefix}-1", "{prefix}-2" … 순서대로 만든다. */
class FakeIdGenerator(
    private val prefix: String = "id",
) : IdGenerator {
    private var next = 1

    override fun newId(): String = "$prefix-${next++}"
}
