package app.barapause.mobile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch

class MainActivity : Activity() {
    private lateinit var prefs: Prefs
    private lateinit var statusText: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::statusText.isInitialized) refreshStatus()
    }

    private fun render() {
        val root = Ui.column(this)
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = Ui.cardBackground(this@MainActivity)
        }
        hero.addView(ImageView(this).apply {
            setImageResource(R.drawable.capybara_mascot)
            layoutParams = LinearLayout.LayoutParams(dp(92), dp(92))
        })
        hero.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, 0, 0)
            addView(Ui.title(this@MainActivity, "BaraPause"))
            addView(Ui.text(this@MainActivity, "可爱地打断无意识刷手机，而不是把手机锁死。", 14.5f))
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(hero)

        statusText = Ui.text(this, "", 14.5f)
        root.addView(statusText)

        root.addView(Ui.section(this, "必要权限"))
        root.addView(Ui.text(this,
            "为了判断你正在使用哪个 App，需要“使用情况访问权限”；为了在其他 App 上方显示底部提醒卡片，需要“显示在其他 App 上层”。BaraPause 不使用无障碍服务。",
            13.5f
        ))
        root.addView(Ui.button(this, "① 授予使用情况访问权限") {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        root.addView(Ui.button(this, "② 授予弹窗显示权限") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        if (Build.VERSION.SDK_INT >= 33) {
            root.addView(Ui.button(this, "③ 允许常驻监控通知（可选显示）") {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
            })
        }

        root.addView(Ui.section(this, "守门"))
        val monitorSwitch = Switch(this).apply {
            text = "启用 BaraPause"
            textSize = 18f
            isChecked = prefs.monitoringEnabled
            setPadding(0, dp(18), 0, dp(8))
            setOnCheckedChangeListener { _, checked ->
                if (checked && (!PermissionUtils.hasUsageAccess(this@MainActivity) || !PermissionUtils.hasOverlay(this@MainActivity))) {
                    isChecked = false
                    prefs.monitoringEnabled = false
                    refreshStatus("先完成前两项系统授权。")
                    return@setOnCheckedChangeListener
                }
                prefs.monitoringEnabled = checked
                val i = Intent(this@MainActivity, MonitoringService::class.java)
                if (checked) {
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
                } else {
                    stopService(i)
                }
                refreshStatus()
            }
        }
        root.addView(monitorSwitch)
        root.addView(Ui.button(this, "选择受限 App / 设置本轮上限") { startActivity(Intent(this, AppPickerActivity::class.java)) })
        root.addView(Ui.button(this, "休息、延期与学习模式") { startActivity(Intent(this, SettingsActivity::class.java)) })
        root.addView(Ui.button(this, "查看今日统计") { startActivity(Intent(this, StatsActivity::class.java)) })
        root.addView(Ui.button(this, "打开系统电池优化设置") { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) })

        root.addView(Ui.section(this, "隐私边界"))
        root.addView(Ui.text(this, "不使用 AccessibilityService；不读取通知、键盘输入或屏幕内容；不联网、不建账号、不上传统计。", 13.5f))
        setContentView(ScrollView(this).apply { addView(root) })
        refreshStatus()
    }

    private fun refreshStatus(extra: String? = null) {
        prefs.rollDayIfNeeded()
        val rules = prefs.rules().values.count { it.enabled }
        val usage = if (PermissionUtils.hasUsageAccess(this)) "✓" else "✗"
        val overlay = if (PermissionUtils.hasOverlay(this)) "✓" else "✗"
        val monitor = if (prefs.monitoringEnabled) "运行中" else "未启用"
        statusText.text = "使用情况 $usage   弹窗权限 $overlay\n受限 App：$rules   监控：$monitor   今日提醒：${prefs.blocksToday()}" +
            (extra?.let { "\n$it" } ?: "")
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
