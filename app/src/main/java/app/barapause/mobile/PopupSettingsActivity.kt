package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.text.InputType
import android.view.Gravity
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class PopupSettingsActivity:Activity() {
    private lateinit var page: ScrollView
    private val PICK_IMAGE = 214

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

        root.addView(Ui.section(this,L10n.t(this,"popup_photo")))
        root.addView(Ui.button(this,L10n.t(this,"choose_photo")) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }, PICK_IMAGE)
        })
        root.addView(Ui.button(this,L10n.t(this,"remove_photo")) {
            prefs.popupImageUri=""
            recreate()
        })
        root.addView(Ui.text(this,L10n.t(this,"photo_hint"),13f))
        root.addView(Ui.section(this,L10n.t(this,"popup_motion")))
        root.addView(Ui.button(this,L10n.t(this,"motion_"+prefs.popupMotion)) {
            val choices=listOf("peek","wave","bob","breathe","still")
            AlertDialog.Builder(this).setTitle(L10n.t(this,"popup_motion"))
                .setSingleChoiceItems(choices.map { L10n.t(this,"motion_"+it) }.toTypedArray(),
                    choices.indexOf(prefs.popupMotion).coerceAtLeast(0)) { dialog,n ->
                    prefs.popupMotion=choices[n];dialog.dismiss();recreate()
                }.show()
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
                    prefs.popupTitle="";prefs.popupBody="";prefs.popupImageUri="";prefs.popupMotion="peek"
                    recreate()
                }.show()
        })

        page=ScrollView(this).apply { isFillViewport=true;setBackgroundColor(Appearance.palette(this@PopupSettingsActivity).bg);addView(root) }
        setContentView(page)
        page.post { page.scrollTo(0,prefs.savedScroll("popup")) }
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=PICK_IMAGE || resultCode!=RESULT_OK) return
        val uri=data?.data ?: return
        try {
            // Keep read-only access after a reboot. Never copy private photos to cloud/storage.
            contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val size=contentResolver.query(uri,arrayOf(android.provider.OpenableColumns.SIZE),null,null,null)?.use { c ->
                if(c.moveToFirst() && !c.isNull(0)) c.getLong(0) else -1L
            } ?: -1L
            if(size>16L*1024*1024) {
                Toast.makeText(this,"Image too large (16 MB max)",Toast.LENGTH_LONG).show()
                return
            }
            prefs.popupImageUri=uri.toString()
            recreate()
        } catch(e:Exception) {
            Toast.makeText(this,e.javaClass.simpleName,Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        if (::page.isInitialized) prefs.saveScroll("popup",page.scrollY)
        super.onPause()
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
        val renderer=OverlayController(this)
        val card=renderer.previewCard(L10n.t(this,"popup_example_app"))
        val d=AlertDialog.Builder(this)
            .setView(card)
            .setPositiveButton(L10n.t(this,"popup_close"),null)
            .create()
        d.setOnDismissListener { renderer.hide() }
        d.show()
        d.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
