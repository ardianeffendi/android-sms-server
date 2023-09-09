package au.com.robin.sms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.com.robin.sms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var smsInterfaceBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smsInterfaceBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = smsInterfaceBinding.root
        setContentView(view)
    }
}