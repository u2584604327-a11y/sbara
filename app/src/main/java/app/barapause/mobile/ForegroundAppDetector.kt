package app.barapause.mobile

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Incrementally tracks the last resumed foreground package.
 *
 * The original V1 queried only a short fixed lookback window. That fails when an app stays
 * foreground longer than the window because there may be no new resume event. This version
 * keeps the last known package and advances the query cursor on every poll.
 */
class ForegroundAppDetector(context: Context) {
    private val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private var cursorWallMs = System.currentTimeMillis() - 60_000L
    private var currentPkg: String? = null

    @Synchronized
    fun currentPackage(now: Long = System.currentTimeMillis()): String? {
        val start = cursorWallMs.coerceAtMost(now)
        val events = usage.queryEvents(start, now) ?: return currentPkg
        val event = UsageEvents.Event()
        var newestTimestamp = start

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.timeStamp < newestTimestamp) continue
            newestTimestamp = event.timeStamp
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> currentPkg = event.packageName

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (currentPkg == event.packageName) currentPkg = null
                }
            }
        }

        cursorWallMs = (now - 250L).coerceAtLeast(start)
        return currentPkg
    }
}
