package co.mailtarget.logback.appenders.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author masasdani
 * @since 3/18/17
 */
object HttpUtil {

    @Throws(IOException::class)
    fun postMessage(uri: String, contentType: String, bytes: ByteArray, timeout: Int) {
        val conn = URL(uri).openConnection() as HttpURLConnection
        conn.connectTimeout = timeout
        conn.readTimeout = timeout
        conn.doOutput = true
        conn.requestMethod = "POST"
        conn.setFixedLengthStreamingMode(bytes.size)
        conn.setRequestProperty("Content-Type", contentType)

        val os = conn.outputStream
        os.write(bytes)

        os.flush()
        os.close()
    }

}