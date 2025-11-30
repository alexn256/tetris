package model

import input.Input
import java.awt.Graphics
import java.util.*
import model.State.NOT_ACTIVE
import model.Level.LEVEL_1
import util.GameSerializer
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.event.KeyEvent


/**
 * Represents model of game field.
 */
class Model(val w: Int, val h: Int, private val input: Input) {

    /**
     * Map that represents game field.
     */
    var array: Array<Array<Node?>>

    /**
     * Shadow of Shape.
     */
    private val shadow: Array<Node?>

    /**
     * State of Shape.
     */
    private var state: Boolean

    /**
     * Amount of not active Nodes on field.
     */
    var nodes: Int

    /**
     * Show whether the game is on pause.
     */
    private var isPaused: Boolean

    /**
     * Show whether shadow is enabled.
     */
    private var isShadowEnabled: Boolean

    /**
     * Current Shape.
     */
    var shape: Shape

    /**
     * Type of next Shape.
     */
    lateinit var nextType: Type

    /**
     * Color of next Shape.
     */
    lateinit var nextColor: NodeColor

    /**
     * Level (velocity) of game.
     */
    var level: Level

    /**
     * Amount of completed lines.
     */
    var lines: Lines

    /**
     * The game is over.
     */
    var gameOver: Boolean

    /**
     * Queue of line animations currently running
     */
    private val activeAnimations = mutableListOf<LineAnimation>()

    /**
     * Lines pending to be removed after animation completes
     */
    private val linesToRemove = mutableListOf<Int>()

    init {
        gameOver = false
        nodes = 0
        isPaused = false
        isShadowEnabled = true
        state = true
        initNextShape()
        level = LEVEL_1
        lines = Lines
        shape = randomShape()
        array = Array(h) { Array<Node?>(w) { null } }
        shadow = Array(4) { null }
        for (i in shape.body.indices) {
            shadow[i] = shape.body[i].clone()
        }
    }

    /**
     * Update process of game field.
     */
    fun update() {
        if (gameOver) {
            return
        }
        updateAnimations()
        if (activeAnimations.isEmpty()) {
            input(input)
            if (nodes != 0) {
                checkCollisions()
                if (gameOver) {
                    return
                }
            }
            state = shape.active
            if (!state) {
                if (!gameOver) {
                    newShape()
                }
                if (activeAnimations.isEmpty()) {
                    checkLine()
                }
            }
        } else {
            // During animation, pause the shape
            if (!shape.isPaused) {
                shape.pause()
            }
        }
    }

    /**
     * Handles Shape action.
     */
    private fun input(input: Input) {
        moveLeft(input)
        moveRight(input)
        moveDown(input)
        rotate(input)
        pause(input)
        moveInstantDown(input)
        toggleShadow(input)
        updateShadow()
    }

    /**
     * Generate thr "Shadow" of Shape.
     * Shows the place where the Shape will be installed,
     * if Shape will be move same.
     */
    private fun updateShadow() {
        for (i in shape.body.indices) {
            shadow[i] = shape.body[i].clone()
        }
        var a = true
        while (a) {
            for (n in shadow) {
                if (n!!.y < h - 1) {
                    n.down()
                }
                if (nodes == 0) {
                    if (n.y == h - 1) {
                        a = false
                    }
                } else {
                    if (n.y == h - 1 || array[n.y + 1][n.x] != null) a = false
                }
            }
            if (!a) break
        }
    }

    /**
     * Handle keyboard click event for ' <-- ' key.
     */
    private fun moveLeft(input: Input) {
        if (input.getKey(KeyEvent.VK_LEFT) && !isPaused) {
            for (n in shape.body) {
                if ((n.x > 0 && array[n.y][n.x - 1] != null)) {
                    shape.left = false
                    break
                }
            }
            if (shape.left) {
                shape.left()
            }
            shape.left = true
            input.map[KeyEvent.VK_LEFT] = false
        }
    }

    /**
     * Handle keyboard click event for ' --> ' key.
     */
    private fun moveRight(input: Input) {
        if (input.getKey(KeyEvent.VK_RIGHT) && !isPaused) {
            for (n in shape.body) {
                if ((n.x < w - 1 && array[n.y][n.x + 1] != null)) {
                    shape.right = false
                    break
                }
            }
            if (shape.right) {
                shape.right()
            }
            shape.right = true
            input.map[KeyEvent.VK_RIGHT] = false
        }
    }

