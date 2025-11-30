package config

import java.lang.RuntimeException


/**
 * Contains configuration app properties.
 */
object Configuration {

    /**
     * the application name.
     */
    const val title = "Tetris"

    /**
     * the application logo.
     */
    const val logo = "icon/logo.png"

    /**
     * the logo for info frame.
     */
    const val infoLogo = "icon/big-logo.png"

    /**
     * the statistic Display height px.
     */
    const val statDisplayHeight = 80

    /**
     *  the game Display height px.
     */
    const val gameDisplayHeight = 460

    /**
     * the Display width px.
     */
    const val displayWidth = 200

    /**
     * the model.Node width/height px.
     */
    const val nodeSize = 20

    /**
     * the number Nodes of horizontally.
     */
    const val height = gameDisplayHeight / nodeSize

    /**
     * the number Nodes of vertically.
     */
    const val width = displayWidth / nodeSize

    /**
     * the file path for game save.
     */
     val filePath = filePath()

    /**
     * Get file path for game.dat file, depending on the os.
     */
    private fun filePath():String {
        val os = System.getProperty("os.name").lowercase()
        val user = System.getProperty("user.name")
        if (os.contains("windows")) {
            return "C:\\tetris\\game.dat"
        }
        if (os.contains("linux")) {
            return "/home/$user/tetris/game.dat"
        }
        if (os.contains("mac")) {
            return "/Users/$user/tetris/game.dat"
        }
        throw RuntimeException("Unsupported OS")
    }
}