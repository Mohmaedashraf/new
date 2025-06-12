
package com.accident.detection

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BluetoothService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var socket: BluetoothSocket? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    override fun onCreate() {
        super.onCreate()
        startForeground(NotificationHelper.NOTIFICATION_ID, NotificationHelper.baseNotification(this))
        scope.launch { connectAndListen() }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastLocation(): Location? = suspendCoroutine { cont ->
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    private suspend fun connectAndListen() {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
            val device = adapter.bondedDevices.firstOrNull { it.name == "HC-06" } ?: return
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid)
            adapter.cancelDiscovery()
            socket!!.connect()

            val reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            while (scope.isActive && socket!!.isConnected) {
                val line = reader.readLine() ?: break
                val loc = lastLocation()
                val locStr = loc?.let { "${it.latitude},${it.longitude}" } ?: "no_location"
                val msg = "$line : $locStr"
                NotificationHelper.pushMessage(this, msg)
                val phone = getSharedPreferences("prefs", MODE_PRIVATE).getString("phone", null)
                if (phone != null) SmsHelper.sendSms(phone, msg)
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error", e)
        } finally {
            stopSelf()
        }
    }

    override fun onDestroy() {
        socket?.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
