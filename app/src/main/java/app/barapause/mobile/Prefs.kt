package app.barapause.mobile

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate
import java.util.Calendar

class Prefs(context: Context) {
    private val p = context.getSharedPreferences("barapause", Context.MODE_PRIVATE)

    var monitoringEnabled: Boolean
        get() = p.getBoolean("monitoring_enabled", false)
        set(v) = p.edit().putBoolean("monitoring_enabled", v).apply()

    var breakMinutes: Int
        get() = (breakSeconds / 60).coerceAtLeast(1)
        set(v) { breakSeconds = v.coerceIn(1, 60) * 60 }

    var extensionMinutes: Int
        get() = (extensionSeconds / 60).coerceAtLeast(1)
        set(v) { extensionSeconds = v.coerceIn(1, 30) * 60 }

    var maxExtensionsPerDay: Int
        get() = p.getInt("max_extensions", 1)
        set(v) = p.edit().putInt("max_extensions", v.coerceIn(0, 10)).apply()

    var studyModeEnabled: Boolean
        get() = p.getBoolean("study_enabled", true)
        set(v) = p.edit().putBoolean("study_enabled", v).apply()

    var studyStartMinute: Int
        get() = p.getInt("study_start", 19 * 60)
        set(v) = p.edit().putInt("study_start", v.coerceIn(0, 1439)).apply()

    var studyEndMinute: Int
        get() = p.getInt("study_end", 22 * 60)
        set(v) = p.edit().putInt("study_end", v.coerceIn(0, 1439)).apply()

    var breakSeconds: Int
        get() = p.getInt("break_seconds", p.getInt("break_minutes", 5) * 60)
        set(v) = p.edit().putInt("break_seconds", v.coerceIn(15, 3600)).apply()

    var extensionSeconds: Int
        get() = p.getInt("extension_seconds", p.getInt("extension_minutes", 5) * 60)
        set(v) = p.edit().putInt("extension_seconds", v.coerceIn(15, 1800)).apply()

    var theme: String
        get() = p.getString("theme", "system") ?: "system"
        set(v) = p.edit().putString("theme", v).apply()

    var language: String
        get() = p.getString("language", "system") ?: "system"
        set(v) = p.edit().putString("language", v).apply()

    var testMode: Boolean
        get() = p.getBoolean("test_mode", false)
        set(v) {
            if (v != testMode) {
                val e = p.edit()
                p.all.keys.filter { it.startsWith("test_") }.forEach { e.remove(it) }
                e.putBoolean("test_mode", v).apply()
            }
        }

    var gridMode: Boolean
        get() = p.getBoolean("grid_mode", false)
        set(v) = p.edit().putBoolean("grid_mode", v).apply()


    var popupPosition: String
        get() = p.getString("popup_position", "bottom") ?: "bottom"
        set(v) = p.edit().putString("popup_position", if (v in setOf("top","middle","bottom")) v else "bottom").apply()

    var popupSize: String
        get() = p.getString("popup_size", "normal") ?: "normal"
        set(v) = p.edit().putString("popup_size", if (v=="compact") v else "normal").apply()

    var popupColor: String
        get() = p.getString("popup_color", "sage") ?: "sage"
        set(v) = p.edit().putString("popup_color", if (v in setOf("sage","peach","lavender")) v else "sage").apply()

    var popupAnimation: String
        get() = p.getString("popup_animation", "spring") ?: "spring"
        set(v) = p.edit().putString("popup_animation", if (v in setOf("spring","fade","none")) v else "spring").apply()

    var popupOpacity: Int
        get() = p.getInt("popup_opacity", 100).coerceIn(70,100)
        set(v) = p.edit().putInt("popup_opacity", v.coerceIn(70,100)).apply()

    var popupMascot: Boolean
        get() = p.getBoolean("popup_mascot", true)
        set(v) = p.edit().putBoolean("popup_mascot", v).apply()

    var popupTitle: String
        get() = p.getString("popup_title", "") ?: ""
        set(v) = p.edit().putString("popup_title", v.trim().take(56)).apply()

    var popupBody: String
        get() = p.getString("popup_body", "") ?: ""
        set(v) = p.edit().putString("popup_body", v.trim().take(140)).apply()


    var testBreakSeconds: Int
        get() = p.getInt("test_break_seconds", 30)
        set(v) = p.edit().putInt("test_break_seconds", v.coerceIn(15, 60)).apply()

    var testExtensionSeconds: Int
        get() = p.getInt("test_extension_seconds", 30)
        set(v) = p.edit().putInt("test_extension_seconds", v.coerceIn(15, 60)).apply()

    fun effectiveBreakSeconds(): Int = if (testMode) testBreakSeconds else breakSeconds
    fun effectiveExtensionSeconds(): Int = if (testMode) testExtensionSeconds else extensionSeconds

    fun setTestLimitSeconds(pkg: String, seconds: Int) {
        if(testMode) p.edit().putInt("test_limit_" + pkg, seconds.coerceIn(15,60)).apply()
    }
    fun ruleLimitSeconds(pkg: String, normal: Int): Int =
        if(testMode) p.getInt("test_limit_" + pkg, normal) else normal

    private fun sessionKey(pkg: String): String=(if(testMode) "test_" else "") + "session_" + pkg
    private fun bonusKey(pkg: String): String=(if(testMode) "test_" else "") + "bonus_" + pkg
    private fun cooldownKey(pkg: String): String=(if(testMode) "test_" else "") + "cooldown_" + pkg

