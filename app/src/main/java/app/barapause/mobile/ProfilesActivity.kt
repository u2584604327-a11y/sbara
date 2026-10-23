package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class ProfilesActivity:Activity() {
    private lateinit var profiles:ProfileStore
    private lateinit var settings:Prefs
    private lateinit var page:ScrollView

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        Appearance.applyWindow(this)
        profiles=ProfileStore(this)
        settings=Prefs(this)
        render()
    }
    private fun label(zh:String,en:String,tw:String=zh,ja:String=en):String {
        return when(settings.language) {
            "en" -> en
            "ja" -> ja
            "zh-Hant" -> tw
            else -> zh
        }
    }
    private fun toast(text:String)=Toast.makeText(this,text,Toast.LENGTH_LONG).show()
    private fun render() {
        val old=if(::page.isInitialized)page.scrollY else settings.savedScroll("profiles")
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,label("快速切换存档","Profile presets","快速切換存檔","プロファイル")))
        root.addView(Ui.text(this,label(
            "最多 8 个独立设置档；同一时间只有一个档位生效。切换时重置当前倒计时，但保留今天的总体使用统计。",
            "Up to 8 local presets. Only one runs at a time. Switching clears session timers, not daily usage.",
            "最多 8 個設定檔；僅一個生效。切換會重設倒數但保留今日統計。",
            "最大8件。切替時に使用セッションをリセットし、今日の統計は保持します。"),14f))
        root.addView(Ui.section(this,label("存档操作","Preset actions","存檔操作","操作")))
        root.addView(Ui.button(this,label("保存当前档位","Save current preset","儲存目前存檔","現在の設定を保存")) {
            runCatching{ profiles.saveCurrent() }.onSuccess{toast(label("已保存","Saved"))}
                .onFailure {toast(it.message ?: "Error")}
        })
        root.addView(Ui.button(this,label("复制当前配置","Duplicate current","複製目前設定","現在の設定を複製")) {
            nameDialog(label("新存档名称","New profile name","新存檔名稱","名前"),"考研") { name ->
                runCatching{ profiles.create(name,false) }.onSuccess{render()}
                    .onFailure{toast(it.message ?: "Error")}
            }
        })
        root.addView(Ui.button(this,label("创建空白规则档","Create preset without app rules","建立空白規則檔","空のルールを作成")) {
            nameDialog(label("新存档名称","New profile name"),"日常") { name ->
                runCatching{ profiles.create(name,true) }.onSuccess{render()}
                    .onFailure{toast(it.message ?: "Error")}
            }
        })
        root.addView(Ui.section(this,label("所有存档","Saved presets","所有存檔","保存した設定")))
        profiles.profiles().forEach { preset ->
            val active=preset.active
            val cell=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL
                background=Ui.cardBackground(this@ProfilesActivity)
                setPadding(dp(12),dp(12),dp(12),dp(12))
            }
            cell.addView(Ui.section(this,(if(active) "✓  " else "")+preset.name))
            if(!active) {
                cell.addView(Ui.button(this,label("切换到此档","Switch to this preset","切換到此檔","この設定に切替")) {
                    AlertDialog.Builder(this)
                        .setTitle(label("确认切换？","Switch active preset?","確認切換？","切り替えますか？"))
                        .setMessage(label("将保存旧档并重置会话/休息倒计时。","Current preset will be saved; session timers reset."))
                        .setNegativeButton(label("取消","Cancel","取消","キャンセル"),null)
                        .setPositiveButton(label("切换","Switch","切換","切替")){_,_->
                            runCatching {
                                profiles.switchTo(preset.id)
                                if(settings.monitoringEnabled && PermissionUtils.hasUsageAccess(this) &&
                                    PermissionUtils.hasOverlay(this)) {
                                    val i=Intent(this,MonitoringService::class.java).apply {
                                        action=MonitoringService.ACTION_PROFILE_CHANGED
                                    }
                                    if(Build.VERSION.SDK_INT>=26)startForegroundService(i) else startService(i)
                                }
                                WidgetUpdater.refresh(this)
                            }.onSuccess{render()}.onFailure{toast(it.message ?: "Error")}
                        }.show()
                })
            }
            cell.addView(Ui.button(this,label("重命名","Rename","重新命名","名前を変更")) {
                nameDialog(label("存档名称","Preset name","存檔名稱","名前"),preset.name){ title ->
                    runCatching{profiles.rename(preset.id,title)}.onSuccess{render()}
                        .onFailure{toast(it.message ?: "Error")}
                }
            })
            if(!active) {
                cell.addView(Ui.button(this,label("删除此档","Delete preset","刪除此檔","削除")) {
                    AlertDialog.Builder(this)
                        .setTitle(label("永久删除存档？","Permanently delete preset?"))
                        .setNegativeButton(label("取消","Cancel"),null)
                        .setPositiveButton(label("删除","Delete")){_,_->
                            runCatching{profiles.delete(preset.id)}.onSuccess{render()}
                                .onFailure{toast(it.message ?: "Error")}
                        }.show()
                })
            }
            root.addView(cell,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(10)})
        }
        page=ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(Appearance.palette(this@ProfilesActivity).bg)
            addView(root)
        }
        setContentView(page)
        page.post{page.scrollTo(0,old)}
    }

    private fun nameDialog(title:String,initial:String,action:(String)->Unit) {
        val input=EditText(this).apply{
            setSingleLine(true);setText(initial);setSelection(text.length);filters=arrayOf(android.text.InputFilter.LengthFilter(24))
        }
        val wrapper=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(22),0,dp(22),0)
            addView(input)
        }
        val dialog=AlertDialog.Builder(this).setTitle(title).setView(wrapper)
            .setNegativeButton(label("取消","Cancel"),null)
            .setPositiveButton(label("保存","Save"),null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name=input.text.toString().trim()
                if(name.isEmpty())input.error=label("不能留空","Name required")
                else {dialog.dismiss();action(name)}
            }
        }
        dialog.show()
    }
    private fun dp(px:Int)=Ui.dp(this,px)
    override fun onPause(){
        if(::page.isInitialized)settings.saveScroll("profiles",page.scrollY)
        super.onPause()
    }
}
