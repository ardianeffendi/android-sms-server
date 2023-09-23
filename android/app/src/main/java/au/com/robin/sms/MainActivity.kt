package au.com.robin.sms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    /**
     * Constant variables
     */
    companion object {
        const val PERMISSION_REQUEST_CODE = 1
        const val SIM_SLOT = "slot"
        const val SIM_SLOT_ONE = 0 // SIM slot number 1 in a multi-sim phone.
    }

    /**
     * Declare variables with lateinit that are `not-null` outside constructor.
     */
    private lateinit var btSend: Button
    private lateinit var etPhoneNo: EditText
    private lateinit var etMessage: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btSend = findViewById(R.id.bt_send)
        etPhoneNo = findViewById(R.id.et_phoneNo)
        etMessage = findViewById(R.id.et_message)

        btSend.setOnClickListener {
            val phoneNo = etPhoneNo.text?.toString()
            val message = etMessage.text?.toString()

            // Check for permission
            if (checkPermission()) {
                val intent = Intent()
                intent.putExtra(SIM_SLOT, SIM_SLOT_ONE)
                val smsManager = getSmsManager(this, intent)
                smsManager?.sendTextMessage(phoneNo, null, message, null, null)
            } else {
                requestPermission()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val sendSmsPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        val readPhoneStatePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)

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