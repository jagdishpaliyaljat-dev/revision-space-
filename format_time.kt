fun formatRevisionTime(mins: Int): String {
    if (mins == 0) return "0 min"
    if (mins < 60) return "$mins min"
    val h = mins / 60
    val m = mins % 60
    if (m == 0) return "${h}h"
    return "${h}h ${m}m"
}
