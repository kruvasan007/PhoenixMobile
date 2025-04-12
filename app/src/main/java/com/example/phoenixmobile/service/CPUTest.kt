package com.example.phoenixmobile.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Base64
import android.util.Log
import com.example.phoenixmobile.data.ReportManager
import com.example.phoenixmobile.data.ReportStatus
import com.example.phoenixmobile.data.TestManager
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class CPUTest : Service() {
    private lateinit var hashValue: String

    /* start a block function with a cryptographic test for processor load */
    private fun computeSHAHash(password: String) {
        var mdSha1: MessageDigest? = null
        try {
            mdSha1 = MessageDigest.getInstance("SHA-1")
        } catch (e1: NoSuchAlgorithmException) {
            Log.e("Benchmark", "Error initializing SHA1")
        }
        try {
            mdSha1!!.update(password.toByteArray(charset("ASCII")))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        val data = mdSha1!!.digest()
        val sb = StringBuffer()
        var hex: String?
        hex = Base64.encodeToString(data, 0, data.size, 0)
        sb.append(hex)
        hashValue = sb.toString()
    }

    private fun checkProcessor() {
        val cmd: ProcessBuilder
        var frequency = ""

        val tsLong = System.nanoTime();
        for (i in 0..19999) {
            computeSHAHash("The big bad wolf")
        }
        val ttLong: Long = System.nanoTime() - tsLong
        val tt = ttLong.toString()
        val roundnumber = Math.round((ttLong / 100000000).toFloat())
        val score = roundnumber.toString()
        val output = """${
            """SHA-1 hash:  $hashValue Time Taken: $tt"""
        } Score: $score"""
        Log.d("HASH", output)
        try {
            val args =
                arrayOf(
                    "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
                )
            cmd = ProcessBuilder(*args)
            val process = cmd.start()
            val `in` = process.inputStream
            val re = ByteArray(16)
            `in`.read(re)
            frequency += String(re).split("?")[0]
            frequency = frequency.split("\n")[0]
            `in`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        Log.d("CPU", frequency)
        TestManager.setCpuReport(frequency, score)
    }
    /* end a block function with a cryptographic test for processor load */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ReportManager.reportStatus.observeForever {
            if (it == ReportStatus.STARTED) {
                // This test loads the processing power of the processor and puts a
                // relative score where a score of 6-8 is very good and the higher
                // the value, the worse (for example, 26 is an old phone)
                checkProcessor()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}