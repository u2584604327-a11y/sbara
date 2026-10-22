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
        Appearance.applyWindow(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::statusText.isInitialized) { Appearance.applyWindow(this); render() }
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
            addView(Ui.text(this@MainActivity, L10n.t(this@MainActivity,"home_desc"), 14.5f))
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(hero)

        statusText = Ui.text(this, "", 14.5f)
        root.addView(statusText)

        root.addView(Ui.section(this, L10n.t(this,"permissions")))
        root.addView(Ui.text(this,
            L10n.t(this,"permission_detail"),
            13.5f
        ))
        root.addView(Ui.button(this, "① "+L10n.t(this,"usage_permission")) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        })
        root.addView(Ui.button(this, "② "+L10n.t(this,"overlay_permission")) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        if (Build.VERSION.SDK_INT >= 33) {
            root.addView(Ui.button(this, "③ "+L10n.t(this,"notification_permission")) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
            })
        }

        root.addView(Ui.section(this, L10n.t(this,"status")))
        val monitorSwitch = Switch(this).apply {
            text = L10n.t(this@MainActivity,"monitor")
            textSize = 18f
            isChecked = prefs.monitoringEnabled
            setPadding(0, dp(18), 0, dp(8))
            setOnCheckedChangeListener { _, checked ->
                if (checked && (!PermissionUtils.hasUsageAccess(this@MainActivity) || !PermissionUtils.hasOverlay(this@MainActivity))) {
                    isChecked = false
                    prefs.monitoringEnabled = false
                    refreshStatus(L10n.t(this@MainActivity,"authorize_first"))
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
        root.addView(Ui.button(this, L10n.t(this,"selected_apps")) { startActivity(Intent(this, AppPickerActivity::class.java)) })
        root.addView(Ui.button(this, L10n.t(this,"settings")) { startActivity(Intent(this, SettingsActivity::class.java)) })
        root.addView(Ui.button(this, L10n.t(this,"stats")) { startActivity(Intent(this, StatsActivity::class.java)) })
        root.addView(Ui.button(this, L10n.t(this,"popup_customize")) { startActivity(Intent(this, PopupSettingsActivity::class.java)) })
        root.addView(Ui.button(this, L10n.t(this,"battery")) { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) })

        root.addView(Ui.section(this, L10n.t(this,"privacy")))
        root.addView(Ui.text(this, L10n.t(this,"privacy_detail"), 13.5f))
        setContentView(ScrollView(this).apply { addView(root) })
        refreshStatus()
    }

    private fun refreshStatus(extra: String? = null) {
        prefs.rollDayIfNeeded()
        val rules = prefs.rules().values.count { it.enabled }
        val usage = if (PermissionUtils.hasUsageAccess(this)) "✓" else "✗"
        val overlay = if (PermissionUtils.hasOverlay(this)) "✓" else "✗"
        val monitor = if (prefs.monitoringEnabled) L10n.t(this,"monitor_on") else L10n.t(this,"monitor_off")
        statusText.text = L10n.t(this,"permissions")+": $usage / $overlay  ·  $monitor\n"+L10n.t(this,"summary_apps")+": $rules   "+L10n.t(this,"summary_blocks")+": ${prefs.blocksToday()}" +
            (extra?.let { "\n$it" } ?: "")
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
