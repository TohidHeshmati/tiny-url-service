package org.tohid.tinyurlservice.commands

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.repository.UrlRepository
import org.tohid.tinyurlservice.service.ShortCodeGenerator
import java.time.Instant.now
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/* * LocalDatabaseSeeder is a CommandLineRunner that seeds the local database with initial URLs.
 * It runs only when the application is started with the "local" profile.
 * This is useful for development and testing purposes to have a set of predefined URLs.
 */

@Profile("local")
@Component
class LocalDatabaseSeeder(
    private val urlRepository: UrlRepository,
    private val shortCodeGenerator: ShortCodeGenerator,
) : CommandLineRunner {
    @OptIn(ExperimentalTime::class)
    override fun run(vararg args: String?) {
        println("Seeding local database with initial URLs...")
        if (urlRepository.count() == 0L) {
            val generalUrls =
                listOf(
                    Url(
                        originalUrl = "https://www.google.com",
                        shortUrl = shortCodeGenerator.generate(),
                        expiryDate = randomTimeInFuture(),
                    ),
                    Url(
                        originalUrl = "https://www.github.com",
                        shortUrl = shortCodeGenerator.generate(),
                        expiryDate = randomTimeInFuture(),
                    ),
                    Url(
                        originalUrl = "https://www.stackoverflow.com",
                        shortUrl = shortCodeGenerator.generate(),
                        expiryDate = randomTimeInFuture(),
                    ),
                )
            val randomUrls =
                (1..100).map { i ->
                    Url(
                        originalUrl = "https://example.com/page$i",
                        shortUrl = shortCodeGenerator.generate(),
                        expiryDate = randomTimeInFuture(),
                    )
                }
            urlRepository.saveAll(generalUrls + randomUrls)
        } else {
            println("Local database already seeded with URLs.")
        }
        println("Local database seeding completed. database contains ${urlRepository.count()} URLs.")
    }

    private fun randomTimeInFuture() =
        now()
            .plus(Random.nextLong(700), ChronoUnit.DAYS)
            .plus(Random.nextLong(100), ChronoUnit.HOURS)
            .plusSeconds(Random.nextLong(59))
}
