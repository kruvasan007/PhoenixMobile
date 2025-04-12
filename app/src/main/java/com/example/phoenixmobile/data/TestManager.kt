package com.example.phoenixmobile.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.model.BodyCondition
import com.example.phoenixmobile.model.ScreenCondition
import java.util.*

object TestManager {

    private val _testList = MutableLiveData<TreeMap<String, Int>>()
    private val _audioStatus = MutableLiveData<AudioStatus>()
    val testList: LiveData<TreeMap<String, Int>> get() = _testList
    val audioStatus: LiveData<AudioStatus> get() = _audioStatus

    init {
        resetTests()
    }

    fun resetTests() {
        _testList.value = TreeMap(
            listOf(
                "CPU", "Battery", "Network", "Gyroscope", "Bluetooth",
                "Memory", "Display", "Audio System", "GPS"
            ).associateWith { ReportStatus.NULL.ordinal }
        )
        ReportManager.setReportClear()
    }

    fun setCpuReport(freq: String, mark: String) {
        updateTest("CPU", ReportStatus.DONE)
        ReportManager.setCpuReport(freq, mark)
    }

    fun setBatteryReport(status: Int) {
        updateTest("Battery", ReportStatus.DONE)
        ReportManager.setBatteryReport(status)
    }

    fun setGyroscopeReport(enabled: Boolean) {
        updateTest("Gyroscope", if (enabled) ReportStatus.DONE else ReportStatus.ERROR)
        ReportManager.setGyroscopeReport(enabled)
    }

    fun setMemoryReport(ram: Long, total: Long, available: Long) {
        val valid = listOf(ram, total, available).all { it > 0 }
        updateTest("Memory", if (valid) ReportStatus.DONE else ReportStatus.ERROR)
        ReportManager.setMemoryReport(ram, total, available)
    }


    fun setDisplayReport(screen: ScreenCondition, body: BodyCondition, w: Int, h: Int, d: Float) {
        val valid = w > 0 && h > 0
        updateTest("Display", if (valid) ReportStatus.DONE else ReportStatus.ERROR)
        ReportManager.setDisplayReport(screen, body, w, h, d)
    }

    fun setNetworkReport(level: Int, data: Int, sim: Int, gps: Boolean) {
        val valid = level != -1 && data != -1 && sim != -1
        updateTest("Network", if (valid) ReportStatus.DONE else ReportStatus.ERROR)
        updateTest("GPS", if (gps) ReportStatus.DONE else ReportStatus.ERROR)
        ReportManager.setNetworkReport(level, data, sim, gps)
    }

    fun setBlueToothReport(bt: Boolean) {
        updateTest("Bluetooth", if (bt) ReportStatus.DONE else ReportStatus.ERROR)
        ReportManager.setBlueToothReport(bt)
    }

    fun setAudioReport(status: AudioStatus) {
        if (status == AudioStatus.DONE || status == AudioStatus.ERROR)
            updateTest(
                "Audio System",
                if (status == AudioStatus.DONE) ReportStatus.DONE else ReportStatus.ERROR
            )
        _audioStatus.postValue(status)
        ReportManager.setAudioReport(status == AudioStatus.DONE)
    }

    private fun isAllTestDone(): Boolean {
        val tests = _testList.value ?: return false
        return !tests.containsValue(ReportStatus.NULL.ordinal)
    }

    private fun updateTest(key: String, status: ReportStatus) {
        _testList.value?.set(key, status.ordinal)
        _testList.postValue(_testList.value)
        if (isAllTestDone()) {
            ReportManager.setReportDone()
        }
    }
}

enum class AudioStatus(val code: Int) {
    NULL(0),
    STARTED(1),
    DONE(2),
    ERROR(3),
    WAITING(4),
    DONE_PLAY(5),
}