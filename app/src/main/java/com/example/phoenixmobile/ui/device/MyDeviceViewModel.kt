package com.example.phoenixmobile.ui.device

import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import com.example.phoenixmobile.data.Repository
import com.example.phoenixmobile.model.Battery
import com.example.phoenixmobile.model.Display
import com.example.phoenixmobile.model.Memory
import com.example.phoenixmobile.model.Network
import com.example.phoenixmobile.model.Report
import com.example.phoenixmobile.model.Sensors
import com.example.phoenixmobile.model.System

class MyDeviceViewModel : ViewModel() {

    private val _report = Report()
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