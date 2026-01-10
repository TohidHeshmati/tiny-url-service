package org.tohid.tinyurlservice.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class IdShufflerTest {
    private val idShuffler = IdShuffler()

    @Test
    fun `should generate unique values for sequential inputs`() {
        val count = 10_000
        val start = 1254L
        val results = (start until count + start).map { idShuffler.shuffle(it) }.toSet()

        assertEquals(count, results.size)
    }

    @ParameterizedTest
    @ValueSource(longs = [0L, 1L, 10L, 61L, 62L, 12345L, 99999L, Long.MAX_VALUE])
    fun `should be deterministic`(id: Long) {
        assertEquals(idShuffler.shuffle(id), idShuffler.shuffle(id))
    }
}
