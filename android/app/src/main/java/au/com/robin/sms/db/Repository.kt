package au.com.robin.sms.db

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Repository class responsible for managing the storage and retrieval of a message using SharedPreferences.
 *
 * @property sharedPrefs The [SharedPreferences] instance used for storing the message.
 */
class Repository(private val sharedPrefs: SharedPreferences) {
    // LiveData to observe changes in the stored message
    private val _messageLiveData = MutableLiveData<String>()

    /**
     * Gets a [LiveData] instance for observing changes in the stored message.
     *
     * @return [LiveData] instance containing the stored message.
     */
    fun getMessageLiveData(): LiveData<String> {
        // Try to retrieve the stored message from SharedPreferences
        sharedPrefs.getString(SHARED_PREFS_MESSAGE_KEY, null)?.let {
            // If the message exists, update the LiveData and return it
            _messageLiveData.postValue(it)
            return _messageLiveData
        }

        // If the message is not found, return the LiveData instance as is
        return _messageLiveData
    }

    /**
     * Adds a new message to the repository, updating both SharedPreferences and LiveData.
     *
     * @param message The message to be added.
     */
    fun addMessage(message: String) {
        // Save the message locally in SharedPreferences
        saveMessageLocally(sharedPrefs, message)
        // Update the LiveData with the new message
        _messageLiveData.postValue(message)
    }

    /**
     * Saves a message locally in SharedPreferences.
     *
     * @param sharedPrefs The [SharedPreferences] instance.
     * @param message The message to be saved.
     */
    private fun saveMessageLocally(sharedPrefs: SharedPreferences, message: String) {
        // Use SharedPreferences editor to save the message
        sharedPrefs.edit().putString(SHARED_PREFS_MESSAGE_KEY, message).apply()
    }

    // Companion object containing constants and methods for creating an instance of the Repository
    companion object {
        // Constants for SharedPreferences
        const val SHARED_PREFS_ID = "MainPreferences"
        const val SHARED_PREFS_MESSAGE_KEY = "MessageKeySharedPref"

        // Singleton instance of the Repository
        private var instance: Repository? = null

        /**
         * Gets an instance of the [Repository] using the application context.
         *
         * @param context The application context.
         * @return An instance of the [Repository].
         */
        fun getInstance(context: Context): Repository {
            // Create a SharedPreferences instance using the provided context
            val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_ID, Context.MODE_PRIVATE)
            val newInstance = instance ?: Repository(sharedPrefs)
            instance = newInstance
            return newInstance
        }
    }
}
