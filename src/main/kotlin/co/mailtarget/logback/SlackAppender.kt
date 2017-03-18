package co.mailtarget.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Layout
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.UnsynchronizedAppenderBase
import co.mailtarget.logback.util.HttpUtil
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.StringWriter
import java.lang.Exception
import java.net.URLEncoder
import java.util.*

/**
*
* @author masasdani
* @since 3/18/17
*/
class SlackAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {

    var webhookUri: String? = null
    var token: String? = null
    var channel: String? = null
    var username: String? = null
    var emoji: String? = null
        set(emojiArg) {
            field = emojiArg
            if(!this.emoji!!.startsWith(":")){
                emoji = ":" + emoji
            }
            if(!this.emoji!!.endsWith(":")){
                emoji += ":"
            }
        }

    var layout = defaultLayout
    var timeout = 30000

    override fun append(evt: ILoggingEvent) {
        try {
            if (webhookUri != null) {
                sendMessageWithWebhookUri(evt)
            } else {
                sendMessageWithToken(evt)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            addError("Error posting log to Slack.com ($channel): $evt", ex)
        }

    }

    @Throws(IOException::class)
    private fun sendMessageWithWebhookUri(evt: ILoggingEvent) {
        val parts = layout.doLayout(evt).split("\n".toRegex(), 2)

        val message = HashMap<String, Any>()
        if(channel != null) message.put("channel", channel!!)
        if(username != null) message.put("username", username!!)
        if(emoji != null) message.put("icon_emoji", emoji!!)
        message.put("text", parts[0])

        // Send the lines below the first line as an attachment.
        if (parts.size > 1) {
            val attachment = HashMap<String, String>()
            attachment.put("text", parts[1])
            message.put("attachments", Arrays.asList<Map<String, String>>(attachment))
        }

        val objectMapper = ObjectMapper()
        val bytes = objectMapper.writeValueAsBytes(message)

        HttpUtil.postMessage(webhookUri!!, "application/json", bytes, timeout)
    }

    @Throws(IOException::class)
    private fun sendMessageWithToken(evt: ILoggingEvent) {
        val requestParams = StringWriter()
        requestParams.append("token=").append(token).append("&")

        val parts = layout.doLayout(evt).split("\n".toRegex(), 2).toTypedArray()
        requestParams.append("text=").append(URLEncoder.encode(parts[0], "UTF-8")).append('&')

        // Send the lines below the first line as an attachment.
        if (parts.size > 1) {
            val attachment = HashMap<String, String>()
            attachment.put("text", parts[1])
            val attachments = listOf<Map<String, String>>(attachment)
            val json = ObjectMapper().writeValueAsString(attachments)
            requestParams.append("attachments=").append(URLEncoder.encode(json, "UTF-8")).append('&')
        }
        if (channel != null) {
            requestParams.append("channel=").append(URLEncoder.encode(channel!!, "UTF-8")).append('&')
        }
        if (username != null) {
            requestParams.append("username=").append(URLEncoder.encode(username!!, "UTF-8")).append('&')
        }
        if (emoji != null) {
            requestParams.append("icon_emoji=").append(URLEncoder.encode(emoji!!, "UTF-8"))
        }

        val bytes = requestParams.toString().toByteArray(charset("UTF-8"))
        HttpUtil.postMessage(API_URL, "application/x-www-form-urlencoded", bytes, timeout)
    }

    companion object {
        private val API_URL = "https://slack.com/api/chat.postMessage"
        private val defaultLayout: Layout<ILoggingEvent> = object : LayoutBase<ILoggingEvent>() {
            override fun doLayout(event: ILoggingEvent): String {
                return "-- [" + event.level + "]" +
                        event.loggerName + " - " +
                        event.formattedMessage.replace("\n".toRegex(), "\n\t")
            }
        }
    }

}