
package com.accident.detection

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat

object SmsHelper {
    fun sendSms(phone: String, body: String) {
        SmsManager.getDefault().sendTextMessage(phone, null, body, null, null)
    }

    fun ensurePermission(activity: Activity): Boolean {
        val ok = ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        if (!ok) ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS), 42)
        return ok
    }
}
