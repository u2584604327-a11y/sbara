package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** A local, time-limited focus override that uses existing study-mode restrictions. */
class FocusActivity : Activity() {
    private lateinit var prefs:Prefs
    private lateinit var status:TextView
    private val handler=Handler(Looper.getMainLooper())
    private val refresh=object:Runnable {
        override fun run() {
            if(::status.isInitialized) updateStatus()
            handler.postDelayed(this,1000L)
        }
    }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        prefs=Prefs(this)
        Appearance.applyWindow(this)
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"focus_title")))
        root.addView(Ui.text(this,L10n.t(this,"focus_explain"),14f))
        status=Ui.title(this,"")
        root.addView(status)
        listOf(25,50,90).forEach { minutes ->
            root.addView(Ui.button(this,"$minutes "+L10n.t(this,"minute")+" · "+L10n.t(this,"focus_start")) {
                confirmStart(minutes)
            })
        }
        root.addView(Ui.button(this,L10n.t(this,"focus_custom")) { customDuration() })
        root.addView(Ui.button(this,L10n.t(this,"focus_stop")) {
            if(prefs.isFocusActive()) {
                AlertDialog.Builder(this).setTitle(L10n.t(this,"focus_stop"))
                    .setMessage(L10n.t(this,"focus_stop_confirm"))
                    .setNegativeButton(L10n.t(this,"cancel"),null)
                    .setPositiveButton(L10n.t(this,"save")) { _,_ ->
                        prefs.stopFocus();updateStatus()
                    }.show()
            }
        })
        root.addView(Ui.section(this,L10n.t(this,"focus_priority")))
        root.addView(Ui.text(this,L10n.t(this,"focus_priority_explain"),14f))
        setContentView(ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(Appearance.palette(this@FocusActivity).bg)
            addView(root)
        })
        updateStatus()
    }
    override fun onResume() { super.onResume(); handler.post(refresh) }
    override fun onPause() { handler.removeCallbacks(refresh);super.onPause() }
    private fun updateStatus() {
        val s=(prefs.focusRemainingMillis()+999L)/1000L
        status.text=if(s>0L) "%02d:%02d:%02d".format(java.util.Locale.ROOT,s/3600,(s/60)%60,s%60)
            else L10n.t(this,"focus_inactive")
    }
    private fun confirmStart(minutes:Int) {
        AlertDialog.Builder(this).setTitle(L10n.t(this,"focus_start"))
            .setMessage("$minutes "+L10n.t(this,"minute")+"\n"+L10n.t(this,"focus_confirm"))
            .setNegativeButton(L10n.t(this,"cancel"),null)
            .setPositiveButton(L10n.t(this,"save")) { _,_ -> prefs.startFocus(minutes);updateStatus() }
            .show()
    }
    private fun customDuration() {
        val input=EditText(this).apply {
            inputType=InputType.TYPE_CLASS_NUMBER
            hint="5–180"
            setText("25")
        }
        val wrapper=LinearLayout(this).apply {
            setPadding(Ui.dp(this@FocusActivity,20),0,Ui.dp(this@FocusActivity,20),0)
            addView(input)
        }
        val dialog=AlertDialog.Builder(this).setTitle(L10n.t(this,"focus_custom"))
            .setView(wrapper)
            .setNegativeButton(L10n.t(this,"cancel"),null)
            .setPositiveButton(L10n.t(this,"save"),null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n=input.text.toString().toIntOrNull()
                if(n==null || n !in 5..180) input.error="5–180"
                else { dialog.dismiss();confirmStart(n) }
            }
        }
        dialog.show()
    }
}
