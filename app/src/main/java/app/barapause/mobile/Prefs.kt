package app.barapause.mobile

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
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

    fun savedScroll(page: String): Int = p.getInt("scroll_" + page, 0).coerceAtLeast(0)
    fun saveScroll(page: String, pixels: Int) {
        p.edit().putInt("scroll_" + page, pixels.coerceAtLeast(0)).apply()
    }

    var pickerCategory: String
        get() = p.getString("picker_category", "all") ?: "all"
        set(v) = p.edit().putString("picker_category", v).apply()

    var pickerQuery: String
        get() = p.getString("picker_query", "") ?: ""
        set(v) = p.edit().putString("picker_query", v.take(128)).apply()

    var quickTargetPackage: String
        get() = p.getString("quick_target", "") ?: ""
        set(v) = p.edit().putString("quick_target", v).apply()

    var quickTargetLabel: String
        get() = p.getString("quick_target_label", "") ?: ""
        set(v) = p.edit().putString("quick_target_label", v.take(80)).apply()

    var savedContentLink:String
        get() = p.getString("content_link","") ?: ""
        set(value) = p.edit().putString("content_link",value.take(2048)).apply()

    fun quickFavorites(): Set<String> =
        p.getStringSet("quick_favorites", emptySet())?.toSet() ?: emptySet()
    fun toggleQuickFavorite(pkg: String) {
        val set=quickFavorites().toMutableSet()
        if(!set.add(pkg)) set.remove(pkg)
        p.edit().putStringSet("quick_favorites",set).apply()
    }
    fun recentSearches(): List<String> {
        val arr=runCatching { JSONArray(p.getString("quick_search_history", "[]")) }.getOrElse { JSONArray() }
        return (0 until arr.length()).mapNotNull { index -> arr.optString(index).takeIf { it.isNotBlank() } }
    }
    fun recordSearch(value: String) {
        val trimmed=value.trim().take(100)
        if(trimmed.isBlank()) return
        val list=(listOf(trimmed)+recentSearches().filterNot { it.equals(trimmed, true) }).take(12)
        val arr=JSONArray()
        list.forEach { arr.put(it) }
        p.edit().putString("quick_search_history",arr.toString()).apply()
    }
    fun clearSearchHistory() {
        p.edit().remove("quick_search_history").apply()
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

    /** One atomic preference write for batch selection; preserves prior individual limits. */
    fun putRulesBatch(changes: List<AppRule>) {
        val all = rules().toMutableMap()
        for (rule in changes) all[rule.packageName] = rule
        saveRules(all)
    }

    var popupImageUri: String
        get() = p.getString("popup_image_uri", "") ?: ""
        set(v) = p.edit().putString("popup_image_uri", v).apply()

    var popupMotion: String
        get() = p.getString("popup_motion", "peek") ?: "breathe"
        set(v) = p.edit().putString("popup_motion",
            if(v in setOf("still","breathe","bob","peek","wave")) v else "peek").apply()

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

    /**
     * Manual focus is an explicit time-bound boost to the existing study-mode rules.
     * Expiration is persisted as wall time, so it naturally stops after process death.
     * No alarms or background permissions are requested.
     */
    fun focusUntilMillis(): Long = p.getLong("focus_until",0L)
    fun focusRemainingMillis(now:Long = System.currentTimeMillis()):Long =
        (focusUntilMillis() - now).coerceAtLeast(0L)

    fun isFocusActive(now:Long = System.currentTimeMillis()):Boolean =
        focusRemainingMillis(now)>0L

    fun startFocus(minutes:Int,now:Long=System.currentTimeMillis()) {
        val duration=minutes.coerceIn(5,180)*60_000L
        p.edit().putLong("focus_until",now+duration).apply()
    }

    fun stopFocus() {
        p.edit().remove("focus_until").apply()
    }

    /** Config-only export. Excludes usage history, diagnostics, timers, test data and photo URI. */
    fun exportPortableSettings(): JSONObject {
        val root=JSONObject()
        root.put("schema","barapause-settings/1")
        root.put("version",1)
        val settings=JSONObject().apply {
            put("breakSeconds",breakSeconds)
            put("extensionSeconds",extensionSeconds)
            put("maxExtensionsPerDay",maxExtensionsPerDay)
            put("studyModeEnabled",studyModeEnabled)
            put("studyStartMinute",studyStartMinute)
            put("studyEndMinute",studyEndMinute)
            put("theme",theme)
            put("language",language)
            put("popupPosition",popupPosition)
            put("popupSize",popupSize)
            put("popupColor",popupColor)
            put("popupAnimation",popupAnimation)
            put("popupOpacity",popupOpacity)
            put("popupMascot",popupMascot)
            put("popupTitle",popupTitle)
            put("popupBody",popupBody)
            put("popupMotion",popupMotion)
        }
        root.put("settings",settings)
        val records=JSONArray()
        rules().values.filter { it.packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) }.forEach { r ->
            records.put(JSONObject().apply {
                put("package",r.packageName)
                put("label",r.label)
                put("minutes",r.limitMinutes)
                put("seconds",r.limitSeconds)
                put("enabled",r.enabled)
            })
        }
        root.put("rules",records)
        return root
    }

    /**
     * Validates every import field first, then applies a merge of the portable settings.
     * Missing keys do not override preferences. Existing rules not in backup are retained.
     */
    /**
     * Apply a full profile, not a merging backup. Validate using the existing import
     * path, then replace the rule set and clear session/cooldown counters so one profile
     * cannot inherit another profile's remaining time. Usage history remains global.
     */
    fun applyProfileSnapshot(snapshot: JSONObject): Int {
        require(snapshot.optString("schema")=="barapause-settings/1")
        val entries=snapshot.optJSONArray("rules") ?: error("Missing rules")
        val replacement=linkedMapOf<String,AppRule>()
        require(entries.length()<=500) { "Too many rules" }
        for(i in 0 until entries.length()) {
            val r=entries.optJSONObject(i) ?: error("Invalid rule")
            val pkg=r.optString("package")
            require(pkg.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) { "Invalid package" }
            require(!replacement.containsKey(pkg)) { "Duplicate package" }
            val sec=r.optInt("seconds",-1)
            require(sec in 60..14400)
            replacement[pkg]=AppRule(pkg,r.optString("label",pkg).take(80),
                (sec/60).coerceAtLeast(1),r.optBoolean("enabled",true),sec)
        }
        // The import implementation validates all settings before its first write.
        importPortableSettings(snapshot)
        val editor=p.edit()
        val root=JSONObject()
        for((_,r) in replacement) root.put(r.packageName,JSONObject().apply {
            put("label",r.label);put("limit",r.limitMinutes)
            put("enabled",r.enabled);put("seconds",r.limitSeconds)
        })
        editor.putString("rules",root.toString())
        for(key in p.all.keys) {
            if(key.startsWith("session_") || key.startsWith("bonus_") ||
                key.startsWith("cooldown_") || key.startsWith("test_session_") ||
                key.startsWith("test_bonus_") || key.startsWith("test_cooldown_"))
                editor.remove(key)
        }
        editor.apply()
        return replacement.size
    }

    /**
     * Pure validation for every profile slot. A corrupted inactive slot must fail
     * before a multi-profile backup can replace the user's stored profiles.
     */
    fun validatePortableSettings(root:JSONObject) {
        require(root.optString("schema")=="barapause-settings/1" &&
            root.optInt("version")==1) { "Unsupported settings schema" }
        val config=root.optJSONObject("settings") ?: error("Missing settings")
        val list=root.optJSONArray("rules") ?: error("Missing rules")
        require(list.length()<=500) { "Too many app rules" }
        val seen=mutableSetOf<String>()
        for(i in 0 until list.length()) {
            val obj=list.optJSONObject(i) ?: error("Malformed rule")
            val pkg=obj.optString("package")
            require(pkg.length in 3..180 &&
                pkg.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) &&
                seen.add(pkg)) { "Duplicate or malformed package" }
            require(obj.optInt("seconds",-1) in 60..14400) { "Invalid limit" }
            require(obj.optString("label",pkg).length<=80) { "Invalid label" }
        }
        fun range(key:String,min:Int,max:Int) {
            if(config.has(key)) require(config.optInt(key,Int.MIN_VALUE) in min..max) {
                "Invalid $key"
            }
        }
        fun oneOf(key:String,values:Set<String>) {
            if(config.has(key)) require(config.optString(key) in values) {
                "Invalid $key"
            }
        }
        range("breakSeconds",15,3600)
        range("extensionSeconds",15,1800)
        range("maxExtensionsPerDay",0,10)
        range("studyStartMinute",0,1439)
        range("studyEndMinute",0,1439)
        range("popupOpacity",70,100)
        oneOf("theme",setOf("system","light","dark","eye"))
        oneOf("language",setOf("system","en","ja","zh-Hans","zh-Hant"))
        oneOf("popupPosition",setOf("top","middle","bottom"))
        oneOf("popupSize",setOf("compact","normal"))
        oneOf("popupColor",setOf("sage","peach","lavender"))
        oneOf("popupAnimation",setOf("spring","fade","none"))
        oneOf("popupMotion",setOf("still","breathe","bob","peek","wave"))
        if(config.has("popupTitle")) require(config.optString("popupTitle").length<=56)
        if(config.has("popupBody")) require(config.optString("popupBody").length<=140)
        for(key in listOf("popupMascot","studyModeEnabled")) {
            if(config.has(key)) require(config.opt(key) is Boolean) { "Invalid $key" }
        }
    }

    fun importPortableSettings(root:JSONObject):Int {
        validatePortableSettings(root)
        require(root.optString("schema")=="barapause-settings/1" && root.optInt("version")==1) {
            "Unsupported backup format"
        }
        val config=root.optJSONObject("settings") ?: throw IllegalArgumentException("Missing settings")
        val incoming=root.optJSONArray("rules") ?: throw IllegalArgumentException("Missing rules")
        require(incoming.length()<=500) { "Too many rules" }
        val parsed=mutableListOf<AppRule>()
        val seen=mutableSetOf<String>()
        for(index in 0 until incoming.length()) {
            val obj=incoming.optJSONObject(index) ?: throw IllegalArgumentException("Invalid rule")
            val pkg=obj.optString("package")
            require(pkg.length in 3..180 && pkg.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) { "Invalid package" }
            require(seen.add(pkg)) { "Duplicate package" }
            val sec=obj.optInt("seconds",-1)
            require(sec in 60..14400) { "Invalid time" }
            parsed.add(AppRule(
                pkg,obj.optString("label",pkg).take(80),
                (sec/60).coerceAtLeast(1),obj.optBoolean("enabled",true),sec
            ))
        }
        fun validEnum(key:String, allowed:Set<String>):String? =
            if(config.has(key)) config.optString(key).also { require(it in allowed) { "Invalid $key" } } else null
        val safeTheme=validEnum("theme",setOf("system","light","dark","eye"))
        val safeLang=validEnum("language",setOf("system","en","ja","zh-Hans","zh-Hant"))
        val safePosition=validEnum("popupPosition",setOf("top","middle","bottom"))
        val safeSize=validEnum("popupSize",setOf("compact","normal"))
        val safeColor=validEnum("popupColor",setOf("sage","peach","lavender"))
        val safeAnimation=validEnum("popupAnimation",setOf("spring","fade","none"))
        val safeMotion=validEnum("popupMotion",setOf("still","breathe","bob","peek","wave"))
        fun optionalInt(key:String,min:Int,max:Int):Int? {
            if(!config.has(key)) return null
            val value=config.optInt(key,-1)
            require(value in min..max) { "Invalid $key" }
            return value
        }
        val safeBreak=optionalInt("breakSeconds",15,3600)
        val safeExtend=optionalInt("extensionSeconds",15,1800)
        val safeMax=optionalInt("maxExtensionsPerDay",0,10)
        val safeStart=optionalInt("studyStartMinute",0,1439)
        val safeEnd=optionalInt("studyEndMinute",0,1439)
        val safeOpacity=optionalInt("popupOpacity",70,100)
        fun optionalText(key:String,max:Int):String? {
            if(!config.has(key)) return null
            val str=config.optString(key)
            require(str.length<=max) { "Too long: $key" }
            return str
        }
        val safeTitle=optionalText("popupTitle",56)
        val safeBody=optionalText("popupBody",140)
        // Commit validation-complete state. The normal setters clamp; all values are already checked.
        safeBreak?.let { breakSeconds=it }
        safeExtend?.let { extensionSeconds=it }
        safeMax?.let { maxExtensionsPerDay=it }
        if(config.has("studyModeEnabled")) studyModeEnabled=config.optBoolean("studyModeEnabled")
        safeStart?.let { studyStartMinute=it }
        safeEnd?.let { studyEndMinute=it }
        safeTheme?.let { theme=it }
        safeLang?.let { language=it }
        safePosition?.let { popupPosition=it }
        safeSize?.let { popupSize=it }
        safeColor?.let { popupColor=it }
        safeAnimation?.let { popupAnimation=it }
        safeMotion?.let { popupMotion=it }
        safeOpacity?.let { popupOpacity=it }
        if(config.has("popupMascot")) popupMascot=config.optBoolean("popupMascot")
        safeTitle?.let { popupTitle=it }
        safeBody?.let { popupBody=it }
        putRulesBatch(parsed)
        return parsed.size
    }

    fun isStudyModeActive(now: Calendar = Calendar.getInstance()): Boolean {
        if(isFocusActive()) return true
        if (!studyModeEnabled) return false
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return RuleMath.isMinuteInsideWindow(minute, studyStartMinute, studyEndMinute)
    }
}
