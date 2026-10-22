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
        root.addView(Ui.navHeader(this, L10n.t(this, "stats")))
        val blocks = prefs.blocksToday()
        val extensions = prefs.extensionsUsedToday()
        val maxExt = prefs.maxExtensionsPerDay
        root.addView(Ui.text(this, L10n.t(this, "today_blocks") + ": " + blocks +
            " · " + L10n.t(this, "today_extensions") + ": " + extensions + " / " + maxExt))
        val rules = prefs.rules()
        val usage = prefs.dailyUsageAll()
        if (rules.isEmpty()) {
            root.addView(Ui.text(this, L10n.t(this, "none_selected")))
        } else {
            rules.values.sortedByDescending { usage[it.packageName] ?: 0L }.forEach { rule ->
                val elapsed = (usage[rule.packageName] ?: 0L) / 60_000.0
                val description = rule.label + " · %.1f ".format(elapsed) + L10n.t(this, "minute") +
                    " · " + L10n.t(this, "limit") + ": " + rule.limitSeconds / 60 + " " + L10n.t(this, "minute")
                root.addView(Ui.text(this, description))
            }
        }
        root.addView(Ui.text(this, L10n.t(this, "today_info"), 14f))
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
