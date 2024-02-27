package com.example.phoenixmobile.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.phoenixmobile.data.Repository

class NetworkTest : Service() {
    private var level: Int = -1
    private var simState: Int = -1
    private var dataState: Int = -1
    private var connected = false
    private fun checkConnected() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val permissionCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            Log.d("NETWORK","Error: SDK version error")
        }
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (mBluetoothAdapter.isEnabled) {
                connected = true
            }
            Log.d("BLUETOOTH", mBluetoothAdapter.bondedDevices.toString())
            /*try {
                try {
                    val m = device.javaClass.getMethod(
                        "createRfcommSocket", *arrayOf<Class<*>?>(
                            Int::class.javaPrimitiveType
                        )
                    )
                    try {
                        val bs = m.invoke(device, Integer.valueOf(1)) as BluetoothSocket
                        bs.connect()
                        connected = true
                        Log.d("BLUETOOTH", device.name + " - connected")
                        break
                    } catch (e: IOException) {
                        Log.e("BLUETOOTH", "IOException: " + e.localizedMessage)
                        Log.d("BLUETOOTH", device.name + " - not connected")
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("BLUETOOTH", "IllegalArgumentException: " + e.localizedMessage)
                } catch (e: IllegalAccessException) {
                    Log.e("BLUETOOTH", "IllegalAccessException: " + e.localizedMessage)
                } catch (e: InvocationTargetException) {
                    Log.e("BLUETOOTH", "InvocationTargetException: " + e.localizedMessage)
                }
            } catch (e: SecurityException) {
                Log.e("BLUETOOTH", "SecurityException: " + e.localizedMessage)
            } catch (e: NoSuchMethodException) {
                Log.e("BLUETOOTH", "NoSuchMethodException: " + e.localizedMessage)
            }
        }*/
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkNetworkState() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.READ_PHONE_STATE
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager =
                applicationContext.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            level = telephonyManager.signalStrength!!.level
            dataState = telephonyManager.dataState
            simState = telephonyManager.simState
            Log.d(
                "NETWORK",
                "${levelString(level)}  ${dataStateString(dataState)} ${simString(simState)}\n\n"
            )
        }

    }

    @SuppressLint("ServiceCast")
    fun checkGpsStatus() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val mLocationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // Checking GPS is enabled
            val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // Display the message into the string
            Log.d("GPS", mGPS.toString())
        }
    }

    private fun simString(simState: Int): String {
        return when (simState) {
            TelephonyManager.SIM_STATE_READY -> "SIM_STATE_READY"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "SIM_STATE_NETWORK_LOCKED"
            TelephonyManager.SIM_STATE_NOT_READY -> "SIM_STATE_NOT_READY"
            TelephonyManager.SIM_STATE_UNKNOWN -> "SIM_STATE_UNKNOWN"
            else -> "UNKNOWN"
        }
    }

    private fun dataStateString(dataState: Int): String {
        return when (dataState) {
            TelephonyManager.DATA_CONNECTED -> "DATA_CONNECTED"
            TelephonyManager.DATA_CONNECTING -> "DATA_CONNECTING"
            TelephonyManager.DATA_DISCONNECTED -> "DATA_DISCONNECTED"
            TelephonyManager.DATA_SUSPENDED -> "DATA_SUSPENDED"
            else -> "UNKNOWN"
        }
    }

    private fun levelString(level: Int): String {
        return when (level) {
            0 -> "NO_SIGNAL"
            1 -> "BAD_SIGNAL"
            2 -> "OK_SIGNAL"
            3 -> "GOOD_SIGNAL"
            4 -> "BEST_SIGNAL"
            else -> "UNKNOWN"
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //checkConnected()
        checkGpsStatus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkNetworkState()
        }
        checkConnected()

        Repository.setNetworkReport(level, dataState, simState, 0, connected)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}