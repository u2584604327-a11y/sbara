package app.barapause.mobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Compact, non-fullscreen overlay. It never locks the screen and does not capture touches
 * outside the card. Cross-app display still requires SYSTEM_ALERT_WINDOW.
 */
class OverlayController(private val context: Context) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var root: View? = null
    private var countdown: TextView? = null
    private var extendButton: Button? = null
    private var currentPkg: String? = null

    fun show(
        packageName: String,
        appLabel: String,
        remainingMs: Long,
        canExtend: Boolean,
        extensionSeconds: Int,
        onExtend: () -> Unit
    ) {
        if (!PermissionUtils.hasOverlay(context)) return
        if (root == null || currentPkg != packageName) {
            hide()
            currentPkg = packageName
            root = buildCard(appLabel, extensionSeconds, onExtend).also { card ->
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply { gravity = Gravity.BOTTOM }

                wm.addView(card, params)
                card.alpha = 0f
                card.translationY = dp(96).toFloat()
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(420L)
                    .setInterpolator(OvershootInterpolator(0.8f))
                    .start()
            }
        }
        update(remainingMs, canExtend, extensionSeconds)
    }

    private fun buildCard(appLabel: String, extensionSeconds: Int, onExtend: () -> Unit): View {
        val outer = FrameLayout(context).apply {
            setPadding(dp(14), dp(8), dp(14), dp(18))
        }

        val colors = Appearance.palette(context)
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            elevation = dp(12).toFloat()
            background = rounded(colors.card, 26)
        }

        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mascot = ImageView(context).apply {
            setImageResource(R.drawable.capybara_mascot)
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64))
            contentDescription = "BaraPause 水豚"
        }
        val titleGroup = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val title = TextView(context).apply {
            text = L10n.t(context,"break_title")
            textSize = 21f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colors.text)
        }
        val subtitle = TextView(context).apply {
            text = appLabel + L10n.t(context,"break_desc")
            textSize = 14.5f
            setTextColor(colors.muted)
            setPadding(0, dp(3), 0, 0)
        }
        titleGroup.addView(title)
        titleGroup.addView(subtitle)
        header.addView(mascot)
        header.addView(titleGroup)

        countdown = TextView(context).apply {
            textSize = 29f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colors.accent)
            setPadding(0, dp(10), 0, dp(8))
        }

        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        extendButton = Button(context).apply {
            text = "再用 $extensionMinutes 分钟"
            isAllCaps = false
            backgroundTintList = android.content.res.ColorStateList.valueOf(colors.button)
            setTextColor(colors.text)
            setOnClickListener { onExtend() }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(6)
            }
        }
        val leaveButton = Button(context).apply {
            text = L10n.t(context,"leave")
            isAllCaps = false
            backgroundTintList = android.content.res.ColorStateList.valueOf(colors.accent)
            setTextColor(Color.WHITE)
            setOnClickListener {
                hide()
                val i = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(i)
            }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(6)
            }
        }
        actions.addView(extendButton)
        actions.addView(leaveButton)

        val note = TextView(context).apply {
            text = L10n.t(context,"overlay_note")
            textSize = 12.5f
            gravity = Gravity.CENTER
            setTextColor(colors.muted)
            setPadding(0, dp(6), 0, 0)
        }

        card.addView(header)
        card.addView(countdown)
        card.addView(actions)
        card.addView(note)
        outer.addView(card)
        return outer
    }

    fun update(remainingMs: Long, canExtend: Boolean, extensionSeconds: Int) {
        val totalSec = (remainingMs.coerceAtLeast(0L) + 999L) / 1000L
        val min = totalSec / 60
        val sec = totalSec % 60
        countdown?.text = L10n.t(context,"rest") + " %02d:%02d".format(min, sec)
        extendButton?.apply {
            visibility = if (canExtend) View.VISIBLE else View.GONE
            text = "再用 $extensionMinutes 分钟"
        }
    }

    fun hide() {
        root?.let { view ->
            runCatching {
                view.animate().cancel()
                wm.removeView(view)
            }
        }
        root = null
        countdown = null
        extendButton = null
        currentPkg = null
    }

    private fun duration(context: Context, seconds: Int): String =
        if (seconds % 60 == 0) "${seconds / 60} " + L10n.t(context, "minute") else "$seconds " + L10n.t(context, "seconds")

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        setStroke(dp(1), Appearance.palette(context).stroke)
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
