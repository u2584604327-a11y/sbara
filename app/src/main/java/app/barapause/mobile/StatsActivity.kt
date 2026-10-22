package app.barapause.mobile

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView

class StatsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        val prefs = Prefs(this)
        prefs.rollDayIfNeeded()
        val root = Ui.column(this)
        root.addView(Ui.title(this, L10n.t(this,"stats")))
        root.addView(Ui.text(this, L10n.t(this,"today_blocks")+": ${prefs.blocksToday()}   ·   "+L10n.t(this,"today_extensions")+": ${prefs.extensionsUsedToday()} / ${prefs.maxExtensionsPerDay}))
        val rules = prefs.rules()
        val usage = prefs.dailyUsageAll()
        if (rules.isEmpty()) {
            root.addView(Ui.text(this, L10n.t(this,"none_selected")))
        } else {
            rules.values.sortedByDescending { usage[it.packageName] ?: 0L }.forEach { rule ->
                val ms = usage[rule.packageName] ?: 0L
                val min = ms / 60_000.0
                root.addView(Ui.text(this, "${rule.label}  ·  %.1f ".format(min)+L10n.t(this,"minute")+"  ·  "+L10n.t(this,"limit")+": ${rule.limitSeconds / 60} "+L10n.t(this,"minute")))
            }
        }
        root.addView(Ui.text(this, L10n.t(this,"today_info"), 14f))
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
