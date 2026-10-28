package app.mylauncher

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = rootLayout()
        root.addView(heading("Настройки"))

        root.addView(label("Графический драйвер (рендерер)"))
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, RENDERERS)
        spinner.setSelection(Prefs.renderer(this))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                Prefs.setRenderer(this@SettingsActivity, pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        root.addView(spinner)

        root.addView(slider("Разрешение экрана", 50, 100, Prefs.scale(this), "%") { Prefs.setScale(this, it) })
        root.addView(slider("Память (RAM)", 1024, 4096, Prefs.ram(this), " МБ") { Prefs.setRam(this, it) })

        setContentView(root)
    }

    private fun slider(name: String, lo: Int, hi: Int, value: Int, unit: String, onChange: (Int) -> Unit): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(24), 0, 0)
        val tv = label("$name: $value$unit")
        val sb = SeekBar(this)
        sb.max = hi - lo
        sb.progress = value - lo
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val v = p + lo
                tv.text = "$name: $v$unit"
                onChange(v)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        box.addView(tv)
        box.addView(sb)
        return box
    }
}
