package au.com.robin.sms.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import au.com.robin.sms.db.Repository

/**
 * ViewModel class for managing and providing access to the message data.
 *
 * @param repo The repository responsible for providing the message data.
 */
class MessageViewModel(private val repo: Repository) : ViewModel() {

    /**
     * @return [LiveData] instance representing the message data.
     */
    fun message(): LiveData<String> {
        return repo.getMessageLiveData()
    }
}

/**
 * ViewModelProvider.Factory implementation for creating instances of the [MessageViewModel].
 *
 * @param repo The repository used to initialize the [MessageViewModel].
 */
class MessageViewModelFactory(private val repo: Repository) : ViewModelProvider.Factory {

    /**
     * Creates an instance of the specified [ViewModel] class.
     *
     * @param modelClass The class of the [ViewModel] to create.
     * @return An instance of the [ViewModel].
     * @throws IllegalArgumentException if the provided [modelClass] is unknown.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        with(modelClass) {
            // Check if the requested ViewModel class is MessageViewModel
            when {
                isAssignableFrom(MessageViewModel::class.java) -> MessageViewModel(repo) as T
                else -> throw IllegalArgumentException("Unknown viewModel class $modelClass")
            }
        }
}
