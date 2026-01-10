package org.tohid.tinyurlservice.service

import org.springframework.stereotype.Component

/**
 * A bijective ID scrambler based on a Linear Congruential Generator (LCG) principle.
 * * This component maps sequential IDs (1, 2, 3...) to a pseudorandom space [0, MAX_SPACE).
 * Because MULTIPLIER and MAX_SPACE are co-prime, the mapping is guaranteed to be
 * unique (no collisions).
 * MULTIPLIER is 8th Mersenne Prime (2^31 - 1)
 */
@Component
class IdShuffler {
    fun shuffle(id: Long): Long = Math.floorMod(id * MULTIPLIER + INCREMENT, MAX_SPACE)

    companion object {
        private const val MAX_SPACE: Long = 1_000_000_000_000L
        private const val MULTIPLIER: Long = 2147483647L
        private const val INCREMENT: Long = 123456789L
    }
}
