package app.barapause.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Narrow exported entry point for user-created launcher shortcuts.
 * Never exposes internal settings or monitoring operations.
 */
class ShortcutEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this,QuickLaunchActivity::class.java).apply {
            action=QuickLaunchActivity.ACTION_WIDGET_SEARCH
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }
}
