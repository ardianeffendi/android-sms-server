package au.com.robin.sms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import au.com.robin.sms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var smsInterfaceBinding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsInterfaceBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = smsInterfaceBinding.root
        setContentView(view)

        smsInterfaceBinding.btSend.setOnClickListener {
            val phoneNo = smsInterfaceBinding.etPhoneNo.text?.toString()
            val message = smsInterfaceBinding.etMessage.text?.toString()

            // create an intent to let client choose which sim card slot
            // for now, we use sim card 2. Normally, user will be shown options to choose from.
            val intent = Intent()
            intent.putExtra("slot", 1)

            // Check for permission
            if (checkPermission()) {
                val smsManager = SmsSend.getSmsManager(this, intent)
                smsManager?.sendTextMessage(phoneNo, null, message, null, null)
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val sendSmsPermission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        val readPhoneStatePermission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)

        return sendSmsPermission == PackageManager.PERMISSION_GRANTED && readPhoneStatePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
            PERMISSION_REQUEST_CODE
        )
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