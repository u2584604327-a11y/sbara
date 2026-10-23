package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

/**
 * Explicit app selection on every normal search. The remembered target is used
 * exclusively when user presses the desktop widget's "Open saved app" button.
 */
class QuickLaunchActivity : Activity() {
    companion object {
        const val ACTION_WIDGET_SEARCH="app.barapause.widget.SEARCH"
        const val ACTION_WIDGET_PICK="app.barapause.widget.PICK"
        const val ACTION_WIDGET_OPEN="app.barapause.widget.OPEN"
        const val ACTION_WIDGET_RECENT="app.barapause.widget.RECENT"
    }

    private data class App(val pkg:String,val name:String)
    private val worker=Executors.newSingleThreadExecutor()
    private val apps=mutableListOf<App>()
    private var chosen:App?=null
    private lateinit var prefs:Prefs
    private lateinit var list:LinearLayout
    private lateinit var search:EditText
    private lateinit var keyword:EditText
    private lateinit var chosenLabel:TextView
    private lateinit var page:ScrollView

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        prefs=Prefs(this)
        // Do not restore prefs.quickTargetPackage as an implicit selection.
        if(intent?.action==ACTION_WIDGET_OPEN) {
            val saved=prefs.quickTargetPackage
            if(saved.isNotBlank() && SystemSearch.launch(this,saved)) {
                finish()
                return
            }
        }

        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"quick_search")))
        chosenLabel=Ui.text(this,L10n.t(this,"qs_choose_required"),17f)
        root.addView(chosenLabel)

        search=EditText(this).apply {
            hint=L10n.t(this@QuickLaunchActivity,"quick_search_hint")
            setSingleLine(true)
            setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
            background=Ui.cardBackground(this@QuickLaunchActivity)
            setPadding(dp(12),dp(10),dp(12),dp(10))
            addTextChangedListener(object:TextWatcher {
                override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int) {}
                override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) { renderList() }
                override fun afterTextChanged(s:Editable?) {}
            })
        }
        root.addView(Ui.section(this,L10n.t(this,"choose_app")))
        root.addView(search)

        keyword=EditText(this).apply {
            hint=L10n.t(this@QuickLaunchActivity,"query_text")
            setSingleLine(true)
            setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
            background=Ui.cardBackground(this@QuickLaunchActivity)
            setPadding(dp(12),dp(10),dp(12),dp(10))
        }
        root.addView(Ui.section(this,L10n.t(this,"qs_keyword")))
        root.addView(keyword)

        root.addView(Ui.button(this,L10n.t(this,"search_in_app")) { searchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"qs_open_search_ui")) { openTargetSearchUi() })
        root.addView(Ui.button(this,L10n.t(this,"copy_and_open")) { copyAndOpen() })
        root.addView(Ui.button(this,L10n.t(this,"launch_app")) { launchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"recent_searches")) { selectRecentSearch() })
        root.addView(Ui.button(this,L10n.t(this,"system_web_search")) { webSearch() })
        root.addView(Ui.text(this,L10n.t(this,"qs_explain"),13f))
        root.addView(Ui.button(this,L10n.t(this,"clear_search_history")) {
            prefs.clearSearchHistory()
            WidgetUpdater.refresh(this)
            Toast.makeText(this,L10n.t(this,"history_cleared"),Toast.LENGTH_SHORT).show()
        })

        root.addView(Ui.section(this,L10n.t(this,"qs_favorites")))
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(list)
        page=ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(Appearance.palette(this@QuickLaunchActivity).bg)
            addView(root)
        }
        setContentView(page)

        when(intent?.action) {
            ACTION_WIDGET_PICK -> focus(search)
            ACTION_WIDGET_RECENT -> keyword.post { selectRecentSearch() }
            ACTION_WIDGET_SEARCH -> focus(keyword)
            else -> page.post { page.scrollTo(0,prefs.savedScroll("quick")) }
        }
        worker.execute {
            val intent=Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val found=packageManager.queryIntentActivities(intent,0)
                .filter { it.activityInfo.packageName!=packageName }
                .map { App(it.activityInfo.packageName,it.loadLabel(packageManager).toString()) }
                .distinctBy { it.pkg }
                .sortedBy { it.name.lowercase() }
            runOnUiThread {
                if(!isFinishing && !isDestroyed) {
                    apps.clear(); apps.addAll(found);renderList()
                }
            }
        }
    }

    private fun dp(n:Int)=Ui.dp(this,n)
    private fun focus(field:EditText) {
        field.post {
            field.requestFocus()
            val mgr=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.showSoftInput(field,InputMethodManager.SHOW_IMPLICIT)
        }
    }
    private fun select(app:App) {
        chosen=app
        chosenLabel.text=L10n.t(this,"qs_chosen")+": "+app.name
        // Saved target is only used for the explicit widget "Open saved" button.
        prefs.quickTargetPackage=app.pkg
        prefs.quickTargetLabel=app.name
        WidgetUpdater.refresh(this)
        renderList()
    }
    private fun renderList() {
        if(!::list.isInitialized) return
        list.removeAllViews()
        val q=search.text.toString().trim().lowercase()
        val fav=prefs.quickFavorites()
        val filtered=apps.filter { q.isBlank() || (it.name+" "+it.pkg).lowercase().contains(q) }
            .sortedWith(compareByDescending<App> { fav.contains(it.pkg) }.thenBy { it.name.lowercase() }).take(90)
        for(app in filtered) {
            val row=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL
                gravity=Gravity.CENTER_VERTICAL
                setPadding(dp(8),dp(4),dp(8),dp(4))
            }
            row.addView(ImageView(this).apply {
                setImageDrawable(runCatching { packageManager.getApplicationIcon(app.pkg) }.getOrNull())
                layoutParams=LinearLayout.LayoutParams(dp(38),dp(38))
                contentDescription=app.name
            })
            row.addView(TextView(this).apply {
                text=(if(chosen?.pkg==app.pkg) "✓ " else "")+app.name
                setTextColor(Appearance.palette(this@QuickLaunchActivity).text)
                textSize=15f
                maxLines=1
                setPadding(dp(9),0,0,0)
            },LinearLayout.LayoutParams(0,dp(46),1f).apply { gravity=Gravity.CENTER_VERTICAL })
            row.addView(TextView(this).apply {
                text=if(fav.contains(app.pkg)) "★" else "☆"
                textSize=23f
                gravity=Gravity.CENTER
                setTextColor(Appearance.palette(this@QuickLaunchActivity).accent)
                layoutParams=LinearLayout.LayoutParams(dp(48),dp(48))
                contentDescription=L10n.t(this@QuickLaunchActivity,"favorite")
                setOnClickListener { prefs.toggleQuickFavorite(app.pkg);renderList() }
            })
            row.setOnClickListener { select(app) }
            list.addView(row)
        }
    }
    private fun needTarget():App? {
        val target=chosen
        if(target==null) {
            Toast.makeText(this,L10n.t(this,"qs_choose_required"),Toast.LENGTH_LONG).show()
            // Selecting an app remains explicit even if one was used last time.
            focus(search)
        }
        return target
    }
    private fun launchSelected() {
        val target=needTarget() ?: return
        if(!SystemSearch.launch(this,target.pkg))
            Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
    }
    private fun searchSelected() {
        val target=needTarget() ?: return
        val query=keyword.text.toString().trim()
        if(query.isBlank()) {
            openTargetSearchUi()
            return
        }
        prefs.recordSearch(query)
        WidgetUpdater.refresh(this)
        if(SystemSearch.searchInTarget(this,target.pkg,query)) return
        unsupportedSearch(target.pkg,query)
    }
    private fun openTargetSearchUi() {
        val target=needTarget() ?: return
        if(SystemSearch.openSearchUi(this,target.pkg)) return
        AlertDialog.Builder(this)
            .setTitle(L10n.t(this,"qs_no_search_ui"))
            .setMessage(L10n.t(this,"qs_no_search_explain"))
            .setPositiveButton(L10n.t(this,"launch_app")) { _,_ ->
                if(!SystemSearch.launch(this,target.pkg))
                    Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
            }.setNeutralButton(L10n.t(this,"system_web_search")) { _,_ -> webSearch() }
            .setNegativeButton(L10n.t(this,"cancel"),null).show()
    }
    private fun unsupportedSearch(pkg:String,query:String) {
        AlertDialog.Builder(this)
            .setTitle(L10n.t(this,"qs_no_search_ui"))
            .setMessage(L10n.t(this,"qs_fallback_explain"))
            .setPositiveButton(L10n.t(this,"qs_try_search_ui")) { _,_ ->
                if(!SystemSearch.openSearchUi(this,pkg)) {
                    Toast.makeText(this,L10n.t(this,"qs_no_search_ui"),Toast.LENGTH_LONG).show()
                }
            }
            .setNeutralButton(L10n.t(this,"copy_and_open")) { _,_ ->
                if(!SystemSearch.copyAndLaunch(this,pkg,query))
                    Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(L10n.t(this,"cancel"),null).show()
    }
    private fun copyAndOpen() {
        val target=needTarget() ?: return
        val query=keyword.text.toString().trim()
        if(query.isNotBlank()) prefs.recordSearch(query)
        WidgetUpdater.refresh(this)
        if(!SystemSearch.copyAndLaunch(this,target.pkg,query))
            Toast.makeText(this,L10n.t(this,"launch_failed"),Toast.LENGTH_LONG).show()
    }
    private fun webSearch() {
        val q=keyword.text.toString().trim()
        if(q.isBlank()) {
            focus(keyword);Toast.makeText(this,L10n.t(this,"query_required"),Toast.LENGTH_SHORT).show()
            return
        }
        prefs.recordSearch(q);WidgetUpdater.refresh(this)
        if(!SystemSearch.systemWebSearch(this,q))
            Toast.makeText(this,L10n.t(this,"search_system_unavailable"),Toast.LENGTH_LONG).show()
    }
    private fun selectRecentSearch() {
        val history=prefs.recentSearches()
        if(history.isEmpty()) {
            Toast.makeText(this,L10n.t(this,"history_empty"),Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this).setTitle(L10n.t(this,"recent_searches"))
            .setItems(history.toTypedArray()) { _,which ->
                keyword.setText(history[which])
                keyword.setSelection(keyword.text.length)
            }.setNegativeButton(L10n.t(this,"cancel"),null).show()
    }
    override fun onPause() {
        if(::page.isInitialized) prefs.saveScroll("quick",page.scrollY)
        super.onPause()
    }
    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
