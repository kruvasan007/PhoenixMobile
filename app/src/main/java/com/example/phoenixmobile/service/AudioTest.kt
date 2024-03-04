package com.example.phoenixmobile.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.example.phoenixmobile.data.Repository
import java.io.File
import java.lang.reflect.Method


class AudioTest : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String = ""
    private var isRecording = false
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayerSource: MediaPlayer? = null

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    private fun releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.release()
            mediaRecorder = null
        }
    }

    fun startMediaRecorder() {
        try {
            releaseRecorder()
            val outFile = File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "temp.wav"
            )
            if (outFile.exists()) {
                outFile.delete()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = MediaRecorder(applicationContext).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(fileName)
                    isRecording = true
                    prepare()
                    start()
                }
            } else {
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(fileName)
                    isRecording = true
                    start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayerRecords() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    private fun releaseMediaPlayer() {
        if (mediaPlayerSource != null) {
            mediaPlayerSource!!.release()
            mediaPlayerSource = null
        }
    }

    fun createMediaPlayerSource() {
        try {
            val uri = Uri.parse("android.resource://$packageName/raw/sound")
            mediaPlayerSource = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                isLooping = true
                prepare()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun createMediaPlayerRecording() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fileName)
                prepare()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createMediaPlayerSource()
        Repository.getReportState().observeForever {
            if (it == Repository.REPORT_STARTED && !isRecording) {
                startChecking()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startChecking() {
        Repository.setAudioResponse(Repository.AUDIO_CHECK_STARTED)
        var checkBluetoothConnected = false
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            for (device in mBluetoothAdapter.bondedDevices) {
                if (isConnected((device))) {
                    checkBluetoothConnected = true
                    Log.d("AudioTest", "${device.name} - connected")
                    break
                }
            }
        }
        if (checkBluetoothConnected)
            Repository.setBluetoothConnected()
        else
            Repository.setBluetoothDisconnected()

        Repository.getBluetoothFlag().observeForever {
            if (it) {
                Repository.setAudioResponse(Repository.AUDIO_CHECK_START_PLAYING)
                runTest()
            }
        }
    }

    private fun runTest() {
        val dir =
            File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "temp.wav"
            )
        fileName = dir.absolutePath
        val audioManager =
            application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(
            AudioManager.ADJUST_UNMUTE,
            AudioManager.FLAG_PLAY_SOUND
        )
        object : CountDownTimer(4000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished > 3500L) {
                    mediaPlayerSource?.start()
                    startMediaRecorder()
                }
            }

            override fun onFinish() {
                mediaPlayerSource?.pause()
                Repository.setAudioResponse(Repository.AUDIO_DONE_PLAY)
                startTimerToPlayRecord()
            }
        }.start()

    }

    private fun startTimerToPlayRecord() {
        object : CountDownTimer(3000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished > 2500L) {
                    createMediaPlayerRecording()
                    mediaPlayer?.start()
                }
            }

            override fun onFinish() {
                isRecording = false
                mediaPlayer?.stop()
                Repository.setAudioResponse(Repository.AUDIO_WAIT_ANSWER)
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    override fun onDestroy() {
        releaseMediaPlayer()
        releaseRecorder()
        releaseMediaPlayerRecords()
        super.onDestroy()
    }
}