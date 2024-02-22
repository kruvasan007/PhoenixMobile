package com.example.phoenixmobile.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class AudioTest : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String = ""
    private var mediaPlayer: MediaPlayer? = null

    private fun releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.release()
            mediaRecorder = null
        }
    }

    fun start() {
        try {
            releaseRecorder()
            val outFile = File(fileName)
            if (outFile.exists()) {
                outFile.delete()
            }
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(fileName)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        if (mediaRecorder != null) {
            mediaRecorder!!.stop()
        }
    }

    private fun releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    fun playStart() {
        try {
            releasePlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fileName)
                prepare()
                start()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun playStop() {
        mediaPlayer?.stop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dir =
            File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), ".temp.wav")
        fileName = dir.absolutePath
        GlobalScope.launch(Dispatchers.Main) {
            val mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
            mediaPlayer.start()
            start()
            delay(1000)
            stop()
            mediaPlayer.stop()
            mediaPlayer.release()

            playStart()
            delay(2000)
            playStop()

            Repository.setAudioResponse(Repository.AUDIO_WAIT_ANSWER)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }
}