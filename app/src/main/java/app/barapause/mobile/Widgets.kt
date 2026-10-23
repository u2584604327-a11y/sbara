package app.barapause.mobile

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

/**
 * RemoteViews-compatible foldable card. No overlay, broad permission or periodic poll needed.
 * Expansion is stored per widget ID, and never silently opens a previously saved target.
 */
class WideWidgetProvider : AppWidgetProvider() {
    companion object { const val ACTION_TOGGLE = "app.barapause.mobile.action.WIDGET_TOGGLE" }

    override fun onUpdate(context:Context, manager:AppWidgetManager, ids:IntArray) {
        WidgetUpdater.render(context,manager,ids,false)
    }
    override fun onReceive(context:Context, intent:Intent) {
        if(intent.action==ACTION_TOGGLE) {
            val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
            val manager=AppWidgetManager.getInstance(context)
            val provider=ComponentName(context,WideWidgetProvider::class.java)
            if(id!=AppWidgetManager.INVALID_APPWIDGET_ID && manager.getAppWidgetIds(provider).contains(id)) {
                val state=context.getSharedPreferences("widget_panels",Context.MODE_PRIVATE)
                val key="expand_$id"
                state.edit().putBoolean(key,!state.getBoolean(key,false)).apply()
                WidgetUpdater.render(context,manager,intArrayOf(id),false)
            }
            return
        }
        super.onReceive(context,intent)
    }
    override fun onDeleted(context:Context, ids:IntArray) {
        val editor=context.getSharedPreferences("widget_panels",Context.MODE_PRIVATE).edit()
        ids.forEach { editor.remove("expand_$it") }
        editor.apply()
        super.onDeleted(context,ids)
    }
}
class CompactWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context:Context, manager:AppWidgetManager, ids:IntArray) {
        WidgetUpdater.render(context,manager,ids,true)
    }
}
object WidgetUpdater {
    fun refresh(context:Context) {
        val manager=AppWidgetManager.getInstance(context)
        val wide=manager.getAppWidgetIds(ComponentName(context,WideWidgetProvider::class.java))
        val compact=manager.getAppWidgetIds(ComponentName(context,CompactWidgetProvider::class.java))
        render(context,manager,wide,false)
        render(context,manager,compact,true)
    }

    fun render(context:Context, manager:AppWidgetManager, ids:IntArray, compact:Boolean) {
        val prefs=Prefs(context)
        val label=prefs.quickTargetLabel.takeIf { prefs.quickTargetPackage.isNotBlank() && it.isNotBlank() }
        val target=label ?: "先选择 App"
        val saved=context.getSharedPreferences("widget_panels",Context.MODE_PRIVATE)
        val last=prefs.recentSearches().firstOrNull()?.take(18)
        val flags=PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        fun activity(id:Int,offset:Int,action:String):PendingIntent {
            val intent=Intent(context,QuickLaunchActivity::class.java).apply {
                this.action=action
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                data=android.net.Uri.parse("barapause-widget://open/$id/$offset")
            }
            return PendingIntent.getActivity(context,id*17+offset,intent,flags)
        }
        for(id in ids) {
            val views=RemoteViews(context.packageName,
                if(compact) R.layout.widget_compact else R.layout.widget_wide)
            views.setTextViewText(R.id.widget_title,"BaraPause")
            views.setTextViewText(R.id.widget_target,target)
            views.setOnClickPendingIntent(R.id.widget_root,
                activity(id,1,QuickLaunchActivity.ACTION_WIDGET_PICK))
            if(!compact) {
                val expanded=saved.getBoolean("expand_$id",false)
                views.setViewVisibility(R.id.widget_actions,if(expanded) View.VISIBLE else View.GONE)
                views.setTextViewText(R.id.widget_expand,if(expanded) "⌃" else "⌄")
                views.setTextViewText(R.id.widget_search_label,
                    if(last.isNullOrBlank()) "选择 App · 输入关键词" else "最近：$last")
                views.setOnClickPendingIntent(R.id.widget_search,activity(id,2,QuickLaunchActivity.ACTION_WIDGET_SEARCH))
                views.setOnClickPendingIntent(R.id.widget_choose,activity(id,3,QuickLaunchActivity.ACTION_WIDGET_PICK))
                // OPEN is an explicit action; only this control can use the remembered target.
                views.setOnClickPendingIntent(R.id.widget_open,activity(id,4,QuickLaunchActivity.ACTION_WIDGET_OPEN))
                views.setOnClickPendingIntent(R.id.widget_recent,activity(id,5,QuickLaunchActivity.ACTION_WIDGET_RECENT))
                val toggle=Intent(context,WideWidgetProvider::class.java).apply {
                    action=WideWidgetProvider.ACTION_TOGGLE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id)
                    data=android.net.Uri.parse("barapause-widget://toggle/$id")
                }
                views.setOnClickPendingIntent(R.id.widget_expand,
                    PendingIntent.getBroadcast(context,id*17+6,toggle,flags))
                // Avoid a root pending intent swallowing the dedicated fold control.
            }
            manager.updateAppWidget(id,views)
        }
    }
}
