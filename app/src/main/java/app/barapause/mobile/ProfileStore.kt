package app.barapause.mobile

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class BaraProfile(val id:String,val name:String,val active:Boolean)

/** Up to eight named, local-only configurations. One physical monitor uses only the active one. */
class ProfileStore(context:Context) {
    private val data=context.getSharedPreferences("barapause_profiles",Context.MODE_PRIVATE)
    private val settings=Prefs(context)
    companion object { const val LIMIT=8 }

    @Synchronized private fun read():JSONObject {
        val raw=data.getString("profiles_json",null)
        val result=runCatching { if(raw==null) null else JSONObject(raw) }.getOrNull()
        if(result!=null && result.optJSONArray("profiles")?.length() in 1..LIMIT) return result
        return JSONObject().apply {
            put("active","default")
            put("profiles",JSONArray().put(JSONObject().apply {
                put("id","default");put("name","默认")
                put("snapshot",settings.exportPortableSettings())
            }))
        }.also { persist(it) }
    }

    private fun persist(root:JSONObject) {
        // Commit synchronously before applying live rules: crash-recovery stays deterministic.
        check(data.edit().putString("profiles_json",root.toString()).commit())
    }

    private fun records(root:JSONObject)=root.getJSONArray("profiles")
    private fun find(array:JSONArray,id:String):JSONObject? =
        (0 until array.length()).mapNotNull{array.optJSONObject(it)}.firstOrNull{it.optString("id")==id}

    @Synchronized fun profiles():List<BaraProfile> {
        val root=read()
        val active=root.optString("active","default")
        val arr=records(root)
        return (0 until arr.length()).mapNotNull{arr.optJSONObject(it)}.map {
            BaraProfile(it.getString("id"),it.getString("name"),it.getString("id")==active)
        }
    }

    @Synchronized fun saveCurrent() {
        val root=read()
        val active=find(records(root),root.optString("active")) ?: error("Missing active profile")
        active.put("snapshot",settings.exportPortableSettings())
        persist(root)
    }

    @Synchronized fun create(name:String,blank:Boolean=false):String {
        val root=read()
        val arr=records(root)
        require(arr.length()<LIMIT) { "At most $LIMIT profiles" }
        val title=name.trim().take(24)
        require(title.isNotBlank()) { "Empty name" }
        val active=find(arr,root.optString("active")) ?: error("Missing active profile")
        active.put("snapshot",settings.exportPortableSettings())
        val id=UUID.randomUUID().toString()
        val snap=if(blank) JSONObject().apply {
            put("schema","barapause-settings/1")
            put("version",1)
            put("settings",settings.exportPortableSettings().getJSONObject("settings"))
            put("rules",JSONArray())
        } else JSONObject(settings.exportPortableSettings().toString())
        arr.put(JSONObject().apply {
            put("id",id);put("name",title);put("snapshot",snap)
        })
        persist(root)
        return id
    }

    @Synchronized fun rename(id:String,name:String) {
        val title=name.trim().take(24)
        require(title.isNotEmpty())
        val root=read()
        val target=find(records(root),id) ?: error("Unknown profile")
        target.put("name",title)
        persist(root)
    }

    @Synchronized fun switchTo(id:String):Boolean {
        val root=read()
        val activeId=root.optString("active")
        if(id==activeId) { saveCurrent(); return false }
        val arr=records(root)
        val target=find(arr,id) ?: error("Unknown profile")
        val current=find(arr,activeId) ?: error("Missing active profile")
        current.put("snapshot",settings.exportPortableSettings())
        val next=target.getJSONObject("snapshot")
        // A snapshot is internally created using the validated portable schema.
        settings.applyProfileSnapshot(next)
        root.put("active",id)
        persist(root)
        return true
    }

    /** Export all slots without usage history, permissions, search records or active timers. */
    @Synchronized fun exportBundle():JSONObject {
        saveCurrent()
        val root=read()
        return JSONObject().apply {
            put("schema","barapause-profiles/1")
            put("active",root.optString("active"))
            put("profiles",JSONArray(root.getJSONArray("profiles").toString()))
        }
    }

    /**
     * Validate the entire external file before any local mutation.
     * Restore as new UUIDs and activate the saved entry. A failed restore preserves prior slots.
     */
    @Synchronized fun importBundle(input:JSONObject) {
        require(input.optString("schema")=="barapause-profiles/1") { "Unsupported backup" }
        val list=input.optJSONArray("profiles") ?: error("Missing slots")
        require(list.length() in 1..LIMIT) { "Invalid slot count" }
        val restored=JSONArray()
        val ids=mutableMapOf<String,String>()
        for(i in 0 until list.length()) {
            val item=list.optJSONObject(i) ?: error("Malformed preset")
            val name=item.optString("name").trim().take(24)
            require(name.isNotEmpty()) { "Invalid name" }
            val snapshot=item.optJSONObject("snapshot") ?: error("Missing settings")
            require(snapshot.optString("schema")=="barapause-settings/1" &&
                snapshot.optInt("version")==1) { "Invalid settings schema" }
            val settingsObj=snapshot.optJSONObject("settings") ?: error("Invalid options")
            val rulesObj=snapshot.optJSONArray("rules") ?: error("Invalid rules")
            require(rulesObj.length()<=500 && settingsObj.length()<=48) { "Too many entries" }
            // Input validation for rules; live apply repeats full settings validation.
            val seen=mutableSetOf<String>()
            for(j in 0 until rulesObj.length()) {
                val r=rulesObj.optJSONObject(j) ?: error("Invalid rule")
                val pkg=r.optString("package")
                require(pkg.length in 3..180 &&
                    pkg.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) &&
                    seen.add(pkg)) { "Invalid package" }
                require(r.optInt("seconds",-1) in 60..14400)
            }
            val oldId=item.optString("id")
            require(oldId.isNotBlank() && !ids.containsKey(oldId)) { "Duplicate ID" }
            val newId=UUID.randomUUID().toString()
            ids[oldId]=newId
            restored.put(JSONObject().apply {
                put("id",newId);put("name",name)
                put("snapshot",JSONObject(snapshot.toString()))
            })
        }
        val active=ids[input.optString("active")] ?: error("Missing active slot")
        val next=JSONObject().apply {
            put("active",active)
            put("profiles",restored)
        }
        val old=read()
        val oldSnapshot=settings.exportPortableSettings()
        val selected=find(restored,active) ?: error("No active slot")
        try {
            // Full settings validation occurs before any writes in Prefs.importPortableSettings.
            settings.applyProfileSnapshot(selected.getJSONObject("snapshot"))
            persist(next)
        } catch(e:Exception) {
            // Best-effort restore of prior live settings; preserve old profile metadata.
            runCatching { settings.applyProfileSnapshot(oldSnapshot) }
            persist(old)
            throw e
        }
    }

    @Synchronized fun delete(id:String) {
        val root=read()
        val arr=records(root)
        require(arr.length()>1) { "Cannot remove last profile" }
        require(root.optString("active")!=id) { "Switch away before deleting" }
        val filtered=JSONArray()
        for(i in 0 until arr.length()) {
            val p=arr.getJSONObject(i)
            if(p.optString("id")!=id) filtered.put(p)
        }
        require(filtered.length()==arr.length()-1) { "Unknown profile" }
        root.put("profiles",filtered)
        persist(root)
    }
}
