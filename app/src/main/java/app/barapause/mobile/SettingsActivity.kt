package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ScrollView
import java.util.Locale

class SettingsActivity : Activity() {
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        val root = Ui.column(this)
        root.addView(Ui.title(this, "规则设置"))

        root.addView(Ui.button(this, "强制休息：${prefs.breakMinutes} 分钟") { chooseInt("强制休息时长", intArrayOf(1,3,5,10,15), prefs.breakMinutes) { prefs.breakMinutes = it; recreate() } })
        root.addView(Ui.button(this, "单次延期：${prefs.extensionMinutes} 分钟") { chooseInt("延期时长", intArrayOf(1,3,5,10), prefs.extensionMinutes) { prefs.extensionMinutes = it; recreate() } })
        root.addView(Ui.button(this, "每日最多延期：${prefs.maxExtensionsPerDay} 次") { chooseInt("每日延期次数", intArrayOf(0,1,2,3), prefs.maxExtensionsPerDay) { prefs.maxExtensionsPerDay = it; recreate() } })

        val study = CheckBox(this).apply {
            text = "启用学习模式（受限 App 上限减半，并禁用延期）"
            textSize = 17f
            isChecked = prefs.studyModeEnabled
            setOnCheckedChangeListener { _, checked -> prefs.studyModeEnabled = checked }
        }
        root.addView(study)
        root.addView(Ui.button(this, "学习开始：${fmt(prefs.studyStartMinute)}") { pickTime(prefs.studyStartMinute) { prefs.studyStartMinute = it; recreate() } })
        root.addView(Ui.button(this, "学习结束：${fmt(prefs.studyEndMinute)}") { pickTime(prefs.studyEndMinute) { prefs.studyEndMinute = it; recreate() } })
        root.addView(Ui.text(this, "跨午夜时段也支持，例如 22:00 → 01:00。开始时间与结束时间相同表示全天学习模式。", 14f))

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun chooseInt(title: String, values: IntArray, current: Int, save: (Int) -> Unit) {
        val labels = values.map { it.toString() }.toTypedArray()
        AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(labels, values.indexOf(current).coerceAtLeast(0)) { d, which ->
            save(values[which]); d.dismiss()
        }.show()
    }

    private fun pickTime(current: Int, save: (Int) -> Unit) {
        TimePickerDialog(this, { _, h, m -> save(h * 60 + m) }, current / 60, current % 60, true).show()
    }

    private fun fmt(minute: Int): String = String.format(Locale.ROOT, "%02d:%02d", minute / 60, minute % 60)
}
