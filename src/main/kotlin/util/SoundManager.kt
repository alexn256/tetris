package util

import javax.sound.sampled.*
import kotlin.concurrent.thread
import kotlin.math.sin

/**
 * Manages sound effects for the game.
 */
object SoundManager {

    private var enabled = true

    /**
     * Enable or disable all sounds.
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * Plays sound when lines are cleared.
     */
    fun playLineClearSound() {
        if (!enabled) return
        thread {
            try {
                playTone(523.0, 100) // C5
                Thread.sleep(50)
                playTone(659.0, 100) // E5
                Thread.sleep(50)
                playTone(784.0, 150) // G5
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Plays sound when game is over.
     */
    fun playGameOverSound() {
        if (!enabled) return
        thread {
            try {
                playTone(392.0, 200) // G4
                Thread.sleep(100)
                playTone(349.0, 200) // F4
                Thread.sleep(100)
                playTone(294.0, 200) // D4
                Thread.sleep(100)
                playTone(262.0, 400) // C4 (longer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Generates and play a tone at the specified frequency and duration.
     */
    private fun playTone(frequency: Double, durationMs: Int) {
        val sampleRate = 44100.0
        val numSamples = (durationMs * sampleRate / 1000.0).toInt()
        val buffer = ByteArray(numSamples * 2)
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequency / sampleRate
            val sample = (sin(angle) * 127.0).toInt().toByte()
            val envelope = when {
                i < numSamples / 10 -> i.toDouble() / (numSamples / 10)
                i > numSamples * 9 / 10 -> (numSamples - i).toDouble() / (numSamples / 10)
                else -> 1.0
            }
            val envelopedSample = (sample * envelope).toInt().toByte()
            buffer[i * 2] = envelopedSample
            buffer[i * 2 + 1] = envelopedSample
        }
        val audioFormat = AudioFormat(
            sampleRate.toFloat(),
            8,  // 8-bit
            2,  // stereo
            true,  // signed
            false  // little-endian
        )
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        val line = AudioSystem.getLine(info) as SourceDataLine
        line.open(audioFormat)
        line.start()
        line.write(buffer, 0, buffer.size)
        line.drain()
        line.close()
    }
}
