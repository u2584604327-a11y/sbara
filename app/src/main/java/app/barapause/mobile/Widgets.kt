package app.barapause.mobile

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** A native Android AppWidget. OriginOS may place it under 'More widgets' rather than Atomic. */
class WideWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetUpdater.render(context, manager, ids, false)
    }
}

class CompactWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetUpdater.render(context, manager, ids, true)
    }
}

object WidgetUpdater {
    fun refresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val wide = manager.getAppWidgetIds(ComponentName(context, WideWidgetProvider::class.java))
        val compact = manager.getAppWidgetIds(ComponentName(context, CompactWidgetProvider::class.java))
        render(context, manager, wide, false)
        render(context, manager, compact, true)
    }

    fun render(context: Context, manager: AppWidgetManager, ids: IntArray, compact: Boolean) {
        val pref = Prefs(context)
        val target = pref.quickTargetLabel.ifBlank { L10n.t(context, "choose_app") }
        ids.forEach { id ->
            val views = RemoteViews(context.packageName,
                if(compact) R.layout.widget_compact else R.layout.widget_wide)
            views.setTextViewText(R.id.widget_target, target)
            views.setTextViewText(R.id.widget_title, "BaraPause")
            if(!compact) views.setTextViewText(R.id.widget_search_label, L10n.t(context,"quick_search_widget"))
            val search=Intent(context, QuickLaunchActivity::class.java).apply {
                action=QuickLaunchActivity.ACTION_WIDGET_SEARCH
                flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pick=Intent(context, QuickLaunchActivity::class.java).apply {
                action=QuickLaunchActivity.ACTION_WIDGET_PICK
                flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val open=Intent(context, QuickLaunchActivity::class.java).apply {
                action=QuickLaunchActivity.ACTION_WIDGET_OPEN
                flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags=PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            views.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(context,id*11+1,search,flags))
            if(!compact) {
                views.setOnClickPendingIntent(R.id.widget_search,PendingIntent.getActivity(context,id*11+2,search,flags))
                views.setOnClickPendingIntent(R.id.widget_choose,PendingIntent.getActivity(context,id*11+3,pick,flags))
                views.setOnClickPendingIntent(R.id.widget_open,PendingIntent.getActivity(context,id*11+4,open,flags))
            }
            manager.updateAppWidget(id,views)
        }
    }
}
