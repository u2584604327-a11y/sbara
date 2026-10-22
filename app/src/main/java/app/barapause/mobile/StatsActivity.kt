package app.barapause.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView

class StatsActivity : Activity() {
    private lateinit var page: ScrollView
    private lateinit var store: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        store = Prefs(this)
        store.rollDayIfNeeded()
        val root = Ui.column(this)
        root.minimumHeight = resources.displayMetrics.heightPixels
        root.addView(Ui.navHeader(this, L10n.t(this, "stats")))

        val usage=store.dailyUsageAll()
        val monitored=usage.values.sum()
        val blocks=store.blocksToday()
        val extensions=store.extensionsUsedToday()
        val active=store.rules().values.filter { it.enabled }
        val p=Appearance.palette(this)

        val metrics=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(Ui.dp(this@StatsActivity,16),Ui.dp(this@StatsActivity,16),
                Ui.dp(this@StatsActivity,16),Ui.dp(this@StatsActivity,16))
            background=Ui.cardBackground(this@StatsActivity)
        }
        metrics.addView(Ui.section(this, L10n.t(this, "stat_summary")))
        metrics.addView(Ui.title(this, "%.1f ".format(monitored/60_000.0)+L10n.t(this,"minute")))
        metrics.addView(Ui.text(this, L10n.t(this,"today_blocks")+": "+blocks+
            "   ·   "+L10n.t(this,"today_extensions")+": "+extensions+"/"+store.maxExtensionsPerDay,15f))
        root.addView(metrics)

        if(active.isEmpty()) {
            val empty=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL
                gravity=Gravity.CENTER
                setPadding(Ui.dp(this@StatsActivity,18),Ui.dp(this@StatsActivity,20),
                    Ui.dp(this@StatsActivity,18),Ui.dp(this@StatsActivity,22))
                background=Ui.cardBackground(this@StatsActivity)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply { topMargin=Ui.dp(this@StatsActivity,16) }
            }
            empty.addView(ImageView(this).apply {
                setImageResource(R.drawable.capybara_mascot)
                layoutParams=LinearLayout.LayoutParams(Ui.dp(this@StatsActivity,128),
                    Ui.dp(this@StatsActivity,128))
                contentDescription="BaraPause"
            })
            empty.addView(Ui.text(this,L10n.t(this,"none_selected")))
            empty.addView(Ui.text(this,L10n.t(this,"stat_empty_hint"),14f))
            empty.addView(Ui.button(this,L10n.t(this,"stat_choose")) {
                startActivity(Intent(this,AppPickerActivity::class.java))
            })
            root.addView(empty)
        } else {
            root.addView(Ui.section(this,L10n.t(this,"stat_apps")))
            active.sortedByDescending { usage[it.packageName] ?: 0L }.forEach { rule ->
                val used=usage[rule.packageName] ?: 0L
                val item=LinearLayout(this).apply {
                    orientation=LinearLayout.VERTICAL
                    setPadding(Ui.dp(this@StatsActivity,12),Ui.dp(this@StatsActivity,9),
                        Ui.dp(this@StatsActivity,12),Ui.dp(this@StatsActivity,11))
                    background=Ui.cardBackground(this@StatsActivity)
                }
                item.addView(Ui.text(this,rule.label+"  ·  %.1f ".format(used/60_000.0) +
                    L10n.t(this,"minute"),15f))
                item.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {
                    max=1000
                    progress=if(monitored>0) ((used.toDouble()/monitored)*1000).toInt().coerceIn(0,1000) else 0
                    progressTintList=android.content.res.ColorStateList.valueOf(p.accent)
                })
                root.addView(item,LinearLayout.LayoutParams(-1,-2).apply{
                    bottomMargin=Ui.dp(this@StatsActivity,7)
                })
            }
            root.addView(Ui.text(this,L10n.t(this,"stat_share_note"),13f))
            root.addView(Ui.button(this,L10n.t(this,"stat_choose")) {
                startActivity(Intent(this,AppPickerActivity::class.java))
            })
        }
        root.addView(Ui.text(this, L10n.t(this, "today_info"), 13f))
        page = ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(p.bg)
            addView(root)
        }
        setContentView(page)
        page.post { page.scrollTo(0,store.savedScroll("stats")) }
    }

    override fun onPause() {
        if (::page.isInitialized) store.saveScroll("stats",page.scrollY)
        super.onPause()
    }
}
