package au.com.robin.sms.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import au.com.robin.sms.db.Repository
import au.com.robin.sms.develop.R
import au.com.robin.sms.ui.MainActivity

private const val TAG = "SubscriberService"
private const val WAKE_LOCK_TAG = "SubscriberService::lock"
private const val NOTIFICATION_SERVICE_ID = 1
private const val NOTIFICATION_GROUP_ID = "com.robin.sms.NOTIFICATION_GROUP_SERVICE"

/**
 * The `SubscriberService` class manages the foreground service for instant delivery of SMS messages.
 * It handles starting and stopping the service, acquiring necessary resources, and updating the service state.
 *
 * This service is responsible for maintaining a persistent connection to the server, receiving incoming
 * notifications, and dispatching them to the application. It utilizes a wake lock to prevent doze mode
 * from impacting its functionality.
 *
 * @property wakeLock A wake lock to ensure the device remains active and the service can perform its tasks.
 * @property isServiceStarted A flag indicating whether the service is currently started.
 *
 * References:
 * - https://github.com/binwiederhier/ntfy-android/blob/main/app/src/main/java/io/heckel/ntfy/service/SubscriberService.kt#L360
 * - https://robertohuertas.com/2019/06/29/android_foreground_services/
 */
class SubscriberService : Service() {
    private var wsConnection: WsConnection? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    override fun onBind(p0: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Subscriber service has been created!")
        val notification = createNotification()
        startForeground(NOTIFICATION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")

        // Initialise repository
        val repository = Repository.getInstance(applicationContext)

        if (intent != null) {
            Log.d(TAG, "Using an intent with action ${intent.action}")
            when (intent.action) {
                Actions.START.name -> startService(repository)
                Actions.STOP.name -> stopService()
                else -> Log.w(TAG, "This should never happen. No action in the received intent")
            }
        } else {
            Log.d(TAG, "with a null intent. It has been probably restarted by the system.")
        }
        return START_STICKY // restart if the system kills the service
    }

    override fun onDestroy() {
        Log.d(TAG, "Subscriber service has been destroyed")
        stopService()
        super.onDestroy()
    }

    /**
     * Called when the task associated with the service is removed from the recent apps list.
     *
     * This method is triggered when the user swipes away the app from the recent apps list, and it
     * reschedules the task to ensure that the service is restarted after being removed. It creates
     * an intent to restart the `SubscriberService` and schedules it with an `AlarmManager` to be
     * triggered after a short delay (1 second).
     *
     * @param rootIntent The original intent that was used to launch the service.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, SubscriberService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    /**
     * Starts the foreground service, acquiring necessary resources, and updating the service state.
     *
     * This method is responsible for initiating the SubscriberService, setting the service state to indicate that
     * the service is started, and acquiring a wake lock to prevent doze mode from impacting the service.
     * It checks if the service is already started to avoid redundant calls.
     *
     * Note: A wake lock is acquired to ensure that the device remains active and the service can perform its tasks,
     * even when the device is in a low-power state. The wake lock is released when the service is stopped.
     */
    private fun startService(repository: Repository) {
        if (isServiceStarted) return

        Log.d(TAG, "Starting the foreground service task")
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // To prevent doze mode impacting our service
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
                    acquire()
                }
            }
        // Open connection
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        wsConnection = WsConnection({ text: String -> repository.addMessage(text)}, alarmManager)
        wsConnection?.start()
    }

    /**
     * Stops the foreground service, releasing resources and updating the service state.
     *
     * This method is responsible for gracefully stopping the SubscriberService, releasing the wake lock,
     * stopping the foreground service, and updating the service state to indicate that the service is stopped.
     * It also handles exceptions that might occur during the service stop process.
     *
     * Note: The wake lock is released to ensure that the device can go back to a low-power state when the service is stopped.
     * If the Android version is Nougat (API level 24) or higher, it uses `stopForeground(STOP_FOREGROUND_REMOVE)`
     * to remove the service notification from the status bar without stopping the service.
     * For earlier versions, `stopForeground(true)` is used to stop the service while keeping the notification visible.
     */
    private fun stopService() {
        Log.d(TAG, "Stopping foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            stopSelf()
            wakeLock = null
        } catch (e: Exception) {
            Log.d(TAG, "Service stopped without being started: ${e.message}")
        }

        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
        wsConnection?.close()
    }

    /**
     * Creates a notification for the SMS Subscriber Service.
     *
     * This function constructs a notification for the SMS Subscriber Service, ensuring compatibility
     * with Android versions 14 to 33. It sets up a notification channel on devices running Android Oreo
     * or later. The notification provides information about the service and allows users to open the
     * main activity when clicked.
     *
     * @return A configured [Notification] object.
     */
    private fun createNotification(): Notification {
        val notificationChannelId = "SMS-subscriber"

        // Create a notification channel for devices running Android Oreo or later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Subscription Service",
                NotificationManager.IMPORTANCE_LOW // Not audibly intrusive
            ).let {
                it.setShowBadge(false)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create a pending intent to open the main activity when the notification is clicked
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
                this,
                notificationChannelId
            ) else NotificationCompat.Builder(this)

        return builder
            .setContentTitle("SMS Subscriber Service")
            .setContentText("Listening to incoming message from server")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(null)
            .setShowWhen(false) // Do not show date or time
            .setOngoing(true) // Since Android 13, foreground notifications can be swiped away
            .setGroup(NOTIFICATION_GROUP_ID) // Do not group with other notifications
            .build()
    }

    enum class Actions {
        START,
        STOP
    }
}