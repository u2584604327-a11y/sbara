package app.barapause.mobile

object RuleMath {
    fun effectiveLimitMs(limitMinutes: Int, studyModeActive: Boolean): Long {
        val base = limitMinutes.coerceAtLeast(1) * 60_000L
        return if (studyModeActive) (base / 2L).coerceAtLeast(60_000L) else base
    }

    fun isMinuteInsideWindow(minuteOfDay: Int, startMinute: Int, endMinute: Int): Boolean {
        val m = ((minuteOfDay % 1440) + 1440) % 1440
        val s = ((startMinute % 1440) + 1440) % 1440
        val e = ((endMinute % 1440) + 1440) % 1440
        return if (s == e) true else if (s < e) m in s until e else (m >= s || m < e)
    }
}
