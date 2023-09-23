package au.com.robin.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission

/**
 * Get the instance of SmsManager that allows SMS operations such as sending data and text.
 * This function also checks if a device supports multiple active subscriptions at once by
 * checking its Subscription Service (starting from SDK version 22).
 *
 * @param context An instance of the Context class, used to obtain system services.
 * @param intent An instance of the Intent class, that has information regarding the SIM slot
 *               user has chosen. If no such information found (single SIM device), it defaults to -1.
 * @return An instance of SmsManager or null.
 */
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun getSmsManager(context: Context, intent: Intent): SmsManager? {
    val slot = intent.getIntExtra("slot", -1)

    if (slot == -1) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Devices with SDK version >= 31
            context.getSystemService(SmsManager::class.java)
        } else {
            // Devices with SDK version < 31
            SmsManager.getDefault()
        }
    }

    //
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val sm: SubscriptionManager? =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        sm?.activeSubscriptionInfoList?.forEach { subscriptionInfo ->
            if (subscriptionInfo.simSlotIndex == slot) {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                        .createForSubscriptionId(subscriptionInfo.subscriptionId)
                } else {
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                }
            }
        }
        null // If the slot was not found
    } else {
        // Android does not support multi-SIM from the SDK for version < 22
        // For now, this is default to SmsManager.getDefault()
        // Alternative for this is,
        // https://stackoverflow.com/questions/14517338/android-check-whether-the-phone-is-dual-sim
        SmsManager.getDefault()
    }
}