package com.symeonchen.wakeupscreen

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toggleNotificationService()
    }

    private fun toggleNotificationService() {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                this, NotificationMonitor::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(
                this, NotificationMonitor::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

}
