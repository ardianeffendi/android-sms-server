package au.com.robin.sms.service

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Model after ntfy-android/WsConnection.kt.
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

    private val globalId = GLOBAL_ID.incrementAndGet()
    private val listenerId = AtomicLong(0)

    init {
        Log.d(TAG, "New connection with global ID $globalId")
    }

    @Synchronized
    override fun start() {
        if (closed || state == State.Connecting || state == State.Connected) {
            Log.d(
                TAG,
                "(gid=$globalId): Not (re-)starting, because connection is marked closed/connecting/connected"
            )
            return
        }
        if (webSocket != null) {
            webSocket!!.close(WS_CLOSE_NORMAL, "")
        }
        state = State.Connecting
        val nextListenerId = listenerId.incrementAndGet()
        val request = requestBuilder(SERVER_URL).build()
        Log.d(TAG, "Opening connection with listener ID $nextListenerId")
        webSocket = client.newWebSocket(request, Listener(nextListenerId))
    }

    @Synchronized
    override fun close() {
        closed = true
        if (webSocket == null) {
            Log.d(
                TAG,
                "(gid=$globalId): Not closing existing connection because there is no active websocket"
            )
            return
        }
        Log.d(TAG, "(gid=$globalId): Closing connection")
        state = State.Disconnected
        webSocket!!.close(WS_CLOSE_NORMAL, "")
        webSocket = null
    }

    // TODO(Implement function `scheduleReconnect()`)

    private inner class Listener(private val id: Long) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            synchronize("onOpen") {
                Log.d(TAG, "(gid=$globalId): Opened connection")
                state = State.Connected
                if (errorCount > 0) {
                    errorCount = 0
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            synchronize("onMessage") {
                Log.d(TAG, "(gid=$globalId, lid=$id): Received message: $text")
                // TODO(Implement a callback to pass the message to UI)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            synchronize("onClosed") {
                Log.w(TAG, "(gid=$globalId, lid=$id): Closed connection")
                state = State.Disconnected
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            synchronize("onFailure") {
                if (response == null) {
                    Log.e(
                        TAG,
                        "(gid=$globalId, lid=$id): Connection failed (response is null): ${t.message}",
                        t
                    )
                } else {
                    Log.e(
                        TAG,
                        "(gid=$globalId, lid=$id): Connection failed (response code ${response.code}, message: ${response.message}): ${t.message}",
                        t
                    )
                }
                if (closed) {
                    Log.d(
                        TAG,
                        "(gid=$globalId, lid=$id): Connection marked as closed. Not retrying."
                    )
                    return@synchronize
                }
                state = State.Disconnected
                errorCount++
                val retrySeconds = RETRY_SECONDS.getOrNull(errorCount) ?: RETRY_SECONDS.last()
                // Call scheduleReconnect() - not yet implemented
            }
        }

        private fun synchronize(tag: String, fn: () -> Unit) {
            synchronized(this) {
                if (listenerId.get() == id) {
                    fn()
                } else {
                    Log.w(
                        TAG,
                        "(gid=$globalId): Skipping synchronized block '$tag' because listener ID does not match ${listenerId.get()}"
                    )
                }
            }
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
        private val GLOBAL_ID = AtomicLong(0)

        fun requestBuilder(url: String): Request.Builder {
            return Request.Builder().url(url)
        }
    }

}