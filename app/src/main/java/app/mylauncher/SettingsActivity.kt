package app.mylauncher

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = rootLayout()
        root.addView(heading("Настройки"))
        root.addView(label("По умолчанию всё подбирается автоматически. Меняй, только если нужно."))

        val body = LinearLayout(this)
        body.orientation = LinearLayout.VERTICAL
        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // Рендерер
        body.addView(section("Графический драйвер (рендерер)"))
        body.addView(spinner(RENDERERS, Prefs.renderer(this)) { Prefs.setRenderer(this, it) })

        // Java
        val javaVersions = listOf(8, 17, 21)
        val javaAuto = check("Java: подбирать под версию игры автоматически", Prefs.javaAuto(this))
        val javaSpinner = spinner(
            javaVersions.map { "Java $it" },
            javaVersions.indexOf(Prefs.javaVersion(this)).coerceAtLeast(0)
        ) { Prefs.setJavaVersion(this, javaVersions[it]) }
        fun applyJava() {
            javaSpinner.visibility = if (javaAuto.isChecked) View.GONE else View.VISIBLE
        }
        javaAuto.setOnCheckedChangeListener { _, v ->
            Prefs.setJavaAuto(this, v)
            applyJava()
        }
        body.addView(section("Ядро игры"))
        body.addView(javaAuto)
        body.addView(javaSpinner)
        applyJava()

        // Память
        val ramAuto = check("Память: подбирать автоматически", Prefs.ramAuto(this))
        val ramSlider = slider("Память (RAM)", 1024, 4096, Prefs.ram(this), " МБ") { Prefs.setRam(this, it) }
        fun applyRam() {
            ramSlider.visibility = if (ramAuto.isChecked) View.GONE else View.VISIBLE
        }
        ramAuto.setOnCheckedChangeListener { _, v ->
            Prefs.setRamAuto(this, v)
            applyRam()
        }
        body.addView(ramAuto)
        body.addView(ramSlider)
        applyRam()

        // Разрешение
        body.addView(slider("Разрешение экрана", 50, 100, Prefs.scale(this), "%") { Prefs.setScale(this, it) })

        // Microsoft Client ID
        body.addView(section("Microsoft Client ID (для входа с лицензией)"))
        val idField = EditText(this)
        idField.setText(Prefs.msClientId(this))
        idField.hint = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
        idField.setSingleLine(true)
        body.addView(idField)
        body.addView(button("Сохранить Client ID") {
            Prefs.setMsClientId(this, idField.text.toString().trim())
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        })

        setContentView(root)
    }

    private fun section(text: String) = label(text).apply { setPadding(0, dp(24), 0, dp(4)) }

    private fun check(text: String, value: Boolean): CheckBox {
        val cb = CheckBox(this)
        cb.text = text
        cb.setTextColor(0xFFE6EAF0.toInt())
        cb.isChecked = value
        return cb
    }

    private fun spinner(items: List<String>, selected: Int, onPick: (Int) -> Unit): Spinner {
        val sp = Spinner(this)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        sp.setSelection(selected)
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                onPick(pos)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        return sp
    }

    private fun slider(name: String, lo: Int, hi: Int, value: Int, unit: String, onChange: (Int) -> Unit): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(16), 0, 0)
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