    fun monitorDiagnostic(key: String): String =
        p.getString("diag_" + key, "") ?: ""

    fun recordMonitorDiagnostic(key: String, value: String) {
        p.edit().putString("diag_" + key, value.take(160)).apply()
    }

    fun rules(): Map<String, AppRule> {
        val raw = p.getString("rules", "{}") ?: "{}"
        val obj = runCatching { JSONObject(raw) }.getOrElse { JSONObject() }
        val out = linkedMapOf<String, AppRule>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val pkg = keys.next()
            val r = obj.optJSONObject(pkg) ?: continue
            out[pkg] = AppRule(
                packageName = pkg,
                label = r.optString("label", pkg),
                limitMinutes = r.optInt("limit", 20).coerceIn(1, 240),
                enabled = r.optBoolean("enabled", true),
                limitSeconds = r.optInt("seconds", r.optInt("limit", 20) * 60).coerceIn(30, 14400)
            )
        }
        return out
    }

    fun putRule(rule: AppRule) {
        val all = rules().toMutableMap()
        all[rule.packageName] = rule
        saveRules(all)
    }

    fun removeRule(packageName: String) {
        val all = rules().toMutableMap()
        all.remove(packageName)
        saveRules(all)
    }

    private fun saveRules(all: Map<String, AppRule>) {
        val root = JSONObject()
        all.values.forEach { r ->
            root.put(r.packageName, JSONObject().apply {
                put("label", r.label)
                put("limit", r.limitMinutes)
                put("enabled", r.enabled)
                put("seconds", r.limitSeconds)
            })
        }
        p.edit().putString("rules", root.toString()).apply()
    }

    private fun todayKey(): String = LocalDate.now().toString()

    fun rollDayIfNeeded() {
        val key = todayKey()
        if (p.getString("day_key", "") != key) {
            p.edit()
                .putString("day_key", key)
                .putString("daily_usage", "{}")
                .putInt("extensions_used", 0)
                .putInt("blocks_today", 0)
                .apply()
        }
    }

    fun addDailyUsage(pkg: String, deltaMs: Long) {
        if(testMode) return
        rollDayIfNeeded()
        val obj = JSONObject(p.getString("daily_usage", "{}") ?: "{}")
        obj.put(pkg, obj.optLong(pkg, 0L) + deltaMs.coerceIn(0L, 5_000L))
        p.edit().putString("daily_usage", obj.toString()).apply()
    }

    fun dailyUsage(pkg: String): Long {
        rollDayIfNeeded()
        return JSONObject(p.getString("daily_usage", "{}") ?: "{}").optLong(pkg, 0L)
    }

    fun dailyUsageAll(): Map<String, Long> {
        rollDayIfNeeded()
        val obj = JSONObject(p.getString("daily_usage", "{}") ?: "{}")
        val out = linkedMapOf<String, Long>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.optLong(k, 0L)
        }
        return out
    }

    fun sessionMs(pkg: String): Long = p.getLong(sessionKey(pkg), 0L)
    fun setSessionMs(pkg: String, value: Long) = p.edit().putLong(sessionKey(pkg), value.coerceAtLeast(0L)).apply()
    fun addSessionMs(pkg: String, delta: Long) = setSessionMs(pkg, sessionMs(pkg) + delta.coerceIn(0L, 5_000L))

    fun bonusMs(pkg: String): Long = p.getLong(bonusKey(pkg), 0L)
    fun addBonusMs(pkg: String, delta: Long) = p.edit().putLong(bonusKey(pkg), bonusMs(pkg) + delta.coerceAtLeast(0L)).apply()

    fun cooldownUntil(pkg: String): Long = p.getLong(cooldownKey(pkg), 0L)
    fun setCooldownUntil(pkg: String, value: Long) = p.edit().putLong(cooldownKey(pkg), value).apply()

    fun resetSession(pkg: String) {
        p.edit().putLong(sessionKey(pkg), 0L).putLong(bonusKey(pkg), 0L).putLong(cooldownKey(pkg), 0L).apply()
    }

    fun extensionsUsedToday(): Int { rollDayIfNeeded(); return p.getInt(if(testMode) "test_extensions_used" else "extensions_used", 0) }
    fun canExtend(studyActive: Boolean): Boolean = !studyActive && extensionsUsedToday() < maxExtensionsPerDay
    fun consumeExtension(): Boolean {
        rollDayIfNeeded()
        val used = extensionsUsedToday()
        if (used >= maxExtensionsPerDay) return false
        p.edit().putInt(if(testMode) "test_extensions_used" else "extensions_used", used + 1).apply()
        return true
    }

    fun incrementBlocks() {
        rollDayIfNeeded()
        val key=if(testMode) "test_blocks" else "blocks_today"
        p.edit().putInt(key, p.getInt(key, 0) + 1).apply()
    }

    fun blocksToday(): Int { rollDayIfNeeded(); return p.getInt(if(testMode) "test_blocks" else "blocks_today", 0) }

    fun isStudyModeActive(now: Calendar = Calendar.getInstance()): Boolean {
        if (!studyModeEnabled) return false
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return RuleMath.isMinuteInsideWindow(minute, studyStartMinute, studyEndMinute)
    }
}
