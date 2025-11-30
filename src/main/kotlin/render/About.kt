package render

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.*

import config.Configuration.infoLogo as logo


/**
 * Info frame.
 * Represent application info.
 */
class About(s: Dimension): JDialog() {

    init {
        title = "About"
        setSize(s.width + 18, s.height / 3 + 100)
        isResizable = false

        val font = Font("Arial", Font.BOLD, 12)

        val rootPanel = JPanel(BorderLayout())

        val namePanel = JPanel()
        val nameLabel = JLabel("\u00a9 Tetris v1.0", SwingConstants.CENTER)
        nameLabel.font = font
        nameLabel.preferredSize = Dimension(s.width + 18, 20)

        val imgPanel = JPanel()
        val imgLabel = JLabel(ImageIcon(this.javaClass.classLoader.getResource(logo)))
        imgLabel.preferredSize = Dimension(s.width + 18, 120)

        val infoPanel = JPanel(GridLayout(3, 1))

        val authorLabel = JLabel("Alexander Naumov", SwingConstants.CENTER)
        authorLabel.preferredSize = Dimension(s.width + 18, 10)

        val infoLabel = JLabel("<html><a href=\"https://www.linkedin.com/in/alexander-naumov-913991134\">https://www.linkedin.com/in/alexander-naumov-913991134</a></html>", SwingConstants.CENTER)
        infoLabel.font = font
        infoLabel.preferredSize = Dimension(s.width + 18, 20)
        infoLabel.cursor = Cursor(Cursor.HAND_CURSOR)
        infoLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                Desktop.getDesktop().browse(URI("https://www.linkedin.com/in/alexander-naumov-913991134"));
            }
        })

        val emptyLabel = JLabel()
        emptyLabel.preferredSize = Dimension(s.width + 18, 10)

        namePanel.add(nameLabel)
        imgPanel.add(imgLabel)
        infoPanel.add(authorLabel)
        infoPanel.add(infoLabel)
        infoPanel.add(emptyLabel)

        rootPanel.add(namePanel, BorderLayout.NORTH)
        rootPanel.add(imgPanel, BorderLayout.CENTER)
        rootPanel.add(infoPanel, BorderLayout.SOUTH)

        contentPane.add(rootPanel)
    }
}