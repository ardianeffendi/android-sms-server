package au.com.robin.sms.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val SMS_SHARED_PREF = "SmsSharedPreference"
const val PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS
const val PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
const val PERMISSION_REQUEST_CODE = 123
const val PERMISSION_ASKED_KEY = "permission_asked"

/**
 * Requests necessary permissions for SMS and foreground service based on API level.
 *
 * @param activity The [Activity] to use for requesting permissions.
 */
fun requestPermissions(activity: Activity) {
    val permissionsToRequest = mutableListOf<String>()

    // Check SMS permission
    if (isPermissionNeeded(activity, PERMISSION_SEND_SMS)) {
        permissionsToRequest.add(PERMISSION_SEND_SMS)
    }

    // Check READ_PHONE_STATE permission
    if (isPermissionNeeded(activity, PERMISSION_READ_PHONE_STATE)) {
        permissionsToRequest.add(PERMISSION_READ_PHONE_STATE)
    }

    // Check FOREGROUND_SERVICE_DATA_SYNC permission [starting from Android 34]
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        isPermissionNeeded(activity, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
    ) {
        permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
    }

    if (permissionsToRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            activity,
            permissionsToRequest.toTypedArray(),
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
 * Checks whether a specific permission is needed based on the Android API level.
 *
 * On devices running Android 6.0 (Marshmallow) and higher, it verifies if the specified permission
 * is not granted. On devices with a lower API level, all permissions are considered granted
 * at installation time.
 *
 * @param activity The [Activity] context to use for checking the permission.
 * @param permission The permission to check.
 * @return True if the permission is needed (not granted), false otherwise.
 */
fun isPermissionNeeded(activity: Activity, permission: String): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
}

/**
 * Checks if all permissions defined are granted.
 *
 * @param context The [Context] to use for checking the permission.
 * @return True if the permission is granted, false otherwise.
 */
fun arePermissionsGranted(context: Context): Boolean {
    val sendSmsPermission = ContextCompat.checkSelfPermission(
        context,
        PERMISSION_SEND_SMS
    ) == PackageManager.PERMISSION_GRANTED

    val readPhoneStatePermission = ContextCompat.checkSelfPermission(
        context,
        PERMISSION_READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    // Only applicable starting from Android 34
    val foregroundServicePermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    return sendSmsPermission && readPhoneStatePermission && foregroundServicePermission
}