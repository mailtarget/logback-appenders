package co.mtarget.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Layout
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import kotlin.jvm.Throws

/**
 *
 * Enabled push notitication to slack channel
 *
 * @author masasdani
 * @since 3/18/17
 */
class DiscordAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

    var webhookUri: String? = null
    var serverName: String? = null
    var serviceName: String? = null

    var layout = defaultLayout
    var timeout = 30000

    override fun append(evt: ILoggingEvent) {
        try {
            sendMessageWithWebhookUri(evt)
        } catch (ex: Exception) {
            ex.printStackTrace()
            addError("Error posting log to Discord ($webhookUri): $evt", ex)
        }

    }

    @Throws(IOException::class)
    private fun sendMessageWithWebhookUri(evt: ILoggingEvent) {
        val parts = layout.doLayout(evt).split("\n".toRegex(), 2)
        val host = InetAddress.getLocalHost()

        if (serverName.isNullOrEmpty()) serverName = host.hostName
        if (serviceName.isNullOrEmpty()) serviceName = evt.loggerName
        val author = HashMap<String, Any?>()
        author["name"] = serverName + "/${host.hostAddress}"
        author["url"] = null
        author["icon_url"] = "https://i.imgur.com/R66g1Pe.jpg"

        val embed = HashMap<String, Any?>()
        embed["title"] = serviceName
        embed["url"] = null
        embed["description"] = "[${evt.loggerName}] ${evt.message}"
        embed["color"] = 16711680
        embed["author"] = author
        embed["fields"] = null
        embed["footer"] = null
        embed["thumbnail"] = mapOf(("url" to "https://upload.wikimedia.org/wikipedia/commons/3/38/4-Nature-Wallpapers-2014-1_ukaavUI.jpg"))
        embed["image"] = mapOf(("url" to "https://upload.wikimedia.org/wikipedia/commons/5/5a/A_picture_from_China_every_day_108.jpg"))

        val message = HashMap<String, Any>()
        message["content"] = ""
        message["username"] = "Error Notifier"
        message["embeds"] = listOf(embed)

        val messageString = ObjectMapper().writeValueAsString(message)
        postStringMessage(webhookUri!!, "application/json", messageString, timeout)
    }

    @Throws(IOException::class)
    fun postStringMessage(uri: String, contentType: String, message: String, timeout: Int) {
        val conn = URL(uri).openConnection() as HttpURLConnection
        conn.connectTimeout = timeout
        conn.readTimeout = timeout
        conn.doOutput = true
        conn.requestMethod = "POST"
        conn.setFixedLengthStreamingMode(message.length)
        conn.setRequestProperty("Content-Type", contentType)
        conn.setRequestProperty("User-Agent", "Mozilla")
        val bw = BufferedWriter(OutputStreamWriter(conn.outputStream))
        bw.write(message)
        bw.close()
    }

    companion object {
        private val defaultLayout: Layout<ILoggingEvent> = object : LayoutBase<ILoggingEvent>() {
            override fun doLayout(event: ILoggingEvent): String {
                return "-- [" + event.level + "]" +
                        event.loggerName + " - " +
                        event.formattedMessage.replace("\n".toRegex(), "\n\t")
            }
        }
    }

}