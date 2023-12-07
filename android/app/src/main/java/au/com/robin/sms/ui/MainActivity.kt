package au.com.robin.sms.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import au.com.robin.sms.app.Application
import au.com.robin.sms.develop.R
import au.com.robin.sms.service.ServiceState
import au.com.robin.sms.service.SubscriberService
import au.com.robin.sms.service.getServiceState
import au.com.robin.sms.util.PERMISSION_REQUEST_CODE
import au.com.robin.sms.util.arePermissionsGranted
import au.com.robin.sms.util.getSmsManager
import org.json.JSONObject

// Constants
private const val SIM_SLOT = "slot"
private const val SIM_SLOT_ONE = 0
private const val SIM_SLOT_TWO = 1

class MainActivity : AppCompatActivity() {
    /**
     * `by viewModels` is a property delegate provided by the activity-ktx libraries.
     * It simplifies the process of obtaining a ViewModel instance tied to the lifecycle.
     * The factory is needed to provide additional parameters to the ViewModel constructor as dependency.
     * In this case, the dependency is `repository`.
     */
    private val viewModel by viewModels<MessageViewModel> {
        MessageViewModelFactory((application as Application).repository)
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialised the variables tied to the views here
        // since they are not in use anywhere
        val btSend = findViewById<Button>(R.id.bt_send)
        val etPhoneNo = findViewById<EditText>(R.id.et_phoneNo)
        val etMessage = findViewById<EditText>(R.id.et_message)


        if (arePermissionsGranted(this)) {
            findViewById<Button>(R.id.bt_start_service).let {
                it.setOnClickListener {
                    actionService(SubscriberService.Actions.START)
                    Log.d("MainActivity", "Start the foreground service on demand")
                }
            }

            findViewById<Button>(R.id.bt_stop_service).let {
                it.setOnClickListener {
                    actionService(SubscriberService.Actions.STOP)
                    Log.d("MainActivity", "Stop the foreground service on demand")
                }
            }

            btSend.setOnClickListener {
                val phoneNo = etPhoneNo.text?.toString()
                val message = etMessage.text?.toString()

                val intent = Intent()
                // Default to SIM slot 1 for now
                // This is supposed to be passed in with a value that user chooses.
                intent.putExtra(SIM_SLOT, SIM_SLOT_ONE)
                val smsManager = getSmsManager(this, intent)
                smsManager?.sendTextMessage(phoneNo, null, message, null, null)
            }
        } else {
            au.com.robin.sms.util.requestPermissions(this)
        }

        viewModel.message().observe(this) {
            it?.let { msg ->
                try {
                    val json = JSONObject(msg)
                    val phoneNo = json.getString("phoneNumber")
                    val message = json.getString("message")
                    etPhoneNo.text =
                        Editable.Factory.getInstance().newEditable(phoneNo)
                    etMessage.text =
                        Editable.Factory.getInstance().newEditable(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        webViewRequest()
    }

    /**
     * Performs an action on the SubscriberService based on the provided action type.
     *
     * This function creates an intent for the SubscriberService with the specified action, allowing
     * the service to start or stop based on the provided action. It checks the current state of the
     * service to avoid redundant calls when stopping the service that is already in a stopped state.
     * On Android Oreo (API level 26) and higher, it uses `startForegroundService` for compatibility.
     *
     * @param action The action to perform on the SubscriberService (START or STOP).
     */
    private fun actionService(action: SubscriberService.Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == SubscriberService.Actions.STOP) return
        Intent(this, SubscriberService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }

    /**
     * This function demonstrates toggling the visibility of a WebView and making a POST request to a URL.
     */
    private fun webViewRequest() {
        val webView = findViewById<WebView>(R.id.webView)
        val toggleButton = findViewById<Button>(R.id.bt_webview)

        toggleButton.setOnClickListener {
            if (webView.visibility == View.GONE) {
                webView.visibility = View.VISIBLE

                val postData = "Hello, world!"
                val url = "http://192.168.1.106:8080/simple-post"
                webView.postUrl(url, postData.toByteArray())
            } else {
                webView.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST_CODE) {
            Toast.makeText(this, "Permission denied. SMS can't be sent!", Toast.LENGTH_SHORT).show()
        }
    }
}