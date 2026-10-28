package app.mylauncher

import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Net {
    const val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    fun download(url: String, out: File, onProgress: (Int) -> Unit) {
        out.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        val total = conn.contentLength
        conn.inputStream.use { input ->
            out.outputStream().use { output ->
                val buf = ByteArray(16384)
                var done = 0L
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    output.write(buf, 0, n)
                    done += n
                    if (total > 0) onProgress((done * 100 / total).toInt())
                }
            }
        }
        onProgress(100)
    }
}
