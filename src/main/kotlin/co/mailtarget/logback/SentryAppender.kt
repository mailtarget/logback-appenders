package co.mailtarget.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import co.mtarget.vesko.EntityNotFoundException
import co.mtarget.vesko.NotAllowedException
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.protocol.Message
import java.net.InetAddress


class SentryAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    var serviceName: String? = ""

    init {
        Sentry.init { options: SentryOptions ->
            options.dsn = "https://085b748ec3f3491aa2c90a3f5c139d4a@o596696.ingest.sentry.io/4503996860203008"
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = 1.0
            // When first trying Sentry it's good to see what the SDK is doing:
            options.isDebug = true
//            options.addIgnoredExceptionForType(EntityNotFoundException::class.java) // add EntityNotFoundException, NotAllowedException
//            options.addIgnoredExceptionForType(NotAllowedException::class.java) // add EntityNotFoundException, NotAllowedException
            options.beforeSend = SentryOptions.BeforeSendCallback { event, hint ->
                when (event.throwable) {
                    is EntityNotFoundException -> null
                    is NotAllowedException -> null
                    else -> event
                }

//                if (event.throwable is EntityNotFoundException) { // add EntityNotFoundException, NotAllowedException
//                    null
//                }
//                event
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
            val event = SentryEvent().also { event ->
                event.message = Message().also {
//                    it.message = "[${host.hostName}/${host.hostAddress}][${evt.loggerName}]\n${evt.message}"
                    it.message = "[$serviceName][${evt.loggerName}] ${evt.formattedMessage}"
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