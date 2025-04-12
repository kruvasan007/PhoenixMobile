package com.example.phoenixmobile.util

import android.content.Context
import android.os.Build
import com.example.phoenixmobile.R
import com.jaredrummler.android.device.DeviceName
import java.io.BufferedReader
import java.io.InputStreamReader

object SettingsManager {

    private var settingsMap: Map<String, String>? = null

    /**
     * Загружает файл один раз (ленивая инициализация)
     */

    /**
     * Загружает файл из res/raw один раз
     */
    fun initialize(context: Context, rawResId: Int = R.raw.device_names) {
        if (settingsMap != null) return // уже загружено

        try {
            val map = mutableMapOf<String, String>()

            val inputStream = context.resources.openRawResource(rawResId)
            BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        map[key] = value
                    }
                }
            }

            settingsMap = map

        } catch (e: Exception) {
            e.printStackTrace()
            settingsMap = emptyMap()
        }
    }
    /**
     * Получение значения по ключу
     */
    private fun getValue(key: String): String? {
        return settingsMap?.get(key)
    }

    fun getDeviceName(): String {
        val deviceNameRaw = DeviceName.getDeviceName(Build.MODEL, Build.MODEL)
        val deviceName = getValue(deviceNameRaw)
        return deviceName ?: deviceNameRaw
    }
}