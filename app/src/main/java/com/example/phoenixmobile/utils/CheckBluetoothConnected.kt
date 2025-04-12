package com.example.phoenixmobile.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.data.ReportManager
import com.example.phoenixmobile.data.TestManager
import java.lang.reflect.Method

object CheckBluetoothConnected {
    private val _bluetoothConnectedStatus = MutableLiveData(false)

    val bluetoothConnectedStatus: LiveData<Boolean> get() = _bluetoothConnectedStatus

    private fun isConnected(device: BluetoothDevice): Boolean {
        // checking if there are headphones connected
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    fun checkBluetoothConnected(context: Context): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            if (mBluetoothAdapter.isEnabled) {
                TestManager.setBlueToothReport(true)
            }
            // checking all devices to see if they are connected
            for (device in mBluetoothAdapter.bondedDevices) {
                if (isConnected((device))) {
                    TestManager.setBlueToothReport(true)
                    _bluetoothConnectedStatus.postValue(true)
                    return true
                }
            }
        }
        TestManager.setBlueToothReport(false)
        return false
    }

    fun skipStatus() {
        _bluetoothConnectedStatus.postValue(false)
    }
}