package app.mylauncher

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File

class VersionsActivity : AppCompatActivity() {
    private val all = mutableListOf<Pair<String, String>>() // id, type
    private val shown = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = rootLayout()
        root.addView(heading("Версии"))

        val cb = CheckBox(this)
        cb.text = "Показывать снапшоты и старые версии"
        cb.setTextColor(0xFFB0B8C4.toInt())
        root.addView(cb)

        val list = ListView(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shown)
        list.adapter = adapter
        root.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        try {
            val arr = JSONObject(File(filesDir, "version_manifest_v2.json").readText()).getJSONArray("versions")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                all.add(o.getString("id") to o.getString("type"))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось прочитать список версий: ${e.message}", Toast.LENGTH_LONG).show()
        }

        fun refresh() {
            shown.clear()
            all.filter { cb.isChecked || it.second == "release" }.forEach { shown.add(it.first) }
            adapter.notifyDataSetChanged()
        }
        cb.setOnCheckedChangeListener { _, _ -> refresh() }
        refresh()

        list.setOnItemClickListener { _, _, pos, _ -> pick(shown[pos]) }
        setContentView(root)
    }

    private fun pick(id: String) {
        val loaders = arrayOf("Vanilla", "Fabric", "Forge", "OptiFine")
        AlertDialog.Builder(this)
            .setTitle("Minecraft $id")
            .setItems(loaders) { _, i ->
                Prefs.setSelected(this, "$id · ${loaders[i]}")
                // TODO (следующий шаг): скачать клиент, библиотеки, ассеты и установить загрузчик.
                Toast.makeText(this, "Выбрано. Скачивание подключим на следующем шаге", Toast.LENGTH_LONG).show()
                finish()
            }
            .show()
    }
}
