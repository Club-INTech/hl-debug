import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.imageio.ImageIO
import javax.swing.JPanel

class RenderPanel(private val chronology: Chronology): JPanel() {

    private val FRAME_AT_ONCE_COUNT = 10
    private val ASPECT_RATIO = 16/9f
    private val HEIGHT = 720
    private val WIDTH = (ASPECT_RATIO*HEIGHT).toInt()
    private val image = ImageIO.read(javaClass.getResourceAsStream("/table.png"))
    private val GOLD = Color(255, 215, 0, 100)
    private val SILVER = Color(192, 192, 192, 50)

    private var i = 0

    private val frames = chronology.timestamps

    init {
        preferredSize = Dimension(WIDTH, HEIGHT)
    }

    override fun paint(g: Graphics) {
        g.drawImage(image, 0, 0, width, height, null)

        val radius = (220/3000f*WIDTH).toInt()
        val robotRadius = (190/3000f*WIDTH).toInt()


        for(index in i until minOf(frames.size, i+FRAME_AT_ONCE_COUNT)) {
            val frame = chronology[chronology.timestamps[index]]
            for(point in frame.lidarPoints) {
                val centerX = ((point.x + 1500) / 3000f * width).toInt()
                val centerY = ((2000 - point.y) / 2000f * height).toInt()
                g.color = GOLD
                g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2)

            }

            g.color = SILVER
            val robotLocation = frame.position
            val robotX = ((robotLocation.x + 1500) / 3000f * width).toInt()
            val robotY = ((2000-robotLocation.y) / 2000f * height).toInt()
            g.fillOval(robotX-robotRadius, robotY-robotRadius, robotRadius*2, robotRadius*2)

            g.color = Color.GREEN
            val lookVectorPixelLength = 150f
            val directionX = (Math.cos(robotLocation.orientation)*lookVectorPixelLength).toInt()
            val directionY = -(Math.sin(robotLocation.orientation)*lookVectorPixelLength).toInt()
            g.drawLine(robotX, robotY, directionX+robotX, directionY+robotY)

            g.color = Color.GREEN
            g.drawString(frame.time, 0, 10+(index-i)*10)
        }

        i %= frames.size
    }

    fun incrementFrame() {
        i++
    }
}
