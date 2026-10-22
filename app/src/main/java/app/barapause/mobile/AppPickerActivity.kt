package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

class AppPickerActivity : Activity() {
    private data class Installed(val pkg: String, val label: String, val category: String)
    private lateinit var prefs: Prefs
    private val bg = Executors.newSingleThreadExecutor()
    private var apps: List<Installed> = emptyList()
    private lateinit var container: LinearLayout
    private lateinit var summary: TextView
    private lateinit var toggleView: Button
    private var query = ""
    private var category = "all"
    private val categories = listOf("all","selected","browser","social","video","ai","office","tools","other")
    private lateinit var chips: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        prefs = Prefs(this)
        val root = Ui.column(this)
        root.addView(Ui.title(this, L10n.t(this,"selected_apps")))
        root.addView(Ui.text(this,L10n.t(this,"apps_intro"),14f))
        summary = Ui.text(this,"",14f)
        root.addView(summary)

        val search = EditText(this).apply {
            hint = L10n.t(this@AppPickerActivity,"search")
            textSize = 16f
            setSingleLine(true)
            setTextColor(Appearance.palette(this@AppPickerActivity).text)
            setHintTextColor(Appearance.palette(this@AppPickerActivity).muted)
            background = Ui.cardBackground(this@AppPickerActivity)
            setPadding(Ui.dp(this@AppPickerActivity,14),Ui.dp(this@AppPickerActivity,12),Ui.dp(this@AppPickerActivity,14),Ui.dp(this@AppPickerActivity,12))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start:Int,count:Int,after:Int) {}
                override fun onTextChanged(s: CharSequence?, start:Int,before:Int,count:Int) { query=s?.toString()?.trim()?.lowercase() ?: ""; renderItems() }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        root.addView(search)

        val modeRow=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        toggleView=Button(this).apply {
            isAllCaps=false
            setOnClickListener {
                prefs.gridMode=!prefs.gridMode
                updateViewButton()
                renderItems()
            }
        }
        modeRow.addView(toggleView)
        root.addView(modeRow)
        updateViewButton()

