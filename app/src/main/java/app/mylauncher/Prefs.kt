package app.mylauncher

import android.content.Context

val RENDERERS = listOf("GL4ES", "Zink (Vulkan)", "ANGLE")

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    fun setupDone(c: Context) = sp(c).getBoolean("setup_done", false)
    fun setSetupDone(c: Context) = sp(c).edit().putBoolean("setup_done", true).apply()

    fun renderer(c: Context) = sp(c).getInt("renderer", 0)
    fun setRenderer(c: Context, v: Int) = sp(c).edit().putInt("renderer", v).apply()

    fun scale(c: Context) = sp(c).getInt("scale", 100)
    fun setScale(c: Context, v: Int) = sp(c).edit().putInt("scale", v).apply()

    fun ram(c: Context) = sp(c).getInt("ram", 2048)
    fun setRam(c: Context, v: Int) = sp(c).edit().putInt("ram", v).apply()

    fun selected(c: Context): String = sp(c).getString("selected", "") ?: ""
    fun setSelected(c: Context, v: String) = sp(c).edit().putString("selected", v).apply()
}
