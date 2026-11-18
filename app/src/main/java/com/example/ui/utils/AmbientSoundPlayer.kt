package com.example.ui.utils
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.Random

object AmbientSoundPlayer {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private val random = Random()
    private var playThread: Thread? = null

    var currentSound = "None"
        private set
    
    fun setSound(sound: String) {
        val oldSound = currentSound
        currentSound = sound
        if (isPlaying && sound != oldSound) {
            stop()
            if (sound != "None") {
                play()
            }
        }
    }
    
    fun play() {
        if (isPlaying || currentSound == "None") return
        isPlaying = true
        playThread = Thread {
            val sampleRate = 44100
            val minSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minSize,
                AudioTrack.MODE_STREAM
            )
            
            try {
                audioTrack?.play()
            } catch (e: Exception) {
                isPlaying = false
                return@Thread
            }
            
            val buffer = ShortArray(minSize)
            var lastOut = 0.0
            
            try {
                while (isPlaying) {
                    for (i in buffer.indices) {
                        val white = random.nextDouble() * 2 - 1
                        
                        val out = when (currentSound) {
                            "Brown Noise" -> {
                                lastOut = (lastOut + (0.02 * white)) / 1.02
                                lastOut * 3.5 // gain
                            }
                            "Pink Noise" -> {
                                lastOut = 0.99886 * lastOut + white * 0.0555179
                                lastOut
                            }
                            else -> { // White Noise
                                white * 0.2
                            }
                        }
                        
                        buffer[i] = (out.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        playThread?.start()
    }
    
    fun stop() {
        isPlaying = false
        playThread?.interrupt()
        playThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }
}
