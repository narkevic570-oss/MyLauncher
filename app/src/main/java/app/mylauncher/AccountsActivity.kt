package app.mylauncher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AccountsActivity : AppCompatActivity() {
    private var accounts = mutableListOf<Account>()
    private val items = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    @Volatile
    private var cancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = rootLayout()
        root.addView(heading("Аккаунты"))
        root.addView(label("Нажми на аккаунт, чтобы выбрать его. Долгое нажатие удаляет."))

        val list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.adapter = adapter
        root.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        list.setOnItemClickListener { _, _, pos, _ ->
            Prefs.setActiveAccount(this, accounts[pos].uuid)
            refresh()
        }
        list.setOnItemLongClickListener { _, _, pos, _ ->
            val a = accounts[pos]
            AlertDialog.Builder(this)
                .setTitle("Удалить ${a.name}?")
                .setPositiveButton("Удалить") { _, _ ->
                    Accounts.remove(this, a.uuid)
                    refresh()
                }
                .setNegativeButton("Отмена", null)
                .show()
            true
        }

        root.addView(button("Добавить оффлайн-аккаунт") { addOffline() })
        root.addView(button("Войти через Microsoft (лицензия)") { msLogin() })

        refresh()
        setContentView(root)
    }

    private fun refresh() {
        accounts = Accounts.list(this)
        val active = Prefs.activeAccount(this)
        items.clear()
        accounts.forEach {
            val mark = if (it.uuid == active) "● " else ""
            val kind = if (it.type == "offline") "оффлайн" else "Microsoft"
            items.add("$mark${it.name}  ($kind)")
        }
        adapter.notifyDataSetChanged()
    }

    private fun addOffline() {
        val et = EditText(this)
        et.hint = "Ник: 3–16 символов, латиница, цифры, _"
        AlertDialog.Builder(this)
            .setTitle("Оффлайн-аккаунт")
            .setView(et)
            .setPositiveButton("Добавить") { _, _ ->
                val n = et.text.toString().trim()
                if (!Regex("^[A-Za-z0-9_]{3,16}$").matches(n)) {
                    Toast.makeText(this, "Ник не подходит", Toast.LENGTH_LONG).show()
                } else {
                    Accounts.add(this, Accounts.offline(n))
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun msLogin() {
        val clientId = Prefs.msClientId(this)
        if (clientId.isBlank()) {
            AlertDialog.Builder(this)
                .setTitle("Нужен Client ID")
                .setMessage(
                    "Для входа через Microsoft нужен Client ID твоего приложения Azure, " +
                        "одобренный Mojang (форма: aka.ms/mce-reviewappid). " +
                        "Когда получишь его, вставь в Настройки → Microsoft Client ID."
                )
                .setPositiveButton("Ок", null)
                .show()
            return
        }
        cancelled = false
        var dlg: AlertDialog? = null
        Thread {
            try {
                val dc = MsAuth.requestDeviceCode(clientId)
                runOnUiThread {
                    val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", dc.userCode))
                    dlg = AlertDialog.Builder(this)
                        .setTitle("Вход через Microsoft")
                        .setMessage("Открой ${dc.url} и введи код (он уже скопирован):\n\n${dc.userCode}")
                        .setNegativeButton("Отмена") { _, _ -> cancelled = true }
                        .setCancelable(false)
                        .show()
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dc.url)))
                }
                val tokens = MsAuth.pollToken(clientId, dc) { cancelled }
                runOnUiThread { dlg?.setMessage("Проверяю аккаунт…") }
                val acc = MsAuth.xboxChain(tokens.first, tokens.second)
                Accounts.add(this, acc)
                runOnUiThread {
                    dlg?.dismiss()
                    refresh()
                    Toast.makeText(this, "Вход выполнен: ${acc.name}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    dlg?.dismiss()
                    if (!cancelled) {
                        AlertDialog.Builder(this)
                            .setTitle("Не удалось войти")
                            .setMessage(e.message ?: "Неизвестная ошибка")
                            .setPositiveButton("Ок", null)
                            .show()
                    }
                }
            }
        }.start()
    }
}
