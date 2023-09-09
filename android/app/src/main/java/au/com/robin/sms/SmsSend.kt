package au.com.robin.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresPermission

class SmsSend {

    companion object {
        private const val LOG_TAG: String = "SmsSend"
        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        fun getSmsManager(context: Context, intent: Intent): SmsManager? {
            val slot = intent.getIntExtra("slot", -1)

            if (slot == -1) {
                return context.getSystemService(SmsManager::class.java)
            }

            val sm: SubscriptionManager? =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (sm == null) {
                Log.e(LOG_TAG, "SubscriptionManager is not supported!")
                return null
            }

            val activeSubscriptions = sm.activeSubscriptionInfoList
            activeSubscriptions?.forEach { subscriptionInfo ->
                if (subscriptionInfo.simSlotIndex == slot) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                            .createForSubscriptionId(subscriptionInfo.subscriptionId)
                    } else {
                        SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.subscriptionId)
                    }
                }
            }

            Log.e(LOG_TAG, "Sim slot $slot not found")
            return null
        }
    }
}