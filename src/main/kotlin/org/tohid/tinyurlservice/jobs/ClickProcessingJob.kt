package org.tohid.tinyurlservice.jobs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.tohid.tinyurlservice.repository.UrlDailyClickRepository
import org.tohid.tinyurlservice.repository.UrlHourlyClickRepository
import org.tohid.tinyurlservice.repository.UrlRepository
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val CLICK_STREAM_KEY = "click_events"
private const val CONSUMER_GROUP = "click_processors"
private const val CONSUMER_NAME = "processor-1"

@Component
@Profile("!test")
class ClickProcessingJob(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val urlRepository: UrlRepository,
    private val dailyClickRepository: UrlDailyClickRepository,
    private val hourlyClickRepository: UrlHourlyClickRepository,
) {
    init {
        try {
            redisTemplate.opsForStream<String, String>().createGroup(CLICK_STREAM_KEY, CONSUMER_GROUP)
        } catch (e: Exception) {
            logger.info("Consumer group '$CONSUMER_GROUP' may already exist.")
        }
    }

    @Scheduled(cron = "\${click.processing.cron}")
    @SchedulerLock(name = "processClicks", lockAtMostFor = "5m", lockAtLeastFor = "30s")
    fun processClicks() {
        logger.info("Starting click processing job.")
        val streamOps = redisTemplate.opsForStream<String, String>()
        val messages =
            streamOps.read(
                Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                StreamOffset.create(CLICK_STREAM_KEY, ReadOffset.lastConsumed()),
            )

        if (messages.isNullOrEmpty()) {
            logger.info("No new click events to process.")
            return
        }

        val dailyAggregates = mutableMapOf<Pair<String, java.time.LocalDate>, Long>()
        val hourlyAggregates = mutableMapOf<Pair<String, Instant>, Long>()
        val totalAggregates = mutableMapOf<String, Long>()

        messages.forEach { message: MapRecord<String, String, String> ->
            val payload = message.value["payload"] ?: return@forEach
            val event = objectMapper.readValue<Map<String, String>>(payload)
            val shortCode = event["shortCode"]!!
            val timestamp = Instant.parse(event["timestamp"])

            val date = timestamp.atZone(ZoneOffset.UTC).toLocalDate()
            val hour = timestamp.truncatedTo(ChronoUnit.HOURS)

            dailyAggregates.merge(shortCode to date, 1L, Long::plus)
            hourlyAggregates.merge(shortCode to hour, 1L, Long::plus)
            totalAggregates.merge(shortCode, 1L, Long::plus)
        }

        logger.info("Aggregated ${messages.size} click events. Persisting to database.")

        val urls = urlRepository.findAll().associateBy { it.shortUrl }

        dailyAggregates.forEach { (key, count) ->
            val (shortCode, date) = key
            urls[shortCode]?.let { dailyClickRepository.upsert(it.id, date, count) }
        }

        hourlyAggregates.forEach { (key, count) ->
            val (shortCode, hour) = key
            urls[shortCode]?.let { hourlyClickRepository.upsert(it.id, hour, count) }
        }

        totalAggregates.forEach { (shortUrl, count) ->
            urlRepository.incrementTotalClickCount(shortUrl, count)
        }

        streamOps.acknowledge(CLICK_STREAM_KEY, CONSUMER_GROUP, *messages.map { it.id }.toTypedArray())
        logger.info("Successfully processed and acknowledged ${messages.size} click events.")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClickProcessingJob::class.java)
    }
}
