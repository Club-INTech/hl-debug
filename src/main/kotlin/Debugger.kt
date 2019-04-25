import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.concurrent.thread

val PointRegex = Regex("^.*\\[.*(?<time>[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]) LIDAR.* Circle \\[center: \\((?<x>[0-9]+),(?<y>[0-9]+).*\$")
val PositionRegex = Regex("\\[.*POSITION .*xy=\\((?<x>[0-9]+),(?<y>[0-9]+)\\), o=(?<orientation>.+)\\)")

data class XYO(var x: Int, var y: Int, var orientation: Double)
data class LidarPoint(val x: Int, val y: Int, val time: String, val robotPos: XYO)

class Debugger(points: List<LidarPoint>) : JFrame("HL Lidar Debugger") {

    private val panel = RenderPanel(points)

    init {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        contentPane = panel
        pack()
    }

    fun incrementFrame() {
        panel.incrementFrame()
    }
}

fun main() {
    // sélection du fichier de logs
    val fileChooser = JFileChooser()
    println("Sélection du fichier de logs")
    if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { // si fichier sélectionné
        val file = fileChooser.selectedFile
        println("Ouverture du fichier ${file.canonicalPath}")
        val reader = BufferedReader(FileReader(file))
        val points = LinkedList<LidarPoint>()

        val currentPosition = XYO(0,0,0.0)

        // lecture des points
        reader.lines().forEach { line ->
            val matchResult = PointRegex.matchEntire(line)
            if(matchResult != null) {
                val x = matchResult.groups["x"]!!.value.toInt()
                val y = matchResult.groups["y"]!!.value.toInt()
                val time = matchResult.groups["time"]!!.value
                points += LidarPoint(x, y, time, currentPosition.copy())
                println("Point trouvé: ($x, $y, $time)")
            } else {
                val posResult = PositionRegex.matchEntire(line) ?: return@forEach
                val x = posResult.groups["x"]!!.value.toInt()
                val y = posResult.groups["y"]!!.value.toInt()
                val orientation = posResult.groups["orientation"]!!.value.toDouble()
                currentPosition.x = x
                currentPosition.y = y
                currentPosition.orientation = orientation
            }
        }

        // création de la fenêtre
        val debugger = Debugger(points)

        thread(isDaemon = true) {
            while (true) {
                debugger.incrementFrame()
                debugger.repaint()
                Thread.sleep(16)
            }
        }
        debugger.isVisible = true
    }
}
