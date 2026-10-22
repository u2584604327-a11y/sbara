package app.barapause.mobile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Ui {
    fun dp(context: Context, n: Int): Int = (n * context.resources.displayMetrics.density + .5f).toInt()

    fun column(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 18), dp(context, 18), dp(context, 18), dp(context, 30))
        setBackgroundColor(Appearance.palette(context).bg)
    }

    fun title(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 25f
        setTextColor(Appearance.palette(context).text)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(context, 8), 0, dp(context, 10))
    }

    fun text(context: Context, text: String, size: Float = 16f): TextView = TextView(context).apply {
        this.text = text
        textSize = size
        setTextColor(Appearance.palette(context).text)
        setPadding(0, dp(context, 5), 0, dp(context, 7))
    }

    fun section(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Appearance.palette(context).accent)
        setPadding(0, dp(context, 15), 0, dp(context, 6))
    }

    fun button(context: Context, text: String, onClick: () -> Unit): Button = Button(context).apply {
        this.text = text
        isAllCaps = false
        textSize = 15f
        setTextColor(Appearance.palette(context).text)
        backgroundTintList = ColorStateList.valueOf(Appearance.palette(context).button)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 4)
        }
    }

    fun cardBackground(context: Context): GradientDrawable = Appearance.panel(context)
}
