package app.mylauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var versionLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Prefs.setupDone(this)) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        val root = rootLayout()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(heading("MyLauncher"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(Button(this).apply {
            text = "⚙"
            textSize = 24f
            setTextColor(Color.WHITE)
            background = null
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        })
        root.addView(top)

        versionLabel = label("")
        versionLabel.textSize = 16f
        root.addView(versionLabel)

        root.addView(button("Выбрать / скачать версию") {
            startActivity(Intent(this, VersionsActivity::class.java))
        })
        root.addView(button("Папка игры") {
            startActivity(Intent(this, GameFolderActivity::class.java))
        })
        root.addView(button("▶  Играть") {
            Toast.makeText(this, "Ядро запуска игры подключим на следующем шаге", Toast.LENGTH_LONG).show()
        })

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        if (::versionLabel.isInitialized) {
            val v = Prefs.selected(this)
            versionLabel.text = if (v.isEmpty()) "Версия не выбрана" else "Выбрано: $v"
        }
    }
}
