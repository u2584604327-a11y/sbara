package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

/**
 * Generic quick-launch/search hub. Only calls documented Android intents. If a third-party
 * app has no public search activity, copies the query and opens the app instead.
 */
class QuickLaunchActivity : Activity() {
    companion object {
        const val ACTION_WIDGET_SEARCH = "app.barapause.widget.SEARCH"
        const val ACTION_WIDGET_PICK = "app.barapause.widget.PICK"
        const val ACTION_WIDGET_OPEN = "app.barapause.widget.OPEN"
    }

    private data class App(val pkg:String,val name:String)
    private val worker=Executors.newSingleThreadExecutor()
    private var apps=listOf<App>()
    private lateinit var prefs:Prefs
    private var chosen:App?=null
    private lateinit var list:LinearLayout
    private lateinit var search:EditText
    private lateinit var keyword:EditText
    private lateinit var chosenLabel:TextView
    private lateinit var page:ScrollView

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        prefs=Prefs(this)
        val saved=prefs.quickTargetPackage
        if(saved.isNotBlank()) chosen=App(saved,prefs.quickTargetLabel.ifBlank { saved })
        if(intent?.action==ACTION_WIDGET_OPEN && chosen!=null) {
            val launch=packageManager.getLaunchIntentForPackage(chosen!!.pkg)
            if(launch!=null && runCatching { startActivity(launch) }.isSuccess) { finish();return }
        }
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"quick_search")))
        chosenLabel=Ui.text(this,L10n.t(this,"choose_app"),16f)
        root.addView(chosenLabel)
        chosen?.let { chosenLabel.text=L10n.t(this,"choose_app")+": "+it.name }
        search=EditText(this).apply {
            hint=L10n.t(this@QuickLaunchActivity,"quick_search_hint")
            setSingleLine(true)
            setPadding(Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,10),Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,10))
            background=Ui.cardBackground(this@QuickLaunchActivity)
            setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
            addTextChangedListener(object:TextWatcher {
                override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
                override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){render()}
                override fun afterTextChanged(s:Editable?){}
            })
        }
        root.addView(search)
        keyword=EditText(this).apply {
            hint=L10n.t(this@QuickLaunchActivity,"query_text")
            setSingleLine(true)
            setPadding(Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,10),Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,10))
            background=Ui.cardBackground(this@QuickLaunchActivity)
            setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
        }
        root.addView(keyword)
        root.addView(Ui.button(this,L10n.t(this,"recent_searches")) { selectRecentSearch() })
        root.addView(Ui.button(this,L10n.t(this,"system_web_search")) { webSearch() })
        root.addView(Ui.button(this,L10n.t(this,"launch_app")) { launchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"search_in_app")) { searchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"copy_and_open")) { copyAndOpen() })
        root.addView(Ui.text(this,L10n.t(this,"quick_search_explain"),12f))
        root.addView(Ui.button(this,L10n.t(this,"clear_search_history")) {
            prefs.clearSearchHistory()
            Toast.makeText(this,L10n.t(this,"history_cleared"),Toast.LENGTH_SHORT).show()
        })
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        val scrolling=ScrollView(this)
        scrolling.addView(list)
        root.addView(scrolling)
        page=ScrollView(this).apply { isFillViewport=true;setBackgroundColor(Appearance.palette(this@QuickLaunchActivity).bg);addView(root) }
        setContentView(page)
        if(intent?.action==ACTION_WIDGET_SEARCH) {
            keyword.post {
                keyword.requestFocus()
                val keyboard=getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                keyboard.showSoftInput(keyword,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        } else if(intent?.action==ACTION_WIDGET_PICK) {
            search.post {
                search.requestFocus()
                val keyboard=getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                keyboard.showSoftInput(search,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            page.post { page.scrollTo(0,Prefs(this).savedScroll("quick")) }
        }
        worker.execute {
            val q=Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val found=packageManager.queryIntentActivities(q,0)
                .filter { it.activityInfo.packageName!=packageName }
                .map { App(it.activityInfo.packageName,it.loadLabel(packageManager).toString()) }
                .distinctBy { it.pkg }.sortedBy { it.name.lowercase() }
            runOnUiThread {
                if(!isFinishing && !isDestroyed) { apps=found;render() }
            }
        }
    }

    private fun render() {
        if(!::list.isInitialized) return
        list.removeAllViews()
        val q=search.text.toString().lowercase().trim()
        val favorites=prefs.quickFavorites()
        apps.filter { q.isBlank() || (it.name+" "+it.pkg).lowercase().contains(q) }
            .sortedWith(compareByDescending<App> { favorites.contains(it.pkg) }.thenBy { it.name.lowercase() })
            .take(100).forEach { app ->
                val row=LinearLayout(this).apply {
                    orientation=LinearLayout.HORIZONTAL
                    gravity=Gravity.CENTER_VERTICAL
                    setPadding(Ui.dp(this@QuickLaunchActivity,7),Ui.dp(this@QuickLaunchActivity,5),Ui.dp(this@QuickLaunchActivity,7),Ui.dp(this@QuickLaunchActivity,5))
                }
                val picture=android.widget.ImageView(this).apply {
                    setImageDrawable(runCatching { packageManager.getApplicationIcon(app.pkg) }.getOrNull())
                    layoutParams=LinearLayout.LayoutParams(Ui.dp(this@QuickLaunchActivity,38),Ui.dp(this@QuickLaunchActivity,38))
                }
                row.addView(picture)
                row.addView(TextView(this).apply {
                    text=(if(app.pkg==chosen?.pkg) "✓  " else "")+app.name
                    textSize=15f
                    setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
                    setPadding(Ui.dp(this@QuickLaunchActivity,10),0,0,0)
                    maxLines=1
                })
                val star=TextView(this).apply {
                    text=if(favorites.contains(app.pkg)) "★" else "☆"
                    textSize=23f
                    setTextColor(Appearance.palette(this@QuickLaunchActivity).accent)
                    setPadding(Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,6),Ui.dp(this@QuickLaunchActivity,12),Ui.dp(this@QuickLaunchActivity,6))
                    contentDescription=L10n.t(this@QuickLaunchActivity,"favorite")
                    setOnClickListener {
                        prefs.toggleQuickFavorite(app.pkg)
                        render()
                    }
                }
                row.addView(star)
                row.setOnClickListener {
                    chosen=app
                    Prefs(this).apply {
                        quickTargetPackage=app.pkg
                        quickTargetLabel=app.name
                    }
                    WidgetUpdater.refresh(this)
                    chosenLabel.text=L10n.t(this,"choose_app")+": "+app.name
                    // Don't rebuild while typing: keep IME open and cursor position.
                }
                list.addView(row)
            }
    }

    private fun launchSelected() {
        val target=chosen ?: return Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        if(!SystemSearch.launch(this,target.pkg)) {
            Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
        }
    }

    private fun searchSelected() {
        val target=chosen ?: return Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        val query=keyword.text.toString().trim()
        if(query.isBlank()) return Toast.makeText(this,L10n.t(this,"query_required"),Toast.LENGTH_SHORT).show()
        prefs.recordSearch(query)
        if(SystemSearch.searchInTarget(this,target.pkg,query)) return
        val copied=SystemSearch.copyAndLaunch(this,target.pkg,query)
        Toast.makeText(this, L10n.t(this,if(copied) "search_fallback" else "launch_failed"), Toast.LENGTH_LONG).show()
    }

    private fun copyAndOpen() {
        val target=chosen ?: return Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        val query=keyword.text.toString().trim()
        if(query.isNotBlank()) prefs.recordSearch(query)
        if(!SystemSearch.copyAndLaunch(this,target.pkg,query)) {
            Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
        }
    }

    private fun webSearch() {
        val query=keyword.text.toString().trim()
        if(query.isEmpty()) {
            Toast.makeText(this,L10n.t(this,"query_required"),Toast.LENGTH_SHORT).show()
            return
        }
        prefs.recordSearch(query)
        if(!SystemSearch.systemWebSearch(this,query)) {
            Toast.makeText(this,L10n.t(this,"search_system_unavailable"),Toast.LENGTH_LONG).show()
        }
    }

    private fun selectRecentSearch() {
        val history=prefs.recentSearches()
        if(history.isEmpty()) return Toast.makeText(this,L10n.t(this,"history_empty"),Toast.LENGTH_SHORT).show()
        AlertDialog.Builder(this)
            .setTitle(L10n.t(this,"recent_searches"))
            .setItems(history.toTypedArray()) { _,which ->
                keyword.setText(history[which])
                keyword.setSelection(keyword.text.length)
            }
            .setNegativeButton(L10n.t(this,"cancel"),null).show()
    }

    override fun onPause() {
        if (::page.isInitialized) Prefs(this).saveScroll("quick",page.scrollY)
        super.onPause()
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
