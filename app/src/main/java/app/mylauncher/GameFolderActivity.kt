package app.mylauncher

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class GameFolderActivity : AppCompatActivity() {
    private lateinit var base: File
    private lateinit var cur: File
    private val names = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var pathView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        base = File(getExternalFilesDir(null), "game")
        for (d in listOf("mods", "saves", "resourcepacks", "config")) File(base, d).mkdirs()
        cur = base

        val root = rootLayout()
        root.addView(heading("Папка игры"))
        pathView = label("")
        root.addView(pathView)

        val list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        list.adapter = adapter
        root.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        list.setOnItemClickListener { _, _, pos, _ ->
            val n = names[pos]
            if (n == "..") {
                cur.parentFile?.let { cur = it }
            } else {
                val f = File(cur, n.removeSuffix("/"))
                if (f.isDirectory) cur = f
            }
            refresh()
        }
        refresh()
        setContentView(root)
    }

    private fun refresh() {
        pathView.text = cur.absolutePath
        names.clear()
        if (cur != base) names.add("..")
        cur.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?.forEach { names.add(if (it.isDirectory) it.name + "/" else it.name) }
        adapter.notifyDataSetChanged()
    }
}
