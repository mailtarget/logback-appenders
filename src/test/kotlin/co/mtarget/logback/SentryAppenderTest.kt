package co.mtarget.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import org.junit.Test

class SentryAppenderTest {

    private val sentryAppender = SentryAppender()

    @Test
    fun `should send message`() {
        val event = LoggingEvent()
        event.message = """
            test
        """.trimIndent()
        event.level = Level.ERROR
        event.loggerName = "form-service-855bbc7bfd-gl4ll"
        println("event $event")
        sentryAppender.sendMessage(event)
    }

    @Test
    fun `should send message 2`() {
        val event = LoggingEvent()
        event.message = """
            [EventbusCoroutineProxy] 500, RECIPIENT_FAILURE, invalid hexadecimal representation of an ObjectId: [sitemap.xml]
        """.trimIndent()
        event.level = Level.ERROR
        event.loggerName = "api-gateway-v2-55bd64db5c-4tc9q"
        println("event $event")
        sentryAppender.sendMessage(event)
    }

    @Test
    fun `should send message 3`() {
        val event = LoggingEvent()
        event.message = """
            org.mongodb.morphia.mapping.MappingException: The DBOBbject does not contain a className key. Determining entity type is impossible.
            org.mongodb.morphia.mapping.EmbeddedMapper.fromDBObject(EmbeddedMapper.java:76)
            org.mongodb.morphia.mapping.Mapper.readMappedField(Mapper.java:850)
            org.mongodb.morphia.mapping.Mapper.fromDb(Mapper.java:282)
            org.mongodb.morphia.mapping.Mapper.fromDBObject(Mapper.java:193)
            org.mongodb.morphia.query.MorphiaIterator.convertItem(MorphiaIterator.java:134)
            org.mongodb.morphia.query.MorphiaIterator.processItem(MorphiaIterator.java:146)
            org.mongodb.morphia.query.MorphiaIterator.next(MorphiaIterator.java:117)
            org.mongodb.morphia.query.QueryImpl.asList(QueryImpl.java:147)
            org.mongodb.morphia.query.QueryImpl.asList(QueryImpl.java:139)
            org.mongodb.morphia.DatastoreImpl.get(DatastoreImpl.java:1021)
            co.mtarget.transactional.service.VisitService.handleAutomationCallback(VisitService.kt:113)
            co.mtarget.transactional.service.VisitService.linkTracker(VisitService.kt:109)
            kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
            kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
            kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
        """.trimIndent()
        event.level = Level.ERROR
        event.loggerName = "api-gateway-v2-55bd64db5c-4tc9q"
        println("event $event")
        sentryAppender.sendMessage(event)
    }

    @Test
    fun `should not send message because log info`() {
        val event = LoggingEvent()
        event.message = """
            Info Aja Sih
        """.trimIndent()
        event.level = Level.INFO
        event.loggerName = "api-gateway-v2-55bd64db5c-4tc9q"
        println("event $event")
        sentryAppender.sendMessage(event)
    }
}