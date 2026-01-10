package org.tohid.tinyurlservice.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SequenceRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    @Transactional
    fun getNextBlockEnd(blockSize: Int): Long {
        jdbcTemplate.update(
            "UPDATE global_id_sequence SET next_block_start = LAST_INSERT_ID(next_block_start + ?)",
            blockSize,
        )
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long::class.java)
            ?: throw IllegalStateException("ID allocation failed")
    }
}
