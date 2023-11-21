package au.com.robin.sms.service

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Modelled after ntfy-android/WsConnection.kt.
 * (https://github.com/binwiederhier/ntfy-android/blob/main/app/src/main/java/io/heckel/ntfy/service/WsConnection.kt)
 */
class WsConnection : Connection {
    private val SERVER_URL = "ws://192.168.1.106:8080"
    private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(1, TimeUnit.MINUTES).build()
    private var errorCount = 0
    private var webSocket: WebSocket? = null
    private var state: State? = null
    private var closed = false


    init {
        Log.d(TAG, "New connection initialised...")
    }

    override fun start() {
        if (closed || state == State.Connecting || state == State.Connected) {
            Log.d(
                TAG, "Not (re-)starting, because connection is marked closed/connecting/connected"
            )
            return
        }
        if (webSocket != null) {
            webSocket!!.close(WS_CLOSE_NORMAL, "")
        }
        state = State.Connecting
        val request = requestBuilder(SERVER_URL).build()
        Log.d(TAG, "Opening connection...")
        webSocket = client.newWebSocket(request, Listener())
    }

    override fun close() {
        closed = true
        if (webSocket == null) {
            Log.d(
                TAG, "Not closing existing connection because there is no active websocket"
            )
            return
        }
        Log.d(TAG, "Closing connection")
        state = State.Disconnected
        webSocket!!.close(WS_CLOSE_NORMAL, "")
        webSocket = null
    }

    // TODO(Implement function `scheduleReconnect()`)

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "Opened connection")
            state = State.Connected
            if (errorCount > 0) {
                errorCount = 0
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            // TODO(Implement a callback to pass the message to UI)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.w(TAG, "Closed connection")
            state = State.Disconnected
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (response == null) {
                Log.e(
                    TAG, "Connection failed (response is null): ${t.message}", t
                )
            } else {
                Log.e(
                    TAG,
                    "Connection failed (response code ${response.code}, message: ${response.message}): ${t.message}",
                    t
                )
            }
            if (closed) {
                Log.d(
                    TAG, "Connection marked as closed. Not retrying."
                )
                return
            }
            state = State.Disconnected
            errorCount++
            val retrySeconds = RETRY_SECONDS.getOrNull(errorCount) ?: RETRY_SECONDS.last()
            // Call scheduleReconnect() - not yet implemented
        }
    }

    internal enum class State {
        Scheduled, Connecting, Connected, Disconnected
    }

    companion object {
        private const val TAG = "SMSWsConnection"
        private const val RECONNECT_TAG = "WsReconnect"
        private const val WS_CLOSE_NORMAL = 1000
        private val RETRY_SECONDS = listOf(5, 10, 15, 20, 30, 45, 60, 120)

        fun requestBuilder(url: String): Request.Builder {
            return Request.Builder().url(url)
        }
    }

}