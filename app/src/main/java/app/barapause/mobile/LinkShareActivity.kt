package app.barapause.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns

/** Narrow exported share target. Never opens shared URLs automatically. */
class LinkShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent?.action==Intent.ACTION_SEND && intent.type=="text/plain") {
            val raw=intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.take(4096).orEmpty()
            val matcher=Patterns.WEB_URL.matcher(raw)
            var url:String?=null
            while(matcher.find()) {
                val candidate=matcher.group()?.trimEnd('.',',',';',')',']')
                if(candidate!=null && candidate.startsWith("https://",true)) {
                    url=candidate.take(2048)
                    break
                }
            }
            startActivity(Intent(this,ContentLinkActivity::class.java).apply {
                putExtra(ContentLinkActivity.EXTRA_SHARED_URL,url ?: "")
            })
        }
        finish()
    }
}
