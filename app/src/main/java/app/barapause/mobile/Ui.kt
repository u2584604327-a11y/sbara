package app.barapause.mobile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Ui {
    fun column(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, 22), dp(context, 20), dp(context, 22), dp(context, 36))
        setBackgroundColor(Color.rgb(248, 243, 231))
    }

    fun title(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 28f
        setTextColor(Color.rgb(62, 48, 38))
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(context, 10), 0, dp(context, 14))
    }

    fun text(context: Context, text: String, size: Float = 16f): TextView = TextView(context).apply {
        this.text = text
        textSize = size
        setTextColor(Color.rgb(79, 66, 55))
        setPadding(0, dp(context, 7), 0, dp(context, 7))
    }

    fun section(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 13f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Color.rgb(111, 126, 102))
        setPadding(0, dp(context, 18), 0, dp(context, 4))
    }

    fun button(context: Context, text: String, onClick: () -> Unit): Button = Button(context).apply {
        this.text = text
        isAllCaps = false
        textSize = 15f
        setTextColor(Color.rgb(63, 50, 39))
        backgroundTintList = ColorStateList.valueOf(Color.rgb(239, 227, 205))
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(context, 7)
        }
    }

    fun cardBackground(context: Context): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(Color.rgb(255, 251, 242))
        cornerRadius = dp(context, 22).toFloat()
        setStroke(dp(context, 1), Color.rgb(231, 219, 199))
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
