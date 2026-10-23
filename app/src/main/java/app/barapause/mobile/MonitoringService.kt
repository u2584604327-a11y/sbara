package app.barapause.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.Handler
import android.os.Looper
import android.util.Log


class MonitoringService : Service() {
    companion object {
        const val ACTION_TEST_POPUP = "app.barapause.mobile.ACTION_TEST_POPUP"
        const val ACTION_PROFILE_CHANGED = "app.barapause.mobile.ACTION_PROFILE_CHANGED"
    }
    private var previewUntilElapsed = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            try { tick() } catch (e: Exception) {
                Log.e("BaraPause", "Monitor tick error", e)
                if (::prefs.isInitialized) prefs.recordMonitorDiagnostic("error", e.javaClass.simpleName + ": " + (e.message ?: ""))
            }
            handler.postDelayed(this, 1000L)
        }
    }
    private lateinit var prefs: Prefs
    private lateinit var detector: ForegroundAppDetector
    private lateinit var overlay: OverlayController
    private var lastPkg: String? = null
    private var lastNotificationAt=0L
    private var lastNotificationText=""
    private var lastTickElapsed = 0L

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        detector = ForegroundAppDetector(this)
        overlay = OverlayController(this)
        createChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1001, notification)
        }
        lastTickElapsed = SystemClock.elapsedRealtime()
        prefs.recordMonitorDiagnostic("last_start", System.currentTimeMillis().toString())
        handler.post(ticker)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!prefs.monitoringEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_PROFILE_CHANGED) {
            overlay.hide()
            lastPkg=null
            lastTickElapsed=SystemClock.elapsedRealtime()
            prefs.recordMonitorDiagnostic("profile_change",System.currentTimeMillis().toString())
            refreshNotification(L10n.t(this,"not_monitoring"))
        }
        if (intent?.action == ACTION_TEST_POPUP) {
            if (PermissionUtils.hasOverlay(this)) {
                previewUntilElapsed = SystemClock.elapsedRealtime() + 10_000L
                overlay.show("barapause-preview", "BaraPause", 10_000L, false, 60) {}
                prefs.recordMonitorDiagnostic("preview", "overlay_added")
            } else {
                prefs.recordMonitorDiagnostic("preview", "overlay_permission_missing")
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        prefs.recordMonitorDiagnostic("task_removed", System.currentTimeMillis().toString())
        super.onTaskRemoved(rootIntent)
    }

    private fun tick() {
        if (!prefs.monitoringEnabled || !PermissionUtils.hasUsageAccess(this) || !PermissionUtils.hasOverlay(this)) {
            lastPkg=null
            lastTickElapsed=SystemClock.elapsedRealtime()
            overlay.hide()
            prefs.recordMonitorDiagnostic("reason", "permission_or_switch_disabled")
            return
        }
        prefs.recordMonitorDiagnostic("heartbeat", System.currentTimeMillis().toString())
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            overlay.hide()
            lastPkg = null
            lastTickElapsed = SystemClock.elapsedRealtime()
            prefs.recordMonitorDiagnostic("reason", "screen_off")
            refreshNotification(L10n.t(this,"not_monitoring"))
            return
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        if (previewUntilElapsed > nowElapsed) {
            overlay.update(previewUntilElapsed - nowElapsed, false, 60)
            lastTickElapsed = nowElapsed
            prefs.recordMonitorDiagnostic("reason", "preview_popup")
            return
        } else if (previewUntilElapsed != 0L) {
            previewUntilElapsed = 0L
            overlay.hide()
        }
        val delta = (nowElapsed - lastTickElapsed).coerceIn(0L, 2_000L)
        lastTickElapsed = nowElapsed
        val currentPkg = detector.currentPackage()
        prefs.recordMonitorDiagnostic("foreground", currentPkg ?: "(unknown)")
        val rule = currentPkg?.let { prefs.rules()[it] }

        if (rule == null || !rule.enabled || currentPkg == packageName) {
            overlay.hide()
            lastPkg = currentPkg
            prefs.recordMonitorDiagnostic("reason", if(currentPkg == null) "foreground_unknown" else "not_limited")
            refreshNotification(L10n.t(this,"not_monitoring"))
            return
        }

        if (currentPkg == lastPkg) {
            prefs.addSessionMs(currentPkg, delta)
            prefs.addDailyUsage(currentPkg, delta)
        }
        lastPkg = currentPkg
        prefs.recordMonitorDiagnostic("seconds", (prefs.sessionMs(currentPkg) / 1000L).toString())

        val nowWall = System.currentTimeMillis()
        val study = prefs.isStudyModeActive()
        var cooldownUntil = prefs.cooldownUntil(currentPkg)
        if (cooldownUntil > 0 && nowWall >= cooldownUntil) {
            prefs.resetSession(currentPkg)
            cooldownUntil = 0L
        }

        val effectiveLimit = ((prefs.ruleLimitSeconds(currentPkg, rule.limitSeconds) * 1000L) / if (study) 2L else 1L).coerceAtLeast(1000L) + prefs.bonusMs(currentPkg)
        if (cooldownUntil <= 0 && prefs.sessionMs(currentPkg) >= effectiveLimit) {
            cooldownUntil = nowWall + prefs.effectiveBreakSeconds() * 1000L
            prefs.setCooldownUntil(currentPkg, cooldownUntil)
            prefs.incrementBlocks()
            prefs.recordMonitorDiagnostic("last_trigger", currentPkg + "@" + nowWall)
        }

        if (cooldownUntil > nowWall) {
            prefs.recordMonitorDiagnostic("reason", "break_active")
            refreshNotification(rule.label + " · " + L10n.t(this,"break_remaining") +
                " " + duration(cooldownUntil - nowWall))
            val remaining = cooldownUntil - nowWall
            overlay.show(
                packageName = currentPkg,
                appLabel = rule.label,
                remainingMs = remaining,
                canExtend = prefs.canExtend(study),
                extensionSeconds = prefs.effectiveExtensionSeconds()
            ) {
                if (prefs.consumeExtension()) {
                    prefs.addBonusMs(currentPkg, prefs.effectiveExtensionSeconds() * 1000L)
                    prefs.setCooldownUntil(currentPkg, 0L)
                    overlay.hide()
                }
            }
        } else {
            prefs.recordMonitorDiagnostic("reason", "counting")
            refreshNotification(rule.label + " · " + L10n.t(this,"remaining") +
                " " + duration(effectiveLimit - prefs.sessionMs(currentPkg)))
            overlay.hide()
        }
    }

    private fun duration(ms: Long): String {
        val sec=(ms.coerceAtLeast(0L)+999L)/1000L
        return "%02d:%02d".format(java.util.Locale.ROOT,sec/60,sec%60)
    }

    /**
     * Refresh no more than once per 15 seconds; don't attempt to bypass notification
     * permission/channel opt-outs. Android may still allow FGS notifications to be dismissed.
     */
    private fun refreshNotification(content: String) {
        val now=SystemClock.elapsedRealtime()
        if(now-lastNotificationAt<15_000L && content.substringBefore(" · ")==lastNotificationText.substringBefore(" · ")) return
        lastNotificationAt=now
        lastNotificationText=content
        if(Build.VERSION.SDK_INT>=33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            prefs.recordMonitorDiagnostic("notification","permission_denied")
            return
        }
        try {
            val nm=getSystemService(NotificationManager::class.java)
            if(Build.VERSION.SDK_INT>=26 && !nm.areNotificationsEnabled()) {
                prefs.recordMonitorDiagnostic("notification","disabled")
                return
            }
            nm.notify(1001,buildNotification(content))
            prefs.recordMonitorDiagnostic("notification",content)
        } catch(e:Exception) {
            prefs.recordMonitorDiagnostic("notification",e.javaClass.simpleName)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("monitor", "BaraPause monitoring", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Keeps user-enabled app limit monitoring active"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String = L10n.t(this,"not_monitoring")): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = android.app.Notification.Builder(this, "monitor")
        return builder
            .setSmallIcon(R.drawable.ic_stat_barapause)
            .setContentTitle(L10n.t(this,"service_title"))
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .setCategory(android.app.Notification.CATEGORY_STATUS)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        if (::prefs.isInitialized) prefs.recordMonitorDiagnostic("last_destroy", System.currentTimeMillis().toString())
        overlay.hide()
        previewUntilElapsed = 0L
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
