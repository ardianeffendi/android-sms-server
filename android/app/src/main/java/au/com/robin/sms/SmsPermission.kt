package au.com.robin.sms

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val SMS_SHARED_PREF = "SmsSharedPreference"
const val PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS
const val PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
const val PERMISSION_REQUEST_CODE = 123
const val PERMISSION_ASKED_KEY = "permission_asked"

/**
 * Requests a specific permission from the user, if it hasn't been asked before.
 *
 * @param activity The [Activity] instance where the permission request will be initiated.
 */
fun requestPermission(activity: Activity) {
    // Check if the permission has been asked before
    val permissionAsked = activity.getSharedPreferences(SMS_SHARED_PREF, Context.MODE_PRIVATE)
        .getBoolean(PERMISSION_ASKED_KEY, false)

    if (!permissionAsked) {
        // Permission has not been asked, so request it
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
            PERMISSION_REQUEST_CODE
        )

        // Mark the permission as asked in SharedPreferences
        activity.getSharedPreferences(SMS_SHARED_PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PERMISSION_ASKED_KEY, true)
            .apply()
    }
}

/**
 * Checks if a specific permission is granted.
 *
 * @param context The [Context] to use for checking the permission.
 * @return True if the permission is granted, false otherwise.
 */
fun isPermissionGranted(context: Context): Boolean {
    val sendSmsPermission = ContextCompat.checkSelfPermission(
        context,
        PERMISSION_SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    val readPhoneStatePermission = ContextCompat.checkSelfPermission(
        context,
        PERMISSION_READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    return sendSmsPermission && readPhoneStatePermission
}