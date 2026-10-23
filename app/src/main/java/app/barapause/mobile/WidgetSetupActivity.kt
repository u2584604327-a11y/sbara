package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

/** Makes the launcher contract explicit: a pin request isn't a permission grant or a success result. */
class WidgetSetupActivity : Activity() {
    private lateinit var pref: Prefs
    private val widgetManager by lazy { AppWidgetManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref=Prefs(this)
        Appearance.applyWindow(this)
        render()
    }

    override fun onResume() {
        super.onResume()
        if(::pref.isInitialized) render()
    }

    private fun phrase(key:String) = L10n.t(this,key)
    private fun showMessage(text:String) =
        AlertDialog.Builder(this).setTitle(phrase("widget_setup"))
            .setMessage(text).setPositiveButton(phrase("ok"),null).show()

    private fun render() {
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,phrase("widget_setup")))
        root.addView(Ui.text(this,phrase("widget_explain"),14f))
        val supported=Build.VERSION.SDK_INT>=26 && widgetManager.isRequestPinAppWidgetSupported
        val wide=widgetManager.getAppWidgetIds(ComponentName(this,WideWidgetProvider::class.java)).size
        val compact=widgetManager.getAppWidgetIds(ComponentName(this,CompactWidgetProvider::class.java)).size
        root.addView(Ui.section(this,phrase("widget_status")))
        root.addView(Ui.text(this,phrase(if(supported)"widget_supported" else "widget_not_supported"),15f))
        root.addView(Ui.text(this,phrase("widget_added")+": 4×2=$wide  ·  2×2=$compact",14f))
        root.addView(Ui.button(this,phrase("pin_wide")) { pin(WideWidgetProvider::class.java) })
        root.addView(Ui.button(this,phrase("pin_compact")) { pin(CompactWidgetProvider::class.java) })
        root.addView(Ui.section(this,phrase("widget_manual_heading")))
        root.addView(Ui.text(this,phrase("widget_steps"),14f))
        root.addView(Ui.button(this,phrase("go_home")) {
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
        })
        root.addView(Ui.section(this,phrase("widget_alternative")))
        root.addView(Ui.text(this,phrase("shortcut_explain"),14f))
        root.addView(Ui.button(this,phrase("pin_shortcut")) { pinQuickSearchShortcut() })
        root.addView(Ui.button(this,phrase("widget_refresh")) {
            WidgetUpdater.refresh(this)
            render()
        })
        val page=ScrollView(this).apply { isFillViewport=true;setBackgroundColor(Appearance.palette(this@WidgetSetupActivity).bg);addView(root) }
        setContentView(page)
    }

    private fun pin(providerClass: Class<*>) {
        if(Build.VERSION.SDK_INT<26 || !widgetManager.isRequestPinAppWidgetSupported) {
            showMessage(phrase("widget_not_supported") + "\n\n" + phrase("widget_steps"))
            return
        }
        try {
            val provider=ComponentName(this,providerClass)
            val receiver=Intent(this,WidgetPinReceiver::class.java).apply {
                action=WidgetPinReceiver.ACTION_PIN_SUCCESS
                putExtra("provider_name",providerClass.simpleName)
            }
            val requestCode=if(providerClass==WideWidgetProvider::class.java) 211 else 212
            val callback=PendingIntent.getBroadcast(this,requestCode,receiver,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val accepted=widgetManager.requestPinAppWidget(provider,null,callback)
            if(accepted) {
                showMessage(phrase("widget_request_sent") + "\n\n" + phrase("widget_steps"))
            } else {
                showMessage(phrase("widget_not_supported") + "\n\n" + phrase("widget_steps"))
            }
        } catch(e:Exception) {
            pref.recordMonitorDiagnostic("widget_pin_error",e.javaClass.simpleName + ": " + (e.message ?: ""))
            showMessage(phrase("widget_error")+": "+e.javaClass.simpleName+
                "\n\n"+phrase("widget_steps"))
        }
    }

    private fun pinQuickSearchShortcut() {
        if(Build.VERSION.SDK_INT<26) {
            showMessage(phrase("shortcut_not_supported"))
            return
        }
        val shortcuts=getSystemService(ShortcutManager::class.java)
        if(shortcuts==null || !shortcuts.isRequestPinShortcutSupported) {
            showMessage(phrase("shortcut_not_supported") + "\n\n" + phrase("widget_steps"))
            return
        }
        val target=pref.quickTargetLabel.ifBlank { phrase("quick_search") }
        val launch=Intent(this,QuickLaunchActivity::class.java).apply {
            action=QuickLaunchActivity.ACTION_WIDGET_SEARCH
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val shortcut=ShortcutInfo.Builder(this,"barapause_search")
            .setShortLabel(phrase("quick_search").take(25))
            .setLongLabel((phrase("quick_search")+" · "+target).take(80))
            .setIcon(Icon.createWithResource(this,R.mipmap.ic_launcher))
            .setIntent(launch)
            .build()
        try {
            val requested=shortcuts.requestPinShortcut(shortcut,null)
            showMessage(phrase(if(requested)"shortcut_request_sent" else "shortcut_not_supported"))
        } catch(e:Exception) {
            showMessage(phrase("widget_error")+": "+e.javaClass.simpleName)
        }
    }
}

/** Receives a success callback only if the launcher actually pins the widget. */
class WidgetPinReceiver : android.content.BroadcastReceiver() {
    companion object { const val ACTION_PIN_SUCCESS="app.barapause.widget.PIN_SUCCESS" }
    override fun onReceive(context:android.content.Context,intent:Intent?) {
        if(intent?.action!=ACTION_PIN_SUCCESS) return
        val widgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1)
        val name=intent.getStringExtra("provider_name") ?: "?"
        val pref=Prefs(context)
        pref.recordMonitorDiagnostic("widget_pin_success","$name / id=$widgetId")
        WidgetUpdater.refresh(context)
    }
}
