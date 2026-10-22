package app.barapause.mobile

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ColorDrawable

data class Palette(val bg:Int,val card:Int,val text:Int,val muted:Int,val accent:Int,val stroke:Int,val button:Int)

object Appearance {
    fun palette(context: Context): Palette {
        val pref = context.getSharedPreferences("barapause",Context.MODE_PRIVATE).getString("theme","system") ?: "system"
        val darkSystem = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return when(if (pref=="system") if (darkSystem) "dark" else "light" else pref) {
            "dark" -> Palette(Color.rgb(24,27,27),Color.rgb(37,42,42),Color.rgb(239,241,237),Color.rgb(182,192,183),Color.rgb(152,196,162),Color.rgb(69,77,72),Color.rgb(60,73,63))
            "eye" -> Palette(Color.rgb(232,232,212),Color.rgb(245,243,225),Color.rgb(66,70,57),Color.rgb(108,112,95),Color.rgb(100,131,104),Color.rgb(214,218,194),Color.rgb(219,231,202))
            else -> Palette(Color.rgb(248,243,231),Color.rgb(255,251,242),Color.rgb(62,48,38),Color.rgb(113,97,81),Color.rgb(106,145,111),Color.rgb(229,217,199),Color.rgb(239,227,205))
        }
    }
    fun panel(context:Context):GradientDrawable {
        val p = palette(context)
        return GradientDrawable().apply { shape=GradientDrawable.RECTANGLE;setColor(p.card);cornerRadius=24f*context.resources.displayMetrics.density;setStroke((context.resources.displayMetrics.density).toInt().coerceAtLeast(1),p.stroke) }
    }
    fun applyWindow(activity:Activity) {
        val p=palette(activity)
        activity.window.setBackgroundDrawable(ColorDrawable(p.bg))
        activity.window.decorView.setBackgroundColor(p.bg)
        activity.window.statusBarColor=p.bg
        activity.window.navigationBarColor=p.bg
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility=if (p.bg==Color.rgb(24,27,27)) 0 else android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
}
