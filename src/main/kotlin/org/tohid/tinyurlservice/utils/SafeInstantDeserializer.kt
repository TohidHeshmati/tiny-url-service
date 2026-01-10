package org.tohid.tinyurlservice.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeParseException

class SafeInstantDeserializer : JsonDeserializer<Instant?>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Instant =
        try {
            Instant.parse(p.text)
        } catch (e: DateTimeParseException) {
            logger.error(e.message, e)
            throw InvalidFormatException.from(
                p,
                "Invalid date format for expiryDate. Must be ISO-8601.",
                p.text,
                Instant::class.java,
            )
        }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(SafeInstantDeserializer::class.java)
    }
}
