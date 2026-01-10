package org.tohid.tinyurlservice.service

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.tohid.tinyurlservice.repository.SequenceRepository

@Component
class ShortCodeGenerator(
    private val idShuffler: IdShuffler,
    private val sequenceRepository: SequenceRepository,
    @Value("\${block-size:1000}") private val blockSize: Int,
) {
    private var currentId = 0L
    private var maxIdInBlock = 0L

    @PostConstruct
    fun init() = refreshBlock()

    @Synchronized
    fun generate(): String {
        if (currentId >= maxIdInBlock) {
            refreshBlock()
        }
        val sequentialId = currentId++
        val scrambledId = idShuffler.shuffle(sequentialId)
        return encodeToBase62(scrambledId)
    }

    private fun refreshBlock() {
        val newEnd = sequenceRepository.getNextBlockEnd(blockSize)
        this.maxIdInBlock = newEnd
        this.currentId = newEnd - blockSize
    }

    private fun encodeToBase62(num: Long): String {
        var n = num
        val sb = StringBuilder()
        while (n > 0) {
            sb.append(ALL_CHARS[(n % 62).toInt()])
            n /= 62
        }
        return sb.reverse().toString().padStart(7, '0')
    }

    companion object {
        private const val ALL_CHARS = "w7NF2zP9xLC5uqA1k8mRho4SvY3bJ6cE0dUQfaHIjKMnBODrstTVGeWiXyplgZ"
    }
}
