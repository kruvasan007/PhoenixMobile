package com.example.phoenixmobile.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.data.api_models.ReportAnswer
import com.example.phoenixmobile.data.api_models.ReportRequest
import com.example.phoenixmobile.data.models.*
import com.example.phoenixmobile.model.BodyCondition
import com.example.phoenixmobile.model.ScreenCondition

internal object ReportManager {
    private val _cpuReport = MutableLiveData<CPUReport>()
    private val _hardwareReport = MutableLiveData(HardWareReport())
    private val _networkReport = MutableLiveData(NetworkReport())
    private val _displayReport = MutableLiveData(DisplayReport())
    private val _audioReport = MutableLiveData(AudioReport())


    private val _reportText = MutableLiveData<Map<String, String>>()
    private val _reportStatus = MutableLiveData(ReportStatus.NULL)

    val reportLiveText: LiveData<Map<String, String>> get() = _reportText
    val reportStatus: LiveData<ReportStatus> get() = _reportStatus

    fun setReportInProgress() {
        _reportStatus.value = ReportStatus.STARTED
    }

    fun setReportError() {
        _reportStatus.value = ReportStatus.ERROR
    }

    fun setReportDone() {
        _reportStatus.value = ReportStatus.DONE
    }

    fun setCpuReport(freq: String, mark: String) {
        _cpuReport.value = CPUReport(freq, mark)
    }

    fun setBatteryReport(status: Int) {
        val hw = _hardwareReport.value ?: HardWareReport()
        hw.batteryState = status
        _hardwareReport.value = hw
    }

    fun setGyroscopeReport(enabled: Boolean) {
        val hw = _hardwareReport.value ?: HardWareReport()
        hw.gyroscope = enabled.toString()
        _hardwareReport.value = hw
    }

    fun setMemoryReport(ram: Long, total: Long, available: Long) {
        val hw = _hardwareReport.value ?: HardWareReport()
        hw.ram = ram
        hw.totalSpace = total
        hw.availabSpace = available
        _hardwareReport.value = hw
    }

    fun setDisplayReport(screen: ScreenCondition, body: BodyCondition, w: Int, h: Int, d: Float) {
        _displayReport.value = DisplayReport(screen.description, body.description, w, h, d)
    }

    fun setNetworkReport(level: Int, data: Int, sim: Int, gps: Boolean) {
        _networkReport.value = NetworkReport(level, data, sim, gps)
    }

    fun setBlueToothReport(bt: Boolean){
        val hw = _hardwareReport.value ?: HardWareReport()
        hw.bluetooth = bt
        _hardwareReport.value = hw
    }

    fun setAudioReport(status: Boolean) {
        _audioReport.value = AudioReport(testStatus = status)
    }

    fun getRequest(): ReportRequest {
        val cpu = _cpuReport.value
        val net = _networkReport.value
        val hw = _hardwareReport.value
        val dsp = _displayReport.value
        val audio = _audioReport.value

        return ReportRequest(
            deviceId = DeviceInfoManager.deviceId.takeIf { it.isNotBlank() },
            screen = dsp?.screen,
            body = dsp?.body,
            ram = hw?.ram,
            totalSpace = hw?.totalSpace,
            // not required
            width = dsp?.width,
            height = dsp?.height,
            density = dsp?.density,
            gyroscope = hw?.gyroscope,
            versionOS = hw?.versionOS,
            batteryState = hw?.batteryState,
            level = net?.level,
            dataStatus = net?.dataStatus,
            gps = net?.GPS,
            bluetooth = net?.bluetooth,
            audioReport = audio?.testStatus,
            frequency = cpu?.frequency,
            mark = cpu?.mark,
        )
    }

    fun setReportResult(answer: ReportAnswer?) {
        _reportText.value = if (answer != null) {
            mapOf(
                "mark" to answer.mark,
                "model" to answer.model,
                "condition" to answer.condition,
                "price" to answer.price.toString(),
                "report_id" to answer.report_id.toString()
            )
        } else {
            mapOf("error" to "Empty response")
        }
    }

    fun setReportClear() {
        _reportStatus.postValue(ReportStatus.NULL)
    }
}