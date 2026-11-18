import java.util.zip.ZipFile

fun main() {
    val apk = ZipFile("app/build/outputs/apk/debug/app-debug.apk")
    val entries = apk.entries()
    while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        if (entry.name.contains("AppDatabase")) {
            println(entry.name)
        }
    }
}
