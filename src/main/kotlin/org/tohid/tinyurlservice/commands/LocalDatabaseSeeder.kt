package org.tohid.tinyurlservice.commands

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.tohid.tinyurlservice.domain.Url
import org.tohid.tinyurlservice.domain.UrlDailyClicks
import org.tohid.tinyurlservice.domain.UrlHourlyClicks
import org.tohid.tinyurlservice.repository.UrlDailyClicksRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClicksRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import org.tohid.tinyurlservice.service.ShortCodeGenerator
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate
import java.time.ZoneOffset
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
    private val urlHourlyClicksRepository: UrlHourlyClicksRepository,
    private val urlDailyClicksRepository: UrlDailyClicksRepository,
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
            val savedUrls = urlRepository.saveAll(generalUrls + randomUrls)
            seedStats(savedUrls)
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

    private fun seedStats(urls: Iterable<Url>) {
        val hourlyClicks = mutableListOf<UrlHourlyClicks>()
        val dailyClicks = mutableListOf<UrlDailyClicks>()
        val updatedUrls = mutableListOf<Url>()

        println("Seeding statistics...")
        urls.forEach { url ->
            var urlTotal = 0L
            val now = now()

            for (d in 0..30) {
                val day = now.minus(d.toLong(), ChronoUnit.DAYS)
                val date = LocalDate.ofInstant(day, ZoneOffset.UTC)
                var dailyCount = 0L

                for (h in 0..23) {
                    if (Random.nextDouble() < 0.3) {
                        val count = Random.nextLong(1, 10)
                        val hourInstant = date.atStartOfDay(ZoneOffset.UTC).toInstant().plus(h.toLong(), ChronoUnit.HOURS)

                        hourlyClicks.add(UrlHourlyClicks(urlId = url.id, clickHour = hourInstant, count = count))
                        dailyCount += count
                    }
                }

                if (dailyCount > 0) {
                    dailyClicks.add(UrlDailyClicks(urlId = url.id, clickDate = date, count = dailyCount))
                    urlTotal += dailyCount
                }
            }

            if (urlTotal > 0) {
                updatedUrls.add(url.copy(totalClickCount = urlTotal))
            }
        }

        urlHourlyClicksRepository.saveAll(hourlyClicks)
        urlDailyClicksRepository.saveAll(dailyClicks)
        urlRepository.saveAll(updatedUrls)
        println("Statistics seeded: ${hourlyClicks.size} hourly entries, ${dailyClicks.size} daily entries.")
    }
}
