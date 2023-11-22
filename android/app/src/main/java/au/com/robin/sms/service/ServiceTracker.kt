package au.com.robin.sms.service

import android.content.Context
import android.content.SharedPreferences

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val NAME = "SMS_SERVICE_KEY"
private const val KEY = "SMS_SERVICE_STATE"

/**
 * Sets the service state in shared preferences.
 *
 * This function is responsible for updating the service state in shared preferences. It takes a [Context] and a
 * [ServiceState] as parameters, retrieves the shared preferences, and stores the service state.
 *
 * @param context The context used to access shared preferences.
 * @param state The service state to be stored.
 */
fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(KEY, state.name)
        it.apply()
    }
}

/**
 * Gets the service state from shared preferences.
 *
 * This function retrieves the service state from shared preferences. It takes a [Context] as a parameter,
 * retrieves the shared preferences, and returns the stored service state.
 *
 * @param context The context used to access shared preferences.
 * @return The retrieved [ServiceState].
 */
fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value = sharedPrefs.getString(KEY, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value!!)
}

/**
 * Retrieves the shared preferences.
 *
 * This private function is responsible for getting the shared preferences using a [Context].
 *
 * @param context The context used to access shared preferences.
 * @return The shared preferences.
 */
private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(NAME, 0)
}