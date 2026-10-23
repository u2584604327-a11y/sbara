package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import java.util.Locale

class SettingsActivity : Activity() {
    private lateinit var page: ScrollView
    private lateinit var prefs: Prefs
    private var taps=0

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        prefs=Prefs(this)
        render()
    }
    private fun tr(s:String)=L10n.t(this,s)

    private fun render() {
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,tr("settings")))
        root.addView(Ui.section(this,tr("break")))
        root.addView(Ui.button(this,tr("break")+": "+fmtDuration(prefs.effectiveBreakSeconds())) {
            chooseDuration(tr("break"),prefs.breakSeconds, 60) { if(prefs.testMode) prefs.testBreakSeconds=it else prefs.breakSeconds=it;recreate() }
        })
        root.addView(Ui.button(this,tr("extension")+": "+fmtDuration(prefs.effectiveExtensionSeconds())) {
            chooseDuration(tr("extension"),prefs.extensionSeconds, 30) { if(prefs.testMode) prefs.testExtensionSeconds=it else prefs.extensionSeconds=it;recreate() }
        })
        root.addView(Ui.button(this,tr("extensions")+": ${prefs.maxExtensionsPerDay}") {
            val items=(0..5).map { it.toString() }.toTypedArray()
            AlertDialog.Builder(this).setTitle(tr("extensions")).setItems(items) { _,id ->
                prefs.maxExtensionsPerDay=id;recreate()
            }.show()
        })

        root.addView(Ui.section(this,tr("study")))
        root.addView(CheckBox(this).apply {
            text=tr("study_desc");textSize=16f;setTextColor(Appearance.palette(this@SettingsActivity).text)
            isChecked=prefs.studyModeEnabled
            setOnCheckedChangeListener { _,checked -> prefs.studyModeEnabled=checked }
        })
        root.addView(Ui.button(this,tr("start")+": "+clock(prefs.studyStartMinute)) {
            pickTime(prefs.studyStartMinute) { prefs.studyStartMinute=it;recreate() }
        })
        root.addView(Ui.button(this,tr("end")+": "+clock(prefs.studyEndMinute)) {
            pickTime(prefs.studyEndMinute) { prefs.studyEndMinute=it;recreate() }
        })
        root.addView(Ui.text(this,tr("window"),13f))
        root.addView(Ui.section(this,tr("appearance")))
        val themes=listOf("system","light","dark","eye")
        root.addView(Ui.button(this,tr("theme")+": "+tr(prefs.theme)) {
            val names=themes.map { tr(it) }.toTypedArray()
            AlertDialog.Builder(this).setTitle(tr("theme"))
                .setSingleChoiceItems(names,themes.indexOf(prefs.theme).coerceAtLeast(0)) { dialog,id ->
                    prefs.theme=themes[id];dialog.dismiss();recreate()
                }.show()
        })
        val langs=listOf("system","zh-Hans","zh-Hant","en","ja")
        val langKeys=listOf("language_system","language_zh","language_tw","language_en","language_ja")
        root.addView(Ui.button(this,tr("language")+": "+tr(langKeys[langs.indexOf(prefs.language).coerceAtLeast(0)])) {
            AlertDialog.Builder(this).setTitle(tr("language"))
                .setSingleChoiceItems(langKeys.map { tr(it) }.toTypedArray(),langs.indexOf(prefs.language).coerceAtLeast(0)) { dialog,id ->
                    prefs.language=langs[id];WidgetUpdater.refresh(this);dialog.dismiss();recreate()
                }.show()
        })
        root.addView(Ui.button(this,tr("popup_customize")) {
            startActivity(Intent(this,PopupSettingsActivity::class.java))
        })
        root.addView(Ui.section(this,tr("advanced")))
        root.addView(Ui.button(this,tr("focus_title")) { startActivity(Intent(this,FocusActivity::class.java)) })
        root.addView(Ui.button(this,tr("backup_title")) { startActivity(Intent(this,BackupActivity::class.java)) })
        val version=Ui.button(this,tr("version")) {
            taps++
            if(taps>=7) {
                taps=0
                prefs.testMode=!prefs.testMode
                Toast.makeText(this,if(prefs.testMode) tr("test_enabled") else tr("test_disabled"),Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
        root.addView(version)
        if(prefs.testMode) root.addView(Ui.text(this,tr("test_warning"),14f))
        page=ScrollView(this).apply { isFillViewport=true;setBackgroundColor(Appearance.palette(this@SettingsActivity).bg);addView(root) }
        setContentView(page)
        page.post { page.scrollTo(0,prefs.savedScroll("settings")) }
    }

    override fun onPause() {
        if (::page.isInitialized) prefs.saveScroll("settings",page.scrollY)
        super.onPause()
    }

    private fun chooseDuration(title:String,originalSeconds:Int,maxMinutes:Int,save:(Int)->Unit) {
        val choices=mutableListOf<Pair<String,Int>>()
        if(prefs.testMode) { choices.add("15 "+tr("seconds") to 15);choices.add("30 "+tr("seconds") to 30);choices.add("1 "+tr("minute") to 60) }
        if(!prefs.testMode) listOf(1,3,5,10,15,20,30,45,60).filter { it<=maxMinutes }
            .forEach { choices.add("$it "+tr("minute") to it*60) }
        val texts=choices.map { it.first }.toMutableList()
        if(!prefs.testMode) texts.add(tr("custom"))
        AlertDialog.Builder(this).setTitle(title).setItems(texts.toTypedArray()) { _,index ->
            if(index==choices.size) {
                val input=EditText(this).apply {
                    inputType=InputType.TYPE_CLASS_NUMBER
                    setText((originalSeconds/60).coerceAtLeast(1).toString())
                    hint=tr("enter_minutes")
                }
                val wrapper=LinearLayout(this).apply {
                    setPadding(Ui.dp(this@SettingsActivity,20),0,Ui.dp(this@SettingsActivity,20),0)
                    addView(input)
                }
                val dialog=AlertDialog.Builder(this).setTitle(tr("custom"))
                    .setView(wrapper).setNegativeButton(tr("cancel"),null)
                    .setPositiveButton(tr("save"),null).create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val x=input.text.toString().toIntOrNull()
                        if(x==null || x !in 1..maxMinutes) input.error="1–$maxMinutes"
                        else { save(x*60);dialog.dismiss() }
                    }
                }
                dialog.show()
            } else save(choices[index].second)
        }.show()
    }
    private fun fmtDuration(seconds:Int):String =
        if(seconds<60) "$seconds "+tr("seconds") else "${seconds/60} "+tr("minute")
    private fun pickTime(minute:Int,save:(Int)->Unit) =
        TimePickerDialog(this,{_,h,m->save(h*60+m)},minute/60,minute%60,true).show()
    private fun clock(x:Int):String=String.format(Locale.ROOT,"%02d:%02d",x/60,x%60)
}
