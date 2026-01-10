package org.tohid.tinyurlservice.configs

import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.tohid.tinyurlservice.utils.SafeInstantDeserializer
import java.time.Instant

@Configuration
class JacksonConfig {
    @Bean
    fun customInstantModule(): SimpleModule =
        SimpleModule().addDeserializer(
            Instant::class.java,
            SafeInstantDeserializer(),
        )
}
