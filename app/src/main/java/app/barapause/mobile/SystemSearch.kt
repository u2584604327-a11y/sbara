package app.barapause.mobile

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Public Android intents only. No private deep links, arbitrary intent URI strings or accessibility.
 * A successful startActivity means the intent was handed to Android, not that third-party search
 * results are guaranteed to appear.
 */
object SystemSearch {
    fun launch(context: Context, pkg: String): Boolean {
        val launch=context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launch) }.isSuccess
    }

    fun searchInTarget(context:Context,pkg:String,query:String):Boolean {
        if(query.isBlank()) return false
        val candidates=listOf(
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage(pkg)
                putExtra(SearchManager.QUERY,query)
            },
            Intent("com.google.android.gms.actions.SEARCH_ACTION").apply {
                setPackage(pkg)
                putExtra("query",query)
            }
        )
        for(candidate in candidates) {
            if(runCatching { context.startActivity(candidate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess)
                return true
        }
        return false
    }

    /**
     * Best-effort entry into an app's *publicly exported* search activity.
     * Google's SEARCH_ACTION specifies that omitting QUERY may open the search UI.
     * No generic Android API can tap/focus a private search box in any app.
     */
    fun openSearchUi(context: Context, pkg: String): Boolean {
        val intents=listOf(
            Intent("com.google.android.gms.actions.SEARCH_ACTION").apply { setPackage(pkg) },
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage(pkg)
                putExtra(SearchManager.QUERY,"")
            }
        )
        for(intent in intents) {
            if(runCatching {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess) return true
        }
        return false
    }

    fun systemWebSearch(context: Context, query:String):Boolean {
        if(query.isBlank()) return false
        val system=Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY,query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if(runCatching { context.startActivity(system) }.isSuccess) return true
        // Explicit ACTION_VIEW delegates networking to user's browser; BaraPause has no INTERNET permission.
        val uri=Uri.parse("https://www.google.com/search").buildUpon()
            .appendQueryParameter("q",query).build()
        return runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW,uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
    }

    fun copyAndLaunch(context:Context,pkg:String,query:String):Boolean {
        if(query.isNotBlank()) {
            val clipboard=context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("BaraPause",query))
        }
        return launch(context,pkg)
    }
}
