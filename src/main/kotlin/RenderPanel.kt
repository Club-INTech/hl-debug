import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.imageio.ImageIO
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min

class RenderPanel(private val points: List<LidarPoint>): JPanel() {

    private val ASPECT_RATIO = 16/9f
    private val HEIGHT = 720
    private val WIDTH = (ASPECT_RATIO*HEIGHT).toInt()
    private val image = ImageIO.read(javaClass.getResourceAsStream("/table.png"))
    private val GOLD = Color(255, 215, 0, 10)
    private val SILVER = Color(192, 192, 192, 10)

    private var i = 0

    init {
        preferredSize = Dimension(WIDTH, HEIGHT)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.color = Color.BLACK
        g.fillRect(0, 0, width, height)
        g.drawImage(image, 0, 0, width, height, null)

        val radius = (220/3000f*WIDTH).toInt()
        val robotRadius = (190/3000f*WIDTH).toInt()

        for(index in i until min(points.size, i+100)) {
            val point = points[index]
            val centerX = ((point.x + 1500) / 3000f * width).toInt()
            val centerY = ((2000-point.y) / 2000f * height).toInt()
            g.color = GOLD
            g.fillOval(centerX-radius, centerY-radius, radius*2, radius*2)

            g.color = SILVER

            val robotX = ((point.robotPos.x + 1500) / 3000f * width).toInt()
            val robotY = ((2000-point.robotPos.y) / 2000f * height).toInt()
            g.fillOval(robotX-robotRadius, robotY-robotRadius, robotRadius*2, robotRadius*2)

            g.color = Color.GREEN
            val lookVectorPixelLength = 75
            val directionX = (Math.cos(point.robotPos.orientation)*lookVectorPixelLength).toInt()
            val directionY = -(Math.sin(point.robotPos.orientation)*lookVectorPixelLength).toInt()
            g.drawLine(robotX, robotY, directionX+robotX-robotRadius, directionY+robotY-robotRadius)
        }

        i %= points.size

        g.color = Color.GREEN
        g.drawString(points[i].time, 0, 100)
    }

    fun incrementFrame() {
        i++
    }
}
