package com.example.phoenixmobile.service

import android.app.Service
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
import com.example.phoenixmobile.data.AudioStatus
import com.example.phoenixmobile.data.ReportManager
import com.example.phoenixmobile.data.ReportStatus
import com.example.phoenixmobile.data.TestManager
import com.example.phoenixmobile.utils.CheckBluetoothConnected
import java.io.File


class AudioTest : Service() {
    // to record audio from the speaker
    private var mediaRecorder: MediaRecorder? = null

    // the name of the temporary file where the sound from the speaker will be recorded
    private var fileName: String = ""

    // recording status
    private var isRecording = false

    // the player of the recording
    private var mediaPlayer: MediaPlayer? = null

    // the player of the source recording
    private var mediaPlayerSource: MediaPlayer? = null

    private fun releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.release()
            mediaRecorder = null
        }
    }

    fun startMediaRecorder() {
        try {
            releaseRecorder()
            //create out file "temp.wav" in internal storage
            val outFile = File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "temp.wav"
            )
            if (outFile.exists()) {
                outFile.delete()
            }
            // create recorder
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
                    // where to write sound from
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    // recording formats
                    setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    // which file should I write to
                    setOutputFile(fileName)
                    isRecording = true
                    start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetPlayer() {
        if (mediaPlayerSource != null) {
            mediaPlayerSource!!.release()
            mediaPlayerSource = null
        }
    }

    fun createMediaPlayerSource() {
        try {
            // the place of recording in the internal memory of the device
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

        ReportManager.reportStatus.observeForever {
            // check the current recording status - is it at the recording stage
            if (it == ReportStatus.STARTED && !isRecording) {
                startChecking()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startChecking() {
        // checking connected Bluetooth devices
        if (!CheckBluetoothConnected.checkBluetoothConnected(this)) {
            // if there is a connected device, we inform the controller
            runTest()
        }
    }

    private fun runTest() {
        // we get the record
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

        // timer for sample playback and recording
        object : CountDownTimer(4000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // start playback with some delay
                if (millisUntilFinished > 3500L) {
                    mediaPlayerSource?.start()
                    TestManager.setAudioReport(AudioStatus.STARTED)
                    startMediaRecorder()
                }
            }

            override fun onFinish() {
                // stop recording at the end of the timer
                mediaPlayerSource?.pause()
                TestManager.setAudioReport(AudioStatus.DONE_PLAY)
                startTimerToPlayRecord()
            }
        }.start()

    }

    private fun startTimerToPlayRecord() {
        // timer for playing a recorded sample
        object : CountDownTimer(3000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished > 2500L) {
                    createMediaPlayerRecording()
                    mediaPlayer?.start()
                }
            }

            // stopping the playback of recorded audio from the speaker
            override fun onFinish() {
                isRecording = false
                TestManager.setAudioReport(AudioStatus.WAITING)
                mediaPlayer?.stop()
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    override fun onDestroy() {
        releaseRecorder()
        resetPlayer()
        super.onDestroy()
    }
}