package app.barapause.mobile

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView

class StatsActivity : Activity() {
    private lateinit var page: ScrollView
    private lateinit var store: Prefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        val prefs = Prefs(this)
        store = prefs
        prefs.rollDayIfNeeded()
        val root = Ui.column(this)
        root.minimumHeight = resources.displayMetrics.heightPixels
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
        page = ScrollView(this).apply { isFillViewport = true; setBackgroundColor(Appearance.palette(this@StatsActivity).bg); addView(root) }
        setContentView(page)
        page.post { page.scrollTo(0,store.savedScroll("stats")) }
    }

    override fun onPause() {
        if (::page.isInitialized) store.saveScroll("stats",page.scrollY)
        super.onPause()
    }
}
