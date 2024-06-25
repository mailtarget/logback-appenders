package co.mtarget.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Layout
import ch.qos.logback.core.UnsynchronizedAppenderBase
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.protocol.Message
import java.net.InetAddress

class SentryAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    var serviceName: String? = ""
    var webhookUri: String? = ""
    var layout: Layout<ILoggingEvent>? = null

    override fun start() {
        val host = InetAddress.getLocalHost()
        Sentry.init { options: SentryOptions ->
            options.dsn = webhookUri!!
            options.tracesSampleRate = 1.0
            options.isDebug = true
            options.serverName = "[$serviceName] ${host.hostName}/${host.hostAddress}"
            options.isEnableDeduplication = false
        }
        super.start()
    }

    override fun append(evt: ILoggingEvent) {
        try {
            sendMessage(evt)
        } catch (ex: Exception) {
            ex.printStackTrace()
            addError("Error posting log to Sentry : $evt", ex)
        }
    }

    fun sendMessage(evt: ILoggingEvent) {
        val host = InetAddress.getLocalHost()
        if (serviceName.isNullOrEmpty()) serviceName = "${host.hostName}/${host.hostAddress}"+evt.loggerName

        val formattedMessage = layout?.doLayout(evt) ?: evt.formattedMessage

        if (evt.level == Level.ERROR || evt.level == Level.WARN) {
            val event = SentryEvent().also { event ->
                event.message = Message().also {
                    it.message = "[$serviceName][${evt.loggerName}] $formattedMessage"
                }
                event.level = when (evt.level) {
                    Level.ERROR -> SentryLevel.ERROR
                    Level.WARN -> SentryLevel.WARNING
                    else -> SentryLevel.INFO
                }
                event.logger = SentryAppender::class.java.name
                event.fingerprints = listOf(evt.loggerName, evt.message)
                event.serverName = "${host.hostName}/${host.hostAddress}"
            }
            Sentry.captureEvent(event)
        }
    }
}
