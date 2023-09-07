package au.com.robin.sms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission

class SmsSend {
    private val LOG_TAG: String = "SmsSend"

    companion object {
        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        fun getSmsManager(context: Context, intent: Intent): SmsManager? {
            val slot = intent.getIntExtra("slot", -1)

            if (slot == -1) {
                return context.getSystemService(SmsManager::class.java)
            }

            val sm: SubscriptionManager? =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            sm?.let { manager ->
                val activeSubscriptions = manager.activeSubscriptionInfoList
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
            }

            return null
        }
    }
}