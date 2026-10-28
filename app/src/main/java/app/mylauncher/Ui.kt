package app.mylauncher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun Context.rootLayout(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setBackgroundColor(0xFF12161C.toInt())
    val l = dp(20)
    val t = dp(12)
    val r = dp(20)
    val b = dp(20)
    setPadding(l, t, r, b)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.setPadding(l + bars.left, t + bars.top, r + bars.right, b + bars.bottom)
        insets
    }
}

fun Context.heading(text: String): TextView = TextView(this).apply {
    this.text = text
    textSize = 26f
    setTextColor(Color.WHITE)
    setPadding(0, dp(8), 0, dp(12))
}

fun Context.label(text: String): TextView = TextView(this).apply {
    this.text = text
    textSize = 14f
    setTextColor(0xFFB0B8C4.toInt())
}

fun Context.button(text: String, onClick: () -> Unit): Button = Button(this).apply {
    this.text = text
    isAllCaps = false
    textSize = 16f
    setTextColor(Color.WHITE)
    background = GradientDrawable().apply {
        cornerRadius = dp(14).toFloat()
        setColor(0xFF2E7D32.toInt())
    }
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
    ).apply { topMargin = dp(10) }
    setOnClickListener { onClick() }
}
