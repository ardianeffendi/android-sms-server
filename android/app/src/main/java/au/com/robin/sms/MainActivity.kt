package au.com.robin.sms

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import au.com.robin.sms.develop.R
import au.com.robin.sms.service.ServiceState
import au.com.robin.sms.service.SubscriberService
import au.com.robin.sms.service.WsConnection
import au.com.robin.sms.service.getServiceState

// Constants
private const val SIM_SLOT = "slot"
private const val SIM_SLOT_ONE = 0
private const val SIM_SLOT_TWO = 1

class MainActivity : AppCompatActivity() {
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialised the variables tied to the views here
        // since they are not in used anywhere
        val btSend = findViewById<Button>(R.id.bt_send)
        val etPhoneNo = findViewById<EditText>(R.id.et_phoneNo)
        val etMessage = findViewById<EditText>(R.id.et_message)

        if (arePermissionsGranted(this)) {
            findViewById<Button>(R.id.bt_start_service).let {
                Log.d("MainActivity", "Start the foreground service on demand")
                it.setOnClickListener {
                    actionService(SubscriberService.Actions.START)
                }
            }

            findViewById<Button>(R.id.bt_stop_service).let {
                Log.d("MainActivity", "Stop the foreground service on demand")
                it.setOnClickListener {
                    actionService(SubscriberService.Actions.STOP)
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
            requestPermissions(this)
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
}