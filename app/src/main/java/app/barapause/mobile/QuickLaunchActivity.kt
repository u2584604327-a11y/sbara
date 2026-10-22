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
    private data class App(val pkg:String,val name:String)
    private val worker=Executors.newSingleThreadExecutor()
    private var apps=listOf<App>()
    private var chosen:App?=null
    private lateinit var list:LinearLayout
    private lateinit var search:EditText
    private lateinit var keyword:EditText
    private lateinit var chosenLabel:TextView

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"quick_search")))
        chosenLabel=Ui.text(this,L10n.t(this,"choose_app"),16f)
        root.addView(chosenLabel)
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
        root.addView(Ui.button(this,L10n.t(this,"launch_app")) { launchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"search_in_app")) { searchSelected() })
        root.addView(Ui.button(this,L10n.t(this,"copy_and_open")) { copyAndOpen() })
        root.addView(Ui.text(this,L10n.t(this,"quick_search_explain"),12f))
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        val scrolling=ScrollView(this)
        scrolling.addView(list)
        root.addView(scrolling)
        setContentView(ScrollView(this).apply { addView(root) })
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
        apps.filter { q.isBlank() || (it.name+" "+it.pkg).lowercase().contains(q) }
            .take(80).forEach { app ->
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
                row.setOnClickListener {
                    chosen=app
                    chosenLabel.text=L10n.t(this,"choose_app")+": "+app.name
                    // Don't rebuild while typing: keep IME open and cursor position.
                }
                list.addView(row)
            }
    }

    private fun launchSelected() {
        val target=chosen ?: return Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        val intent=packageManager.getLaunchIntentForPackage(target.pkg)
        if(intent==null) Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        else runCatching { startActivity(intent) }.onFailure { Toast.makeText(this,it.javaClass.simpleName,Toast.LENGTH_LONG).show() }
    }

    private fun searchSelected() {
        val target=chosen ?: return Toast.makeText(this,L10n.t(this,"choose_app"),Toast.LENGTH_SHORT).show()
        val query=keyword.text.toString().trim()
        if(query.isBlank()) return Toast.makeText(this,L10n.t(this,"query_required"),Toast.LENGTH_SHORT).show()
        val attempts=listOf(
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage(target.pkg)
                putExtra(SearchManager.QUERY,query)
            },
            Intent("com.google.android.gms.actions.SEARCH_ACTION").apply {
                setPackage(target.pkg)
                putExtra("query",query)
            }
        )
        for(intent in attempts) {
            if(intent.resolveActivity(packageManager)!=null) {
                if(runCatching { startActivity(intent) }.isSuccess) return
            }
        }
        copyAndOpen()
        Toast.makeText(this,L10n.t(this,"search_fallback"),Toast.LENGTH_LONG).show()
    }

    private fun copyAndOpen() {
        val query=keyword.text.toString().trim()
        if(query.isNotEmpty()) {
            val clipboard=getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("BaraPause query",query))
        }
        launchSelected()
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
