package app.mylauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = rootLayout()
        root.addView(heading("Первый запуск"))
        root.addView(label("Нужно скачать файлы, без которых лаунчер не сможет запускать игру."))

        val status = label("Нажми «Скачать», чтобы начать.")
        status.setPadding(0, dp(20), 0, dp(8))
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        bar.max = 100
        root.addView(status)
        root.addView(bar)

        lateinit var btn: Button
        btn = button("Скачать") {
            btn.isEnabled = false
            Thread {
                try {
                    runOnUiThread { status.text = "Скачиваю список версий Minecraft…" }
                    Net.download(Net.MANIFEST_URL, File(filesDir, "version_manifest_v2.json")) { p ->
                        runOnUiThread { bar.progress = p }
                    }
                    // TODO (следующий шаг): скачать Java runtime и рендереры, проверить хэши.
                    Prefs.setSetupDone(this)
                    runOnUiThread {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        status.text = "Ошибка: ${e.message}"
                        btn.isEnabled = true
                    }
                }
            }.start()
        }
        root.addView(btn)
        setContentView(root)
    }
}
