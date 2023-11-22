package au.com.robin.sms.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

private const val TAG = "StartReceiver"

/**
 * BroadcastReceiver to start the SubscriberService on device boot if the service was previously started.
 *
 * This BroadcastReceiver listens for the `ACTION_BOOT_COMPLETED` broadcast and checks whether the
 * SubscriberService was in a started state. If so, it starts the service accordingly.
 */
class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, SubscriberService::class.java).also {
                it.action = SubscriberService.Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d(TAG, "Starting the service in >= 26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                Log.d(TAG, "Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }
}