package app.barapause.mobile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Appearance.applyWindow(this)
        val colors=Appearance.palette(this)
        val root = FrameLayout(this).apply {
            setBackgroundColor(colors.bg)
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val mascot = ImageView(this).apply {
            setImageResource(R.drawable.capybara_mascot)
            layoutParams = LinearLayout.LayoutParams(dp(150), dp(150))
            scaleX = 0.72f
            scaleY = 0.72f
            alpha = 0f
        }
        val title = TextView(this).apply {
            text = "BaraPause"
            textSize = 31f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(colors.text)
            gravity = Gravity.CENTER
            alpha = 0f
            translationY = dp(12).toFloat()
        }
        val subtitle = TextView(this).apply {
            text = L10n.t(this@SplashActivity,"home_desc")
            textSize = 15f
            setTextColor(colors.muted)
            gravity = Gravity.CENTER
            alpha = 0f
            setPadding(0, dp(4), 0, 0)
        }
        column.addView(mascot)
        column.addView(title)
        column.addView(subtitle)
        root.addView(column, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)

        val pop = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(mascot, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(mascot, View.SCALE_X, 0.72f, 1.04f, 1f),
                ObjectAnimator.ofFloat(mascot, View.SCALE_Y, 0.72f, 1.04f, 1f)
            )
            duration = 620L
            interpolator = OvershootInterpolator(0.9f)
        }
        val words = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, dp(12).toFloat(), 0f),
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f)
            )
            duration = 420L
            interpolator = DecelerateInterpolator()
        }
        AnimatorSet().apply {
            playSequentially(pop, words)
            start()
        }

        root.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1250L)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
