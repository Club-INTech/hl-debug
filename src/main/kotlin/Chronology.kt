data class Frame(val time: String, val lidarPoints: MutableList<LidarPoint>, val position: XYO)

class Chronology {
    val frameMap = mutableMapOf<String, Frame>()
    val timestamps = mutableListOf<String>()
    private var lastXYO = XYO(0,0,0.0)

    fun setPosition(position: XYO, time: String) {
        val pos = this[time].position
        pos.x = position.x
        pos.y = position.y
        pos.orientation = position.orientation
        lastXYO = position
    }

    fun appendLidarPoint(lidarPoint: LidarPoint, time: String) {
        val frame = this[time]
        frame.lidarPoints += lidarPoint
    }

    operator fun get(time: String): Frame {
        if(time !in timestamps) {
            timestamps += time
            frameMap[time] = Frame(time, mutableListOf(), lastXYO.copy())
        }
        return frameMap[time]!!
    }
}