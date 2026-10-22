package app.barapause.mobile

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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MonitoringService : Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var task: ScheduledFuture<*>? = null
    private lateinit var prefs: Prefs
    private lateinit var detector: ForegroundAppDetector
    private lateinit var overlay: OverlayController
    private var lastPkg: String? = null
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
        task = executor.scheduleAtFixedRate({ tickSafely() }, 0, 1, TimeUnit.SECONDS)
    }

    private fun tickSafely() {
        runCatching { tick() }
    }

    private fun tick() {
        if (!prefs.monitoringEnabled || !PermissionUtils.hasUsageAccess(this) || !PermissionUtils.hasOverlay(this)) {
            overlay.hide()
            return
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            overlay.hide()
            lastPkg = null
            lastTickElapsed = SystemClock.elapsedRealtime()
            return
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val delta = (nowElapsed - lastTickElapsed).coerceIn(0L, 2_000L)
        lastTickElapsed = nowElapsed
        val currentPkg = detector.currentPackage()
        val rule = currentPkg?.let { prefs.rules()[it] }

        if (rule == null || !rule.enabled || currentPkg == packageName) {
            overlay.hide()
            lastPkg = currentPkg
            return
        }

        if (currentPkg == lastPkg) {
            prefs.addSessionMs(currentPkg, delta)
            prefs.addDailyUsage(currentPkg, delta)
        }
        lastPkg = currentPkg

        val nowWall = System.currentTimeMillis()
        val study = prefs.isStudyModeActive()
        var cooldownUntil = prefs.cooldownUntil(currentPkg)
        if (cooldownUntil > 0 && nowWall >= cooldownUntil) {
            prefs.resetSession(currentPkg)
            cooldownUntil = 0L
        }

        val effectiveLimit = RuleMath.effectiveLimitMs(rule.limitMinutes, study) + prefs.bonusMs(currentPkg)
        if (cooldownUntil <= 0 && prefs.sessionMs(currentPkg) >= effectiveLimit) {
            cooldownUntil = nowWall + prefs.breakMinutes * 60_000L
            prefs.setCooldownUntil(currentPkg, cooldownUntil)
            prefs.incrementBlocks()
        }

        if (cooldownUntil > nowWall) {
            val remaining = cooldownUntil - nowWall
            overlay.show(
                packageName = currentPkg,
                appLabel = rule.label,
                remainingMs = remaining,
                canExtend = prefs.canExtend(study),
                extensionMinutes = prefs.extensionMinutes
            ) {
                if (prefs.consumeExtension()) {
                    prefs.addBonusMs(currentPkg, prefs.extensionMinutes * 60_000L)
                    prefs.setCooldownUntil(currentPkg, 0L)
                    overlay.hide()
                }
            }
        } else {
            overlay.hide()
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

    private fun buildNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = android.app.Notification.Builder(this, "monitor")
        return builder
            .setSmallIcon(R.drawable.ic_stat_barapause)
            .setContentTitle("BaraPause 正在轻轻守门")
            .setContentText("只统计你选择的 App；提醒与数据都留在本机")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        task?.cancel(true)
        executor.shutdownNow()
        overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
