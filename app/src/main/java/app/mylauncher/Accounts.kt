package app.mylauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Account(
    val type: String, // "offline" или "microsoft"
    val name: String,
    val uuid: String,
    val mcToken: String = "",
    val msRefresh: String = ""
)

object Accounts {
    fun list(c: Context): MutableList<Account> {
        val arr = try { JSONArray(Prefs.accountsJson(c)) } catch (e: Exception) { JSONArray() }
        return MutableList(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Account(
                o.getString("type"), o.getString("name"), o.getString("uuid"),
                o.optString("mcToken"), o.optString("msRefresh")
            )
        }
    }

    private fun save(c: Context, list: List<Account>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject().put("type", it.type).put("name", it.name).put("uuid", it.uuid)
                    .put("mcToken", it.mcToken).put("msRefresh", it.msRefresh)
            )
        }
        Prefs.setAccountsJson(c, arr.toString())
    }

    fun active(c: Context): Account? {
        val id = Prefs.activeAccount(c)
        return list(c).firstOrNull { it.uuid == id }
    }

    fun add(c: Context, a: Account) {
        val l = list(c).filter { it.uuid != a.uuid }.toMutableList()
        l.add(a)
        save(c, l)
        Prefs.setActiveAccount(c, a.uuid)
    }

    fun remove(c: Context, uuid: String) {
        val l = list(c).filter { it.uuid != uuid }
        save(c, l)
        if (Prefs.activeAccount(c) == uuid) Prefs.setActiveAccount(c, l.firstOrNull()?.uuid ?: "")
    }

    // Так считает UUID сама игра для оффлайн-ника.
    fun offline(name: String): Account {
        val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8)).toString()
        return Account("offline", name, uuid)
    }
}
