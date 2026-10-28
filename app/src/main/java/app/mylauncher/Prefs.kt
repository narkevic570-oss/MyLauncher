package app.mylauncher

import android.content.Context

val RENDERERS = listOf("Авто", "GL4ES", "Zink (Vulkan)", "ANGLE")

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    fun setupDone(c: Context) = sp(c).getBoolean("setup_done", false)
    fun setSetupDone(c: Context) = sp(c).edit().putBoolean("setup_done", true).apply()

    fun renderer(c: Context) = sp(c).getInt("renderer", 0)
    fun setRenderer(c: Context, v: Int) = sp(c).edit().putInt("renderer", v).apply()

    fun scale(c: Context) = sp(c).getInt("scale", 100)
    fun setScale(c: Context, v: Int) = sp(c).edit().putInt("scale", v).apply()

    fun ramAuto(c: Context) = sp(c).getBoolean("ram_auto", true)
    fun setRamAuto(c: Context, v: Boolean) = sp(c).edit().putBoolean("ram_auto", v).apply()
    fun ram(c: Context) = sp(c).getInt("ram", 2048)
    fun setRam(c: Context, v: Int) = sp(c).edit().putInt("ram", v).apply()

    fun javaAuto(c: Context) = sp(c).getBoolean("java_auto", true)
    fun setJavaAuto(c: Context, v: Boolean) = sp(c).edit().putBoolean("java_auto", v).apply()
    fun javaVersion(c: Context) = sp(c).getInt("java_version", 17)
    fun setJavaVersion(c: Context, v: Int) = sp(c).edit().putInt("java_version", v).apply()

    fun selected(c: Context): String = sp(c).getString("selected", "") ?: ""
    fun setSelected(c: Context, v: String) = sp(c).edit().putString("selected", v).apply()

    fun accountsJson(c: Context): String = sp(c).getString("accounts", "[]") ?: "[]"
    fun setAccountsJson(c: Context, v: String) = sp(c).edit().putString("accounts", v).apply()
    fun activeAccount(c: Context): String = sp(c).getString("active_account", "") ?: ""
    fun setActiveAccount(c: Context, v: String) = sp(c).edit().putString("active_account", v).apply()

    fun msClientId(c: Context): String = sp(c).getString("ms_client_id", "") ?: ""
    fun setMsClientId(c: Context, v: String) = sp(c).edit().putString("ms_client_id", v).apply()
}