    /**
     * Handle keyboard click event for ' | ' key.
     *                                   v
     */
    private fun moveDown(input: Input) {
        if (input.getKey(KeyEvent.VK_DOWN) && !isPaused) {
            shape.down()
            input.map[KeyEvent.VK_DOWN] = false
        }
    }

    /**
     *                                   ^
     * Handle keyboard click event for ' | ' key.
     */
    private fun rotate(input: Input) {
        if (input.getKey(KeyEvent.VK_UP) && !isPaused) {
            shape.rotate(array)
            input.map[KeyEvent.VK_UP] = false
        }
    }

    /**
     * Handle keyboard click event for ' P ' key.
     */
    private fun pause(input: Input) {
        if (input.getKey(KeyEvent.VK_P) && activeAnimations.isEmpty()) {
            if (!isPaused) {
                isPaused = true
                shape.pause()
            } else {
                isPaused = false
                shape.resume()
            }
            input.map[KeyEvent.VK_P] = false
        }
    }

    /**
     * Handle keyboard click event for ' SPACE ' key.
     */
    private fun moveInstantDown(input: Input) {
        if (input.getKey(KeyEvent.VK_SPACE) && !isPaused && activeAnimations.isEmpty()) {
            while (shape.active) {
                shape.down()
                if (nodes != 0 && !gameOver) {
                    for (n in shape.body) {
                        if (n.y < array.size - 1) {
                            if (array[n.y + 1][n.x] != null) {
                                shape.active = false
                                newShape()
                                if (activeAnimations.isEmpty()) {
                                    checkLine()
                                }
                                input.map[KeyEvent.VK_SPACE] = false
                                return
                            }
                        }
                    }
                }
            }
            shape.active = false
            if (!gameOver) {
                newShape()
            }
            if (activeAnimations.isEmpty()) {
                checkLine()
            }
            input.map[KeyEvent.VK_SPACE] = false
        }
    }

    /**
     * Enables shadow mode.
     */
    private fun toggleShadow(input: Input) {
        if (input.getKey(KeyEvent.VK_S)) {
            isShadowEnabled = !isShadowEnabled
            input.map[KeyEvent.VK_S] = false
        }
    }

    /**
     * Render shadow.
     */
    private fun renderShadow(g: Graphics) {
        shadow.forEach { it!!.renderShadow(g) }
    }

    /**
     * Check for vertically collisions.
     */
    private fun checkCollisions() {
        for (n in shape.body) {
            if (n.y < array.size - 1) {
                if (array[n.y + 1][n.x] != null) {
                    if (n.y == 1) {
                        gameOver = true
                    }
                    shape.isCollision = true
                    return
                }
            }
        }
    }

    /**
     * Generate new Shape and moves Nodes of old Shape to game field.
     */
    private fun newShape() {
        for (n in shape.body) {
            n.state = NOT_ACTIVE
            array[n.y][n.x] = n
            nodes++
        }
        shape.timer.cancel()
        shape = Shape(nextType, nextColor, level)
        initNextShape()
    }

    /**
     * Update all active line animations
     */
    private fun updateAnimations() {
        val completedAnimations = mutableListOf<LineAnimation>()
        for (animation in activeAnimations) {
            val stillRunning = animation.update()
            if (!stillRunning) {
                completedAnimations.add(animation)
            }
        }
        if (completedAnimations.isNotEmpty()) {
            for (animation in completedAnimations) {
                activeAnimations.remove(animation)
            }
            removeLines(completedAnimations.map { it.lineY })
            if (activeAnimations.isEmpty()) {
                checkLine()
            }
        }
        if (activeAnimations.isEmpty() && shape.isPaused && !isPaused) {
            shape.resume()
        }
    }

