package model

import java.awt.Color
import model.State.*

/**
 * Represents animation for line clearing.
 * Animation phases:
 * 1. BLINKING - blocks blink white/original color (3-4 times)
 * 2. DISAPPEARING - blocks fade out from center to edges
 * 3. LINE_FADING - entire line becomes white and fades
 * 4. COMPLETE - animation done, ready to remove line
 */
class LineAnimation(val lineY: Int, val width: Int) {

    /**
     * Animation phases
     */
    enum class Phase {
        BLINKING,
        DISAPPEARING,
        LINE_FADING,
        COMPLETE
    }

    /**
     * Current animation phase
     */
    var phase = Phase.BLINKING

    /**
     * Frame counter for current phase
     */
    private var frameCounter = 0

    /**
     * Configuration constants
     */
    companion object {
        const val BLINK_FRAMES = 24
        const val BLINK_INTERVAL = 6
        const val DISAPPEAR_FRAMES = 18
        const val FADE_FRAMES = 12
    }

    /**
     * Nodes being animated, indexed by X coordinate
     */
    private val nodes = mutableMapOf<Int, Node>()

    /**
     * Disappear order (from center to edges)
     */
    private val disappearOrder = mutableListOf<Int>()

    init {
        val center = width / 2
        val order = mutableListOf<Int>()
        if (width % 2 == 0) {
            order.add(center - 1)
            order.add(center)
            var offset = 1
            while (center - 1 - offset >= 0 || center + offset < width) {
                if (center - 1 - offset >= 0) order.add(center - 1 - offset)
                if (center + offset < width) order.add(center + offset)
                offset++
            }
        } else {
            order.add(center)
            var offset = 1
            while (center - offset >= 0 || center + offset < width) {
                if (center - offset >= 0) order.add(center - offset)
                if (center + offset < width) order.add(center + offset)
                offset++
            }
        }
        disappearOrder.addAll(order)
    }

    /**
     * Add node to animation
     */
    fun addNode(x: Int, node: Node) {
        nodes[x] = node
    }

    /**
     * Update animation state (called each frame)
     * Returns true if animation is still running, false if complete
     */
    fun update(): Boolean {
        frameCounter++
        when (phase) {
            Phase.BLINKING -> {
                updateBlinking()
                if (frameCounter >= BLINK_FRAMES) {
                    // Move to disappearing phase
                    phase = Phase.DISAPPEARING
                    frameCounter = 0
                    // Reset all nodes to normal state
                    nodes.values.forEach {
                        it.state = NOT_ACTIVE
                        it.blinkColor = null
                    }
                }
            }
            Phase.DISAPPEARING -> {
                updateDisappearing()
                if (frameCounter >= DISAPPEAR_FRAMES) {
                    // Move to line fading phase
                    phase = Phase.LINE_FADING
                    frameCounter = 0
                    // Set all nodes to white and start fading
                    nodes.values.forEach {
                        it.blinkColor = Color.WHITE
                        it.alpha = 1.0f
                    }
                }
            }
            Phase.LINE_FADING -> {
                updateLineFading()
                if (frameCounter >= FADE_FRAMES) {
                    // Animation complete
                    phase = Phase.COMPLETE
                    return false
                }
            }
            Phase.COMPLETE -> {
                return false
            }
        }
        return true
    }

    /**
     * Update blinking phase - alternate between white and original color
     */
    private fun updateBlinking() {
        val blinkCycle = frameCounter % BLINK_INTERVAL
        val showWhite = blinkCycle < BLINK_INTERVAL / 2
        nodes.values.forEach { node ->
            node.state = BLINKING
            node.blinkColor = if (showWhite) Color.WHITE else null
        }
    }

    /**
     * Update disappearing phase - fade out from center to edges
     */
    private fun updateDisappearing() {
        val blocksPerFrame = disappearOrder.size.toFloat() / DISAPPEAR_FRAMES
        val numBlocksDisappearing = ((frameCounter + 1) * blocksPerFrame).toInt()
            .coerceAtMost(disappearOrder.size)
        for (i in 0 until numBlocksDisappearing) {
            val x = disappearOrder[i]
            val node = nodes[x] ?: continue
            val blockStartFrame = (i / blocksPerFrame).toInt()
            val blockFrameAge = frameCounter - blockStartFrame
            val fadeFrames = DISAPPEAR_FRAMES - blockStartFrame
            val alpha = if (fadeFrames > 0) {
                (1.0f - (blockFrameAge.toFloat() / fadeFrames)).coerceIn(0.0f, 1.0f)
            } else {
                0.0f
            }
            node.state = DISAPPEARING
            node.alpha = alpha
        }
    }

    /**
     * Update line fading phase - entire line fades to white then disappears
     */
    private fun updateLineFading() {
        val progress = frameCounter.toFloat() / FADE_FRAMES
        val alpha = (1.0f - progress).coerceIn(0.0f, 1.0f)
        nodes.values.forEach { node ->
            node.alpha = alpha
            node.blinkColor = Color.WHITE
        }
    }

    /**
     * Check if animation is complete
     */
    fun isComplete(): Boolean = phase == Phase.COMPLETE

    /**
     * Get current phase for debugging
     */
    fun getCurrentPhase(): String = "${phase.name} (frame $frameCounter)"
}
