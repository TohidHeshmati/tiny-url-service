package org.tohid.tinyurlservice.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.JdbcTemplate
import org.tohid.tinyurlservice.repository.SequenceRepository
import kotlin.test.assertEquals

class ShortCodeGeneratorTest {
    private val idShuffler = IdShuffler()
    private val jdbcTemplate: JdbcTemplate = mock()
    private val sequenceRepository = SequenceRepository(jdbcTemplate)
    private val blockSize = 10
    private val shortCodeGenerator = ShortCodeGenerator(idShuffler, sequenceRepository, blockSize)

    @BeforeEach
    fun setup() {
        whenever(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1)

        whenever(jdbcTemplate.queryForObject(anyString(), eq(Long::class.java))).thenReturn(10L, 20L)
    }

    @Test
    fun `fetch new block when current block is exhausted`() {
        repeat(blockSize + 1) { shortCodeGenerator.generate() }

        shortCodeGenerator.generate()

        verify(jdbcTemplate, times(2)).update(anyString(), eq(blockSize))
    }

    @Test
    fun `should generate unique codes across multiple blocks no matter the size`() {
        whenever(jdbcTemplate.queryForObject(anyString(), eq(Long::class.java)))
            .thenReturn(10L, 20L, 30L)

        val uniqueGenerated = mutableSetOf<String>()
        val generateCount = blockSize * 3

        repeat(generateCount) {
            uniqueGenerated.add(shortCodeGenerator.generate())
        }

        assertEquals(generateCount, uniqueGenerated.size)
    }

    @Test
    fun `should be deterministic - the same sequence of IDs results same code`() {
        whenever(jdbcTemplate.queryForObject(anyString(), eq(Long::class.java))).thenReturn(10L)
        val resultset1 = mutableSetOf<String>()
        val resultset2 = mutableSetOf<String>()

        repeat(10) {
            resultset1.add(shortCodeGenerator.generate())
        }
        repeat(10) {
            resultset2.add(shortCodeGenerator.generate())
        }
        print(resultset1)

        assertEquals(resultset1, resultset2)
    }
}
