package au.com.robin.sms.app

import android.app.Application
import au.com.robin.sms.db.Repository

/**
 * Custom Application class responsible for initializing and providing a singleton instance of the [Repository].
 */
class Application : Application() {

    /**
     * Lazily initializes and provides a singleton instance of the [Repository].
     *
     * `lazy` delegate is used to let the `repository` be initialized the first time it is accessed.
     * Furthermore, upon subsequent accesses, the previously computed value will be used.
     * This helps in delaying the cost of initializing an object until it is actually needed.
     *
     * The repository is initialized using the application context.
     */
    val repository by lazy {
        Repository.getInstance(applicationContext)
    }
}
