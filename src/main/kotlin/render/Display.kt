package render


import java.awt.image.BufferStrategy

import config.Configuration.statDisplayHeight as sh
import config.Configuration.gameDisplayHeight as gh
import config.Configuration.displayWidth as w
import config.Configuration.title as appName
import config.Configuration.logo
import input.Input
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.util.*
import javax.swing.*


/**
 * Represents application window.
 */
class Display(val input:Input) {

    private var created = false
    private var clearColor:Int = 0

    lateinit var window:JFrame
    lateinit var content:Canvas
    lateinit var buffer:BufferedImage
    lateinit var bufferData:IntArray
    lateinit var bufferGraphics: Graphics
    lateinit var bufferStrategy:BufferStrategy

    lateinit var exitMenuItem:JMenuItem

    /**
     * Create game frame.
     */
    fun create(_clearColor:Int) {
        if (created){
            return
        }
        window = JFrame()
        window.title = appName
        window.iconImage = ImageIcon(this::class.java.classLoader.getResource(logo)).image

        val size = Dimension(w, gh + sh)

        content = Canvas()
        content.preferredSize = size

        val a = About(size)

        val menuBar = JMenuBar()
        val menuFont = Font("Arial", Font.BOLD, 10)

        val game = JMenu("Game")
        val help = JMenu("Help")

        val new = JMenuItem("New")
        val load = JMenuItem("Load")
        val scores =JMenuItem("Scores")
        exitMenuItem = JMenuItem("Exit")
        val about =JMenuItem("About")

        game.font = menuFont
        new.font = menuFont
        load.font = menuFont
        scores.font = menuFont
        exitMenuItem.font = menuFont
        help.font = menuFont
        about.font = menuFont

        game.add(new)
        game.add(load)
        game.add(scores)
        game.add(exitMenuItem)

        help.add(about)

        menuBar.add(game)
        menuBar.add(help)

        window.jMenuBar = menuBar
        window.isResizable = false
        window.contentPane.add(content)
        window.pack()
        window.add(input)
        window.setLocationRelativeTo(null)
        window.isVisible = true

        new.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)
        load.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
        scores.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)
        exitMenuItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F5,  0)

        about.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)
        about.addActionListener {
            a.location = Point(window.location.x, (window.location.y + size.height / 2) - 80)
            a.isVisible = true
        }

        buffer = BufferedImage(w, gh + sh, BufferedImage.TYPE_INT_ARGB)
        bufferData = (buffer.raster.dataBuffer as DataBufferInt).data
        bufferGraphics = buffer.graphics

        (bufferGraphics as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        clearColor = _clearColor

        content.createBufferStrategy(3)
        bufferStrategy = content.bufferStrategy

        created = true
    }

    fun clear() {
        Arrays.fill(bufferData, clearColor)
    }

    fun swapBuffers() {
        val g = bufferStrategy.drawGraphics
        g.drawImage(buffer, 0, 0, null)
        bufferStrategy.show()
    }

    fun getGraphics():Graphics2D {
        return bufferGraphics as Graphics2D
    }

    fun destroy() {
        if (!created)
            return
        window.dispose();
    }

}