        chips=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        root.addView(HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled=false
            addView(chips)
        })
        renderChips()
        container=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(container)
        setContentView(ScrollView(this).apply { addView(root) })

        summary.text="…"
        bg.execute {
            val queryIntent=Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val installed=packageManager.queryIntentActivities(queryIntent,0)
                .filter { it.activityInfo.packageName != packageName }
                .map { Installed(it.activityInfo.packageName,it.loadLabel(packageManager).toString(),
                    categorize(it.activityInfo.packageName,it.loadLabel(packageManager).toString())) }
                .distinctBy { it.pkg }
                .sortedBy { it.label.lowercase() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                apps=installed
                renderItems()
            }
        }
    }

    private fun categorize(pkg:String,label:String):String {
        val t=(pkg+" "+label).lowercase()
        return when {
            Regex("chrome|firefox|browser|edge|brave|opera|vivaldi").containsMatchIn(t) -> "browser"
            Regex("bilibili|youtube|tiktok|douyin|抖音|快手|netflix|iqiyi|优酷|腾讯视频|芒果|twitch|reels").containsMatchIn(t) -> "video"
            Regex("telegram|discord|wechat|weixin|whatsapp|signal|instagram|facebook|小红书|微博|reddit|line\\b|twitter|x\\.com").containsMatchIn(t) -> "social"
            Regex("chatgpt|claude|deepseek|gemini|grok|qwen|豆包|kimi|perplexity|copilot|lm studio|mnn chat|ai studio").containsMatchIn(t) -> "ai"
            Regex("gmail|outlook|mail|office|teams|docs|notion|wps|word|excel|drive").containsMatchIn(t) -> "office"
            Regex("clock|camera|calendar|calculator|settings|files|vault|工具|地图|map|weather|时钟|文件").containsMatchIn(t) -> "tools"
            else -> "other"
        }
    }

    private fun renderChips() {
        chips.removeAllViews()
        val colors=Appearance.palette(this)
        categories.forEach { id ->
            val v=TextView(this).apply {
                text=L10n.t(this@AppPickerActivity,id)
                textSize=13.5f
                setPadding(Ui.dp(this@AppPickerActivity,13),Ui.dp(this@AppPickerActivity,10),Ui.dp(this@AppPickerActivity,13),Ui.dp(this@AppPickerActivity,10))
                setTextColor(if(category==id) colors.bg else colors.text)
                background=android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius=Ui.dp(this@AppPickerActivity,18).toFloat()
                    setColor(if(category==id) colors.accent else colors.button)
                }
                setOnClickListener { category=id;renderChips();renderItems() }
            }
            chips.addView(v,LinearLayout.LayoutParams(-2,-2).apply {
                marginEnd=Ui.dp(this@AppPickerActivity,6)
                topMargin=Ui.dp(this@AppPickerActivity,10)
                bottomMargin=Ui.dp(this@AppPickerActivity,9)
            })
        }
    }

    private fun updateViewButton() {
        toggleView.text=if(prefs.gridMode) "▤ "+L10n.t(this,"list") else "▦ "+L10n.t(this,"grid")
    }

    private fun renderItems() {
        if (!::container.isInitialized) return
        val rules=prefs.rules()
        val selected=rules.values.count { it.enabled }
        summary.text=L10n.t(this,"summary_apps")+": $selected   ·   "+apps.size
        container.removeAllViews()
        val filtered=apps.filter { app ->
            (query.isEmpty() || (app.label+" "+app.pkg).lowercase().contains(query)) &&
                (category=="all" || (category=="selected" && rules[app.pkg]?.enabled==true) || app.category==category)
        }.sortedWith(compareByDescending<Installed> { rules[it.pkg]?.enabled==true }.thenBy { it.label.lowercase() })
        if (filtered.isEmpty()) {
            container.addView(Ui.text(this, L10n.t(this,"empty")))
            return
        }
        if (prefs.gridMode) {
            val grid=GridLayout(this).apply { columnCount=3; alignmentMode=GridLayout.ALIGN_BOUNDS }
            val totalWidth=resources.displayMetrics.widthPixels - Ui.dp(this,36)
            val cell=totalWidth/3
            filtered.forEach { app -> grid.addView(gridCell(app,rules[app.pkg]),GridLayout.LayoutParams().apply {
                width=cell; height=Ui.dp(this@AppPickerActivity,125)
            }) }
            container.addView(grid)
        } else {
            filtered.forEach { app -> container.addView(listCell(app,rules[app.pkg])) }
        }
    }

    private fun icon(pkg:String,size:Int):ImageView=ImageView(this).apply {
        try { setImageDrawable(packageManager.getApplicationIcon(pkg)) }
        catch (_:Exception) { setImageResource(android.R.drawable.sym_def_app_icon) }
        layoutParams=LinearLayout.LayoutParams(Ui.dp(this@AppPickerActivity,size),Ui.dp(this@AppPickerActivity,size))
        contentDescription=pkg
    }

    private fun listCell(app:Installed,rule:AppRule?):View {
        val p=Appearance.palette(this)
        val row=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(Ui.dp(this@AppPickerActivity,9),Ui.dp(this@AppPickerActivity,8),Ui.dp(this@AppPickerActivity,8),Ui.dp(this@AppPickerActivity,8))
            background=Ui.cardBackground(this@AppPickerActivity)
        }
        row.addView(icon(app.pkg,40))
        val detail=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(Ui.dp(this@AppPickerActivity,10),0,0,0) }
        detail.addView(TextView(this).apply {
            text=app.label; textSize=15f; setTypeface(typeface,Typeface.BOLD);setTextColor(p.text);maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END
        })
        detail.addView(TextView(this).apply {
            text=duration(prefs.ruleLimitSeconds(app.pkg,rule?.limitSeconds ?:1200));textSize=12f;setTextColor(p.muted)
        })
        row.addView(detail,LinearLayout.LayoutParams(0,-2,1f))
        val time=TextView(this).apply {
            text=duration(prefs.ruleLimitSeconds(app.pkg,rule?.limitSeconds ?:1200))+"  ✎"
            textSize=13f;setTextColor(p.text)
            setPadding(Ui.dp(this@AppPickerActivity,8),Ui.dp(this@AppPickerActivity,13),Ui.dp(this@AppPickerActivity,8),Ui.dp(this@AppPickerActivity,13))
            background=android.graphics.drawable.GradientDrawable().apply {
                setColor(p.button);cornerRadius=Ui.dp(this@AppPickerActivity,14).toFloat()
            }
            setOnClickListener { chooseDuration(app) }
        }
        row.addView(time)
        val check=CheckBox(this).apply {
            isChecked=rule?.enabled==true
            buttonTintList=android.content.res.ColorStateList.valueOf(p.accent)
            contentDescription=app.label
            setOnCheckedChangeListener { _, enabled -> changeEnabled(app,enabled) }
        }
        row.addView(check)
        row.setOnClickListener { check.isChecked=!check.isChecked }
        return LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL;setPadding(0,0,0,Ui.dp(this@AppPickerActivity,7))
            addView(row)
        }
    }

    private fun gridCell(app:Installed,rule:AppRule?):View {
        val p=Appearance.palette(this)
        val selected=rule?.enabled==true
        return LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
            setPadding(Ui.dp(this@AppPickerActivity,3),Ui.dp(this@AppPickerActivity,5),Ui.dp(this@AppPickerActivity,3),Ui.dp(this@AppPickerActivity,5))
            background=android.graphics.drawable.GradientDrawable().apply {
                cornerRadius=Ui.dp(this@AppPickerActivity,18).toFloat()
                setColor(if(selected) p.button else p.card)
                setStroke(Ui.dp(this@AppPickerActivity,2),if(selected) p.accent else p.stroke)
            }
            addView(icon(app.pkg,46))
            addView(TextView(this@AppPickerActivity).apply {
                text=app.label;maxLines=1;ellipsize=android.text.TextUtils.TruncateAt.END
                textSize=12.5f;gravity=Gravity.CENTER;setTextColor(p.text)
            })
            addView(TextView(this@AppPickerActivity).apply {
                text=(if(selected) "✓ " else "")+duration(prefs.ruleLimitSeconds(app.pkg,rule?.limitSeconds ?:1200))
                textSize=11f;gravity=Gravity.CENTER;setTextColor(p.accent)
            })
            setOnClickListener { changeEnabled(app,!selected) }
            setOnLongClickListener { chooseDuration(app);true }
        }
    }

    private fun changeEnabled(app:Installed,enable:Boolean) {
        val r=prefs.rules()[app.pkg]
        prefs.putRule(AppRule(app.pkg,app.label,r?.limitMinutes ?:20,enable,r?.limitSeconds ?:1200))
        renderItems()
    }

    private fun duration(s:Int):String =
        if(s<60) "$s "+L10n.t(this,"seconds") else "${s/60} "+L10n.t(this,"minute")

    private fun chooseDuration(app:Installed) {
        val options=mutableListOf<Pair<String,Int>>()
        if(prefs.testMode) { options.add("30 "+L10n.t(this,"seconds") to 30);options.add("1 "+L10n.t(this,"minute") to 60) }
        listOf(5,10,15,20,30,45,60).forEach { options.add("$it "+L10n.t(this,"minute") to it*60) }
        val labels=options.map { it.first }.toMutableList()
        labels.add(L10n.t(this,"custom")+"…")
        AlertDialog.Builder(this).setTitle(app.label+" · "+L10n.t(this,"limit"))
            .setItems(labels.toTypedArray()) { _, which ->
                if(which==options.size) customMinutes(app)
                else saveDuration(app,options[which].second)
            }.setNegativeButton(L10n.t(this,"cancel"),null).show()
    }

    private fun customMinutes(app:Installed) {
        val input=EditText(this).apply {
            inputType=InputType.TYPE_CLASS_NUMBER
            hint=L10n.t(this@AppPickerActivity,"enter_minutes")
            setText(((prefs.rules()[app.pkg]?.limitSeconds ?:1200)/60).coerceAtLeast(1).toString())
            setSelectAllOnFocus(true)
        }
        val wrapper=LinearLayout(this).apply {
            setPadding(Ui.dp(this@AppPickerActivity,22),0,Ui.dp(this@AppPickerActivity,22),0)
            addView(input)
        }
        val dialog=AlertDialog.Builder(this).setTitle(L10n.t(this,"custom"))
            .setView(wrapper).setNegativeButton(L10n.t(this,"cancel"),null)
            .setPositiveButton(L10n.t(this,"save"),null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val min=input.text.toString().toIntOrNull()
                if(min==null || min !in 1..240) {
                    input.error=L10n.t(this,"invalid_minutes")
                } else {
                    saveDuration(app,min*60);dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun saveDuration(app:Installed,seconds:Int) {
        val prev=prefs.rules()[app.pkg]
        if (prefs.testMode && seconds<=60) {
            if (prev==null) prefs.putRule(AppRule(app.pkg,app.label,20,true,1200))
            prefs.setTestLimitSeconds(app.pkg,seconds)
        } else {
            prefs.putRule(AppRule(app.pkg,app.label,(seconds/60).coerceAtLeast(1),prev?.enabled ?: true,seconds))
        }
        renderItems()
    }

    override fun onDestroy() {
        bg.shutdownNow()
        super.onDestroy()
    }
}
