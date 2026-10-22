package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

class AppPickerActivity : Activity() {
    private lateinit var prefs: Prefs
    private val bg = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        val root = Ui.column(this)
        root.addView(Ui.title(this, "受限 App"))
        root.addView(Ui.text(this, "勾选需要提醒的 App。分钟数代表本轮累计使用上限；学习模式开启时自动减半。"))
        val progress = ProgressBar(this)
        root.addView(progress)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)
        setContentView(ScrollView(this).apply { addView(root) })

        bg.execute {
            val launcher = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val apps = packageManager.queryIntentActivities(launcher, 0)
                .filter { it.activityInfo.packageName != packageName }
                .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
                .distinctBy { it.first }
                .sortedBy { it.second.lowercase() }
            runOnUiThread {
                progress.visibility = View.GONE
                apps.forEach { (pkg, label) -> addRow(list, pkg, label) }
            }
        }
    }

    private fun addRow(parent: LinearLayout, pkg: String, label: String) {
        val current = prefs.rules()[pkg]
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val check = CheckBox(this).apply {
            text = label
            isChecked = current?.enabled == true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val minutes = TextView(this).apply {
            text = "${current?.limitMinutes ?: 20} 分钟"
            textSize = 15f
            setPadding(18, 18, 18, 18)
            setOnClickListener {
                val values = intArrayOf(5, 10, 15, 20, 30, 45, 60)
                val labels = values.map { "$it 分钟" }.toTypedArray()
                AlertDialog.Builder(this@AppPickerActivity)
                    .setTitle("$label · 本轮上限")
                    .setItems(labels) { _, which ->
                        text = labels[which]
                        if (check.isChecked) prefs.putRule(AppRule(pkg, label, values[which], true))
                    }.show()
            }
        }
        check.setOnCheckedChangeListener { _, enabled ->
            if (enabled) {
                val limit = minutes.text.toString().substringBefore(" ").toIntOrNull() ?: 20
                prefs.putRule(AppRule(pkg, label, limit, true))
            } else {
                prefs.removeRule(pkg)
            }
        }
        row.addView(check)
        row.addView(minutes)
        parent.addView(row)
    }

    override fun onDestroy() {
        bg.shutdownNow()
        super.onDestroy()
    }
}
