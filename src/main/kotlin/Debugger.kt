import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.WindowConstants
import kotlin.concurrent.thread

val PointRegex = Regex("^.*\\[.*(?<time>[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]) LIDAR.* Circle \\[center: \\((?<x>[0-9]+),(?<y>[0-9]+).*\$")
val PositionRegex = Regex("\\[.*(?<time>[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]) POSITION .*xy=\\((?<x>[0-9]+),(?<y>[0-9]+)\\), o=(?<orientation>.+)\\)")

data class XYO(var x: Int, var y: Int, var orientation: Double)
data class LidarPoint(val x: Int, val y: Int)

class Debugger(chronology: Chronology) : JFrame("HL Lidar Debugger") {

    private val panel = RenderPanel(chronology)

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

        val chronology = Chronology()
        val currentPosition = XYO(0,0,0.0)

        // lecture des points
        reader.lines().forEach { line ->
            val matchResult = PointRegex.matchEntire(line)
            if(matchResult != null) {
                println("lidar: $line")
                val x = matchResult.groups["x"]!!.value.toInt()
                val y = matchResult.groups["y"]!!.value.toInt()
                val time = matchResult.groups["time"]!!.value
                chronology.appendLidarPoint(LidarPoint(x, y), time)
                println("Point trouvé: ($x, $y)")
            } else {
                val posResult = PositionRegex.matchEntire(line) ?: return@forEach
                println("pos: $line")
                val x = posResult.groups["x"]!!.value.toInt()
                val y = posResult.groups["y"]!!.value.toInt()
                val time = posResult.groups["time"]!!.value
                val orientation = posResult.groups["orientation"]!!.value.toDouble()
                currentPosition.x = x
                currentPosition.y = y
                currentPosition.orientation = orientation
                chronology.setPosition(currentPosition.copy(), time)
            }
        }

        // création de la fenêtre
        val debugger = Debugger(chronology)

        thread(isDaemon = true) {
            while (true) {
                debugger.incrementFrame()
                debugger.repaint()
                Thread.sleep(15)
            }
        }
        debugger.isVisible = true
    }
}
