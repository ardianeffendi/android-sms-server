package au.com.robin.sms.app

import android.app.Application
import au.com.robin.sms.db.Repository

/**
 * Custom Application class responsible for initializing and providing a singleton instance of the [Repository].
 */
class Application : Application() {

    /**
     * Lazily initializes and provides a singleton instance of the [Repository].
     * The repository is initialized using the application context.
     */
    val repository by lazy {
        Repository.getInstance(applicationContext)
    }
}
