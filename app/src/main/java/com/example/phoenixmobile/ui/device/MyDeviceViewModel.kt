package com.example.phoenixmobile.ui.device

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import com.example.phoenixmobile.model.Battery
import com.example.phoenixmobile.model.Display
import com.example.phoenixmobile.model.Memory
import com.example.phoenixmobile.model.Network
import com.example.phoenixmobile.model.Report
import com.example.phoenixmobile.model.Sensors
import com.example.phoenixmobile.model.System
import com.example.phoenixmobile.service.CPUTest
import com.example.phoenixmobile.service.HardWareCheck
import com.example.phoenixmobile.service.NetworkTest


class MyDeviceViewModel : ViewModel() {

    private fun startService(context: Context) {
        context.startService(Intent(context, NetworkTest::class.java))
        context.startService(Intent(context, HardWareCheck::class.java))
        context.startService(Intent(context, CPUTest::class.java))
    }

    private fun stopService(context: Context) {
        context.stopService(Intent(context, NetworkTest::class.java))
        context.stopService(Intent(context, HardWareCheck::class.java))
        context.stopService(Intent(context, CPUTest::class.java))
    }

    private val _report = Report()

    fun startCheck(context: Context) {
        startService(context)
    }

    fun pushReport(): Boolean {
        //Repository.pullReport(_report)
        return true
    }

    fun setNetworkParams(level: Int, dataState: Int, simState: Int): Boolean {
        _report.networkState = Network(level, dataState, simState)
        return true
    }

    fun setBatteryParams(battery: Int): Boolean {
        _report.battery = Battery(battery)
        return true
    }

    fun setSystemParams(
        versionOS: String,
        sdkVersion: Int,
        phoneModel: String,
        hardware: String,
        board: String
    ): Boolean {
        _report.system = System(versionOS, sdkVersion, phoneModel, hardware, board)
        return true
    }

    fun setDisplayParams(height: Int, width: Int, dpi: Int): Boolean {
        _report.display = Display(height, width, dpi)
        return true
    }

    fun setSensorsParams(sensors: List<Sensor>): Boolean {
        _report.sensors = Sensors(sensors)
        return true
    }

    fun setMemoryParams(ram: Long, totalSpace: Long, availSpace: Long): Boolean {
        _report.memoryState = Memory(ram, totalSpace, availSpace)
        return true
    }
}