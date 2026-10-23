package app.barapause.mobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

/**
 * Inspired by advertisement landing-page routing, but accepts only user-supplied HTTPS links.
 * Never deserializes intent://, javascript:, file:, content: or arbitrary app URI schemes.
 */
class ContentLinkActivity : Activity() {
    private lateinit var entry:EditText
    private lateinit var prefs:Prefs

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        prefs=Prefs(this)
        Appearance.applyWindow(this)
        val root=Ui.column(this)
        root.addView(Ui.navHeader(this,L10n.t(this,"links_title")))
        root.addView(Ui.text(this,L10n.t(this,"links_desc"),14f))
        entry=EditText(this).apply {
            hint="https://example.com/content/123"
            inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(false)
            minLines=2
            maxLines=3
            setText(prefs.savedContentLink)
            setTextColor(Appearance.palette(this@ContentLinkActivity).text)
            background=Ui.cardBackground(this@ContentLinkActivity)
            setPadding(Ui.dp(this@ContentLinkActivity,14),Ui.dp(this@ContentLinkActivity,12),Ui.dp(this@ContentLinkActivity,14),Ui.dp(this@ContentLinkActivity,12))
        }
        root.addView(entry)
        root.addView(Ui.button(this,L10n.t(this,"links_open")) { openLink() })
        root.addView(Ui.button(this,L10n.t(this,"links_clear")) {
            entry.setText("")
            prefs.savedContentLink=""
        })
        root.addView(Ui.text(this,L10n.t(this,"links_warning"),13f))
        setContentView(ScrollView(this).apply {
            isFillViewport=true
            setBackgroundColor(Appearance.palette(this@ContentLinkActivity).bg)
            addView(root)
        })
    }

    private fun openLink() {
        val raw=entry.text.toString().trim()
        val link=runCatching { Uri.parse(raw) }.getOrNull()
        val host=link?.host
        if(raw.length>2048 || link?.scheme?.lowercase()!="https" ||
            host.isNullOrBlank() || link.userInfo()!=null ||
            raw.any { it=='\n' || it=='\r' } || host.contains(" ")) {
            Toast.makeText(this,L10n.t(this,"links_invalid"),Toast.LENGTH_LONG).show()
            return
        }
        val checked=link!!
        AlertDialog.Builder(this).setTitle(L10n.t(this,"links_confirm"))
            .setMessage(host+"\n"+L10n.t(this,"links_confirm_desc"))
            .setNegativeButton(L10n.t(this,"cancel"),null)
            .setPositiveButton(L10n.t(this,"links_open")) { _,_ ->
                val linkIntent=Intent(Intent.ACTION_VIEW,checked).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
                val success=runCatching { startActivity(linkIntent) }.isSuccess
                if(success) prefs.savedContentLink=raw
                else Toast.makeText(this,L10n.t(this,"links_unavailable"),Toast.LENGTH_LONG).show()
            }.show()
    }

    private fun Uri.userInfo():String? =
        runCatching { java.net.URI(toString()).userInfo }.getOrNull()
}
