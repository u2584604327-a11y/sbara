package app.barapause.mobile

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Reconstructs the last resumed activity from a 24h event history on initial startup,
 * then advances through UsageEvents incrementally. A fixed 60-second lookback can fail
 * when a target app has already been open for longer than that before monitoring starts.
 */
class ForegroundAppDetector(context: Context) {
    private val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private var cursor = 0L
    private var lastTime = Long.MIN_VALUE
    private var lastEventIds = mutableSetOf<String>()
    private var foreground: String? = null
    private var foregroundActivity: String? = null

    @Synchronized fun currentPackage(now: Long = System.currentTimeMillis()): String? {
        if (cursor == 0L || now + 10_000L < cursor) {
            cursor = (now - 86_400_000L).coerceAtLeast(0L)
            lastTime = Long.MIN_VALUE
            lastEventIds.clear()
            foreground = null
            foregroundActivity = null
        }
        val events = usage.queryEvents((cursor - 2_000L).coerceAtLeast(0L), now)
            ?: return foreground
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.timeStamp < lastTime) continue
            if (event.timeStamp > lastTime) {
                lastTime = event.timeStamp
                lastEventIds.clear()
            }
            val id = "${event.eventType}:${event.packageName}:${event.className ?: ""}"
            if (!lastEventIds.add(id)) continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    foreground = event.packageName
                    foregroundActivity = event.className
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (foreground == event.packageName &&
                        (foregroundActivity == null || foregroundActivity == event.className)) {
                        foreground = null
                        foregroundActivity = null
                    }
                }
            }
        }
        cursor = now
        return foreground
    }
}
