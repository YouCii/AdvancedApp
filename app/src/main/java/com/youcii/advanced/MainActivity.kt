package com.youcii.advanced

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.NoiseSuppressor
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class MainActivity : AppCompatActivity() {

    private var mRecorder: AudioRecord? = null
    private var mRecorderBufferSize = 0
    private var audioSessionId = -1

    private var hasAudioPermission: Boolean = false

    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null
    private var le: LoudnessEnhancer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasAudioPermission = checkAudioPermission()
    }

    override fun onStart() {
        super.onStart()
        if (!hasAudioPermission) {
            Toast.makeText(this, "没有音频权限", Toast.LENGTH_SHORT).show()
            return
        }
        initRecorder()
        initAudioEffect()
        startRecord()
    }

    override fun onStop() {
        super.onStop()
        mRecorder?.stop()
        mRecorder?.release()

        aec?.enabled = false
        aec?.release()
        ns?.enabled = false
        ns?.release()
        agc?.enabled = false
        agc?.release()
        le?.enabled = false
        le?.release()
    }

    /**
     * 开始录制
     */
    private fun startRecord() {
        mRecorder?.startRecording()

        val pcmArray = ShortArray(mRecorderBufferSize)
        val count: Int = mRecorder?.read(pcmArray, 0, pcmArray.size) ?: 0

        val targetFilePath = getExternalFilesDir(Environment.DIRECTORY_ALARMS)?.absolutePath + "record"
        val targetFile = File(targetFilePath)
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val fos = try {
            FileOutputStream(targetFile)
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "FileNotFoundException", Toast.LENGTH_SHORT).show()
            return
        }
        val bos = BufferedOutputStream(fos)
        val dos = DataOutputStream(bos)
        for (i in 0 until count) {
            dos.writeShort(pcmArray[i].toInt())
        }
    }

    /**
     * 检查权限
     */
    private fun checkAudioPermission(): Boolean {
        val checkResult = packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName)
        return checkResult == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 初始化录音
     */
    private fun initRecorder() {
        mRecorderBufferSize = AudioRecord.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        mRecorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            mRecorderBufferSize
        )
        audioSessionId = mRecorder!!.audioSessionId
    }

    /**
     * 初始化音频特效
     */
    private fun initAudioEffect() {
        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(audioSessionId)
            aec?.enabled = true
        }
        if (NoiseSuppressor.isAvailable()) {
            ns = NoiseSuppressor.create(audioSessionId)
            ns?.enabled = true
        }
        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(audioSessionId)
            agc?.enabled = true
        }
        try {
            le = LoudnessEnhancer(audioSessionId)
            le?.enabled = true
            le?.setTargetGain(3)
        } catch (e: IllegalStateException) {
            Log.w("Audio", "not implemented on this device " + e.message)
        } catch (e: IllegalArgumentException) {
            Log.w("Audio", "not implemented on this device " + e.message)
        } catch (e: UnsupportedOperationException) {
            Log.w("Audio", "not enough resources")
        } catch (e: RuntimeException) {
            Log.w("Audio", "not enough memory")
        }
    }

}