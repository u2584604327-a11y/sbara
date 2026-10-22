package app.barapause.mobile

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView

class StatsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        prefs.rollDayIfNeeded()
        val root = Ui.column(this)
        root.addView(Ui.title(this, "今日统计"))
        root.addView(Ui.text(this, "今日拦截 ${prefs.blocksToday()} 次；已使用延期 ${prefs.extensionsUsedToday()} / ${prefs.maxExtensionsPerDay} 次。"))
        val rules = prefs.rules()
        val usage = prefs.dailyUsageAll()
        if (rules.isEmpty()) {
            root.addView(Ui.text(this, "还没有选择受限 App。"))
        } else {
            rules.values.sortedByDescending { usage[it.packageName] ?: 0L }.forEach { rule ->
                val ms = usage[rule.packageName] ?: 0L
                val min = ms / 60_000.0
                root.addView(Ui.text(this, "${rule.label}  ·  %.1f 分钟  ·  单次上限 ${rule.limitMinutes} 分钟".format(min)))
            }
        }
        root.addView(Ui.text(this, "说明：V1 的统计是 BaraPause 自己在监控运行期间累计的数据，不等同于 Android 系统“数字健康”的整日统计。", 14f))
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
