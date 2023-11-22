package au.com.robin.sms

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import au.com.robin.sms.develop.R
import au.com.robin.sms.service.ServiceState
import au.com.robin.sms.service.SubscriberService
import au.com.robin.sms.service.getServiceState

// Constants
private const val SIM_SLOT = "slot"
private const val SIM_SLOT_ONE = 0
private const val SIM_SLOT_TWO = 1

class MainActivity : AppCompatActivity() {
    @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialised the variables tied to the views here
        // since they are not in used anywhere
        val btSend = findViewById<Button>(R.id.bt_send)
        val etPhoneNo = findViewById<EditText>(R.id.et_phoneNo)
        val etMessage = findViewById<EditText>(R.id.et_message)

        btSend.setOnClickListener {
            val phoneNo = etPhoneNo.text?.toString()
            val message = etMessage.text?.toString()

            // Check for permission
            if (isPermissionGranted(this)) {
                val intent = Intent()
                // Default to SIM slot 1 for now
                // This is supposed to be passed in with a value that user chooses.
                intent.putExtra(SIM_SLOT, SIM_SLOT_ONE)
                val smsManager = getSmsManager(this, intent)
                smsManager?.sendTextMessage(phoneNo, null, message, null, null)
            } else {
                requestPermission(this)
            }
        }

        findViewById<Button>(R.id.bt_start_service).let {
            it.setOnClickListener {
                actionService(SubscriberService.Actions.START)
            }
        }

        findViewById<Button>(R.id.bt_stop_service).let {
            it.setOnClickListener {
                actionService(SubscriberService.Actions.STOP)
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