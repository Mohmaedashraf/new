
package com.accident.detection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    private var running by mutableStateOf(false)
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var phone by remember { mutableStateOf(loadPhone()) }
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone number") })
                Spacer(Modifier.height(8.dp))
                Button(onClick = { savePhone(phone) }) { Text("Save") }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { ensurePerms { toggleService() } }) {
                    Text(if (running) "Stop Service" else "Start Service")
                }
            }
        }
    }

    private fun toggleService() {
        val intent = Intent(this, BluetoothService::class.java)
        if (running) stopService(intent) else startForegroundService(intent)
        running = !running
    }

    private fun ensurePerms(onGranted: () -> Unit) {
        val needed = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS
        ).filter { ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

        if (needed.isEmpty()) onGranted() else permLauncher.launch(needed.toTypedArray())
    }

    private fun loadPhone(): String =
        getSharedPreferences("prefs", MODE_PRIVATE).getString("phone", "") ?: ""

    private fun savePhone(p: String) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit { putString("phone", p) }
    }
}
