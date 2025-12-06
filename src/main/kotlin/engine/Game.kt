package engine

import input.Input

import model.Model
import render.Display
import util.GameSerializer
import util.Stats
import util.Time
import java.awt.Graphics

import util.Time.second
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JOptionPane
import javax.swing.WindowConstants
import kotlin.system.exitProcess
import kotlinx.coroutines.*

import config.Configuration.height as h
import config.Configuration.width as w

/**
 * The game engine.
 */
class Game {

    val updateRate = 60.0f
    val updateInterval = second / updateRate
    val idleTime = 1L
    val clearColor = 0xff000000.toInt()

    @Volatile
    var running = false

    lateinit var display: Display
    lateinit var g: Graphics
    private var gameJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val keys: Input
    private val model: Model
    private val stats: Stats

    init {
        keys = Input()
        model = GameSerializer.read(keys) ?: Model(w, h, keys)
        stats = Stats(model.nextType, model.nextColor)
    }

    /**
     * Create and configure display frame.
     */
    private fun createAndShowGui() {
        display = Display(keys)
        display.create(clearColor)
        g = display.getGraphics()
        display.exitMenuItem.addActionListener { exitGame() }
        display.window.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exitGame()
            }
        })
    }

    /**
     * Update process.
     */
    private fun update() {
        model.update()
        stats.update(model.nextType, model.nextColor)
    }

    /**
     * Render process.
     */
    private fun render() {
        display.clear()
        stats.render(g, model.lines, model.level)
        model.render(g)
        display.swapBuffers()
    }

    /**
     * the core loop of game.
     */
    private suspend fun run() {
        createAndShowGui()
        var fps = 0
        var upd = 0
        var updl = 0
        var counter = 0L
        var lastTime = Time.get()
        var delta = 0.0F
        while (running) {
            var now = Time.get()
            var elapsedTime = now - lastTime
            lastTime = now
            counter += elapsedTime
            var render = false
            delta += (elapsedTime / updateInterval)
            while (delta > 1) {
                update()
                upd++
                delta--
                if (render) {
                    updl++
                } else {
                    render = true
                }
            }
            if (render) {
                render()
                fps++
            } else {
                delay(idleTime)
            }
            if (counter >= second) {
                fps = 0
                upd = 0
                updl = 0
                counter = 0
            }
        }
    }

    /**
     * Shutdown game coroutine.
     */
    private fun shutDown() {
        running = false
        display.destroy()
        runBlocking {
            gameJob?.join()
        }
        gameScope.cancel()
        util.SoundManager.shutdown()
        exitProcess(0)
    }

    /**
     * exit Game process.
     */
    private fun exitGame() {
        when (JOptionPane.showConfirmDialog(display.window, "Close with saving?", "Exit Game", JOptionPane.YES_NO_OPTION)) {
            JOptionPane.YES_OPTION -> model.saveToFile()
            JOptionPane.NO_OPTION -> println("without saving...")
        }
        shutDown()
        display.window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    }

    /**
     * Start game coroutine.
     */
    fun start() {
        if (running) {
            return
        }
        running = true
        gameJob = gameScope.launch {
            run()
        }
    }
}