package app.barapause.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = Prefs(context)
        if (!prefs.monitoringEnabled) return
        if (!PermissionUtils.hasUsageAccess(context) || !PermissionUtils.hasOverlay(context)) return
        val service = Intent(context, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service) else context.startService(service)
    }
}
