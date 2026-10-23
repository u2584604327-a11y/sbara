package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import org.json.JSONObject

/** Local settings/rules only: Android document picker, no broad storage permission. */
class BackupActivity : Activity() {
    private val exportCode=701
    private val importCode=702
    private lateinit var prefs:Prefs
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        prefs=Prefs(this)
        Appearance.applyWindow(this)
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"backup_title")))
        root.addView(Ui.text(this,L10n.t(this,"backup_explain"),14f))
        root.addView(Ui.button(this,L10n.t(this,"backup_export")) {
            startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="application/json"
                putExtra(Intent.EXTRA_TITLE,"BaraPause-settings.json")
            },exportCode)
        })
        root.addView(Ui.button(this,L10n.t(this,"backup_import")) {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="application/json"
            },importCode)
        })
        root.addView(Ui.text(this,L10n.t(this,"backup_privacy"),13f))
        setContentView(ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(Appearance.palette(this@BackupActivity).bg)
            addView(root)
        })
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK) return
        val uri=data?.data ?: return
        when(requestCode) {
            exportCode -> runCatching {
                contentResolver.openOutputStream(uri,"w")?.use { out ->
                    out.write(prefs.exportPortableSettings().toString(2).toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Cannot write file")
            }.onSuccess {
                Toast.makeText(this,L10n.t(this,"backup_saved"),Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this,it.message ?: it.javaClass.simpleName,Toast.LENGTH_LONG).show()
            }
            importCode -> importFrom(uri)
        }
    }

    private fun importFrom(uri:Uri) {
        val candidate=runCatching {
            // Hard cap at 1 MiB. Do not allocate arbitrarily large third-party documents.
            val stream=contentResolver.openInputStream(uri) ?: error("Cannot read file")
            val data=stream.use { input ->
                val buffer=ByteArray(8192)
                val output=java.io.ByteArrayOutputStream()
                while(true) {
                    val n=input.read(buffer)
                    if(n<0) break
                    if(n==0) continue
                    require(output.size()+n<=1_048_576) { "File exceeds 1 MiB" }
                    output.write(buffer,0,n)
                }
                output.toByteArray()
            }
            JSONObject(String(data,Charsets.UTF_8))
        }.getOrElse {
            Toast.makeText(this,it.message ?: it.javaClass.simpleName,Toast.LENGTH_LONG).show()
            return
        }
        if(candidate.optString("schema")!="barapause-settings/1") {
            Toast.makeText(this,L10n.t(this,"backup_invalid"),Toast.LENGTH_LONG).show()
            return
        }
        val count=candidate.optJSONArray("rules")?.length() ?: 0
        AlertDialog.Builder(this)
            .setTitle(L10n.t(this,"backup_confirm"))
            .setMessage("$count "+L10n.t(this,"backup_rule_count")+"\n"+L10n.t(this,"backup_merge"))
            .setNegativeButton(L10n.t(this,"cancel"),null)
            .setPositiveButton(L10n.t(this,"save")) { _,_ ->
                runCatching { prefs.importPortableSettings(candidate) }
                    .onSuccess { n ->
                        WidgetUpdater.refresh(this)
                        Toast.makeText(this,L10n.t(this,"backup_imported")+": $n",Toast.LENGTH_LONG).show()
                    }.onFailure {
                        Toast.makeText(this,it.message ?: L10n.t(this,"backup_invalid"),Toast.LENGTH_LONG).show()
                    }
            }.show()
    }
}
