package org.tohid.tinyurlservice.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import org.junit.jupiter.api.assertNotNull

class SafeInstantDeserializerTest {
    private val safeInstantDeserializer = SafeInstantDeserializer()
    private val objectMapper = ObjectMapper()
    private val deserializationContext = ObjectMapper().deserializationContext

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2023-10-01T12:00:00Z",
            "2023-10-01T12:00:00.123Z",
            "2023-10-01T12:00:00+02:00",
            "2023-10-01T12:00:00.123+02:00",
        ],
    )
    fun `deserializes valid ISO-8601 format`(input: String) {
        val result =
            safeInstantDeserializer.deserialize(
                createParser(input),
                deserializationContext,
            )

        assertNotNull(result)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "2023-10-01T12:00:00",
            "2023-10-01T12:00:00.123",
            "2023-10-01 12:00:00Z",
            "2023/10/01T12:00:00Z",
        ],
    )
    fun `does not deserialize invalid ISO-8601 format`(input: String) {
        val exception =
            assertThrows<InvalidFormatException> {
                safeInstantDeserializer.deserialize(
                    createParser(input),
                    deserializationContext,
                )
            }

        assert(exception.message?.contains("Invalid date format for expiryDate. Must be ISO-8601.") == true)
    }

    private fun createParser(input: String): JsonParser {
        val parser = objectMapper.factory.createParser("\"$input\"")
        parser.nextToken()
        return parser
    }
}
