package co.mtarget.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
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

    init {
        Sentry.init { options: SentryOptions ->
            options.dsn = webhookUri!!
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = 1.0
            // When first trying Sentry it's good to see what the SDK is doing:
            options.isDebug = true
//            options.addIgnoredExceptionForType(EntityNotFoundException::class.java) // add EntityNotFoundException, NotAllowedException
//            options.addIgnoredExceptionForType(NotAllowedException::class.java) // add EntityNotFoundException, NotAllowedException
            options.beforeSend = SentryOptions.BeforeSendCallback { event, hint ->
                if (event.throwable?.stackTrace?.get(0).toString().contains("Request not allowed")) return@BeforeSendCallback null
                when (event.throwable?.stackTrace?.get(0)!!::class.simpleName) {
                    "EntityNotFoundException" -> null
                    "NotAllowedException" -> null
                    else -> event
                }
            }
        }
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
        if (serviceName.isNullOrEmpty()) serviceName = evt.loggerName

        if (evt.level == Level.ERROR || evt.level == Level.WARN) {
            if (evt.message.contains("not found or deleted")) return

            val event = SentryEvent().also { event ->
                event.message = Message().also {
//                    it.message = "[${host.hostName}/${host.hostAddress}][${evt.loggerName}]\n${evt.message}"
                    it.message = "[$serviceName][${evt.loggerName}] ${evt.message}"
                }
                event.level = when (evt.level) {
                    Level.ERROR -> SentryLevel.ERROR
                    Level.WARN -> SentryLevel.WARNING
                    else -> SentryLevel.INFO
                }
                event.logger = evt.loggerName
                event.fingerprints = listOf(evt.loggerName, evt.message)
                event.serverName = "${host.hostName}/${host.hostAddress}"
            }
            Sentry.captureEvent(event)
        }
    }
}