    /**
     * Remove multiple lines from the field after animations complete.
     */
    private fun removeLines(lineYs: List<Int>) {
        if (lineYs.isEmpty()) return
        val sortedLines = lineYs.sorted().reversed()
        val linesToRemove = sortedLines.toSet()
        for (lineY in linesToRemove) {
            for (x in 0 until w) {
                if (array[lineY][x] != null) {
                    nodes--
                }
            }
            array[lineY].fill(null)
        }
        var writeIndex = h - 1
        for (readIndex in (h - 1) downTo 0) {
            if (!linesToRemove.contains(readIndex)) {
                if (readIndex != writeIndex) {
                    for (x in 0 until w) {
                        val node = array[readIndex][x]
                        if (node != null) {
                            node.state = NOT_ACTIVE
                            node.alpha = 1.0f
                            node.blinkColor = null
                            val shiftDown = writeIndex - readIndex
                            node.y += shiftDown
                            array[writeIndex][x] = node
                        } else {
                            array[writeIndex][x] = null
                        }
                        array[readIndex][x] = null
                    }
                } else {
                    for (x in 0 until w) {
                        val node = array[readIndex][x]
                        if (node != null) {
                            node.state = NOT_ACTIVE
                            node.alpha = 1.0f
                            node.blinkColor = null
                        }
                    }
                }
                writeIndex--
            }
        }
        for (i in 0..writeIndex) {
            array[i].fill(null)
        }
        val numLinesRemoved = linesToRemove.size
        repeat(numLinesRemoved) {
            lines.inc()
        }
        if (lines.get().toInt() % 20 == 0) {
            level++
        }
    }

    /**
     * Remove a single line (legacy method, kept for compatibility)
     */
    private fun removeLine(y: Int) {
        removeLines(listOf(y))
    }

    /**
     * Checks whether the field contains full lines and starts animation.
     */
    private fun checkLine() {
        if (activeAnimations.isNotEmpty()) {
            return
        }
        val animatingLines = activeAnimations.map { it.lineY }.toSet()
        val completeLines = mutableListOf<Int>()
        for (i in array.size - 1 downTo 0) {
            if (!array[i].contains(null) && !animatingLines.contains(i)) {
                completeLines.add(i)
            }
        }
        for (lineY in completeLines) {
            val animation = LineAnimation(lineY, w)
            for (x in 0 until w) {
                val node = array[lineY][x]
                if (node != null) {
                    animation.addNode(x, node)
                }
            }
            activeAnimations.add(animation)
        }
    }

    /**
     * Render process of game field.
     * Render game field (Shape + all not active Nodes).
     */
    fun render(g: Graphics) {
        if (!gameOver && isShadowEnabled) {
            renderShadow(g)
        }
        shape.render(g)
        for (y in array.size - 1 downTo 0) {
            for (x in array[y].size - 1 downTo 0) {
                if (array[y][x] != null) {
                    array[y][x]!!.render(g)
                }
            }
        }
        // Show pause banner only if actually paused (not during line animation)
        if (shape.isPaused && activeAnimations.isEmpty()) {
            gameStateRender(g, "pause")
        }
        if (gameOver) {
            gameStateRender(g, "game over")
        }
    }

    /**
     * Save game (Model, Shape, Lines, Level) to file.
     */
    fun saveToFile() {
        GameSerializer.write(this)
    }

    /**
     * Create new random instance of Shape.
     */
    private fun randomShape(): Shape {
        val types = Type.values()
        val colors = NodeColor.values()
        val random = Random()
        return Shape(types[random.nextInt(types.size)], colors[random.nextInt(colors.size)], level)
    }

    /**
     * Generate Type and Color for next Shape.
     */
    private fun initNextShape() {
        val types = Type.values()
        val colors = NodeColor.values()
        val random = Random()
        nextType = types[random.nextInt(types.size)]
        nextColor = colors[random.nextInt(colors.size)]
    }

    /**
     * Render frame which say that Game is paused or Game is over.
     */
    private fun gameStateRender(g: Graphics, m: String) {
        g as Graphics2D
        g.color = Color.WHITE
        g.fillRect(20, 240, 160, 60)
        g.color = Color.BLACK
        g.fillRect(22, 242, 156, 56)
        g.color = Color.WHITE
        g.font = Font("Arial", Font.BOLD, 16)
        if (m == "pause") g.drawString(m.uppercase(), 73, 275) else g.drawString(m.uppercase(), 45, 275)
    }
}