package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class PopupSettingsActivity:Activity() {
    private lateinit var prefs:Prefs

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        prefs=Prefs(this)
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"popup_customize")))
        root.addView(Ui.text(this,L10n.t(this,"popup_explain"),14f))
        root.addView(Ui.button(this,L10n.t(this,"popup_preview")) { preview() })

        root.addView(Ui.section(this,L10n.t(this,"popup_position")))
        root.addView(Ui.button(this,L10n.t(this,"popup_"+prefs.popupPosition)) {
            choose("popup_position",listOf("top","middle","bottom"),prefs.popupPosition) { prefs.popupPosition=it }
        })
        root.addView(Ui.section(this,L10n.t(this,"popup_size")))
        root.addView(Ui.button(this,L10n.t(this,"popup_"+prefs.popupSize)) {
            choose("popup_size",listOf("compact","normal"),prefs.popupSize) { prefs.popupSize=it }
        })
        root.addView(Ui.section(this,L10n.t(this,"popup_color")))
        root.addView(Ui.button(this,L10n.t(this,"popup_"+prefs.popupColor)) {
            choose("popup_color",listOf("sage","peach","lavender"),prefs.popupColor) { prefs.popupColor=it }
        })
        root.addView(Ui.section(this,L10n.t(this,"popup_animation")))
        root.addView(Ui.button(this,L10n.t(this,"popup_"+prefs.popupAnimation)) {
            choose("popup_animation",listOf("spring","fade","none"),prefs.popupAnimation) { prefs.popupAnimation=it }
        })
        root.addView(Ui.section(this,L10n.t(this,"popup_opacity")))
        val pct=Ui.text(this,"${prefs.popupOpacity}%",14f)
        root.addView(pct)
        root.addView(SeekBar(this).apply {
            max=30
            progress=prefs.popupOpacity-70
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(b:SeekBar?,value:Int,fromUser:Boolean) {
                    pct.text="${value+70}%"
                }
                override fun onStartTrackingTouch(b:SeekBar?) {}
                override fun onStopTrackingTouch(b:SeekBar?) {
                    prefs.popupOpacity=(b?.progress ?:30)+70
                }
            })
        })
        root.addView(CheckBox(this).apply {
            text=L10n.t(this@PopupSettingsActivity,"popup_mascot")
            textSize=16f
            setTextColor(Appearance.palette(this@PopupSettingsActivity).text)
            isChecked=prefs.popupMascot
            setOnCheckedChangeListener { _,v -> prefs.popupMascot=v }
        })

        root.addView(Ui.section(this,L10n.t(this,"popup_messages")))
        root.addView(Ui.button(this,L10n.t(this,"popup_title")) {
            enterText("popup_title",prefs.popupTitle,56) { prefs.popupTitle=it }
        })
        root.addView(Ui.button(this,L10n.t(this,"popup_body")) {
            enterText("popup_body",prefs.popupBody,140) { prefs.popupBody=it }
        })
        root.addView(Ui.text(this,L10n.t(this,"popup_placeholder"),13f))
        root.addView(Ui.button(this,L10n.t(this,"popup_reset")) {
            AlertDialog.Builder(this).setTitle(L10n.t(this,"popup_reset"))
                .setNegativeButton(L10n.t(this,"cancel"),null)
                .setPositiveButton(L10n.t(this,"save")) { _,_ ->
                    prefs.popupPosition="bottom";prefs.popupSize="normal";prefs.popupColor="sage"
                    prefs.popupAnimation="spring";prefs.popupOpacity=100;prefs.popupMascot=true
                    prefs.popupTitle="";prefs.popupBody=""
                    recreate()
                }.show()
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun choose(key:String,options:List<String>,now:String,save:(String)->Unit) {
        val labels=options.map { L10n.t(this,"popup_"+it) }.toTypedArray()
        AlertDialog.Builder(this).setTitle(L10n.t(this,key))
            .setSingleChoiceItems(labels,options.indexOf(now).coerceAtLeast(0)) { dialog,which ->
                save(options[which]);dialog.dismiss();recreate()
            }.setNegativeButton(L10n.t(this,"cancel"),null).show()
    }

    private fun enterText(key:String,current:String,max:Int,save:(String)->Unit) {
        val input=EditText(this).apply {
            inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setSingleLine(false)
            minLines=2
            maxLines=4
            setText(current)
            hint=L10n.t(this@PopupSettingsActivity,"popup_blank")
        }
        val wrapper=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(Ui.dp(this@PopupSettingsActivity,18),0,Ui.dp(this@PopupSettingsActivity,18),0)
            addView(input)
        }
        AlertDialog.Builder(this).setTitle(L10n.t(this,key))
            .setView(wrapper)
            .setPositiveButton(L10n.t(this,"save")) { _,_ -> save(input.text.toString().take(max)) }
            .setNegativeButton(L10n.t(this,"cancel"),null).show()
    }

    private fun preview() {
        val card=OverlayController(this).previewCard(L10n.t(this,"popup_example_app"))
        val d=AlertDialog.Builder(this)
            .setView(card)
            .setPositiveButton(L10n.t(this,"popup_close"),null)
            .create()
        d.show()
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
