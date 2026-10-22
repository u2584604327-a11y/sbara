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

/** Non-fullscreen TYPE_APPLICATION_OVERLAY. Does not intercept outside-card touches. */
class OverlayController(private val context: Context) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val preferences = Prefs(context)
    private var root: View? = null
    private var countdown: TextView? = null
    private var extendButton: Button? = null
    private var currentPkg: String? = null

    fun show(packageName: String, appLabel: String, remainingMs: Long, canExtend: Boolean,
             extensionSeconds: Int, onExtend: () -> Unit) {
        if (!PermissionUtils.hasOverlay(context)) return
        if (root == null || currentPkg != packageName) {
            hide()
            currentPkg = packageName
            val card = buildCard(appLabel,extensionSeconds,onExtend,false)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = when(preferences.popupPosition) {
                    "top" -> Gravity.TOP
                    "middle" -> Gravity.CENTER
                    else -> Gravity.BOTTOM
                }
                if (preferences.popupPosition == "top") y=dp(32)
            }
            wm.addView(card,params)
            root=card
            animateEntrance(card)
        }
        update(remainingMs,canExtend,extensionSeconds)
    }

    /** The very same card renderer is reused for in-app preview; buttons are hidden. */
    fun previewCard(appLabel:String):View {
        val view=buildCard(appLabel,preferences.extensionSeconds,{},true)
        countdown?.text=L10n.t(context,"rest")+" 04:59"
        return view
    }

    private fun animateEntrance(v:View) {
        val finalAlpha=preferences.popupOpacity/100f
        when(preferences.popupAnimation) {
            "none" -> v.alpha=finalAlpha
            "fade" -> {
                v.alpha=0f
                v.animate().alpha(finalAlpha).setDuration(240).start()
            }
            else -> {
                v.alpha=0f
                v.translationY=dp(if(preferences.popupPosition=="top") -64 else 64).toFloat()
                v.animate().alpha(finalAlpha).translationY(0f).setDuration(360)
                    .setInterpolator(OvershootInterpolator(.65f)).start()
            }
        }
    }

    private fun buildCard(appLabel:String,extensionSeconds:Int,onExtend:()->Unit,preview:Boolean):View {
        val palette=Appearance.palette(context)
        val (surface,border) = when(preferences.popupColor) {
            "peach" -> if(palette.bg==Color.rgb(24,27,27)) Color.rgb(77,53,48) to Color.rgb(179,115,101) else Color.rgb(255,234,220) to Color.rgb(223,173,147)
            "lavender" -> if(palette.bg==Color.rgb(24,27,27)) Color.rgb(58,50,77) to Color.rgb(140,126,186) else Color.rgb(242,234,255) to Color.rgb(194,176,224)
            else -> palette.card to palette.stroke
        }
        val compact=preferences.popupSize=="compact"
        val outer=FrameLayout(context).apply {
            setPadding(dp(14),dp(6),dp(14),dp(12))
            if(preview) alpha=preferences.popupOpacity/100f
        }
        val card=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(16),dp(if(compact) 8 else 15),dp(16),dp(if(compact) 10 else 15))
            elevation=dp(12).toFloat()
            background=GradientDrawable().apply {
                setColor(surface)
                cornerRadius=dp(24).toFloat()
                setStroke(dp(1),border)
            }
        }
        val row=LinearLayout(context).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL }
        if(preferences.popupMascot) {
            row.addView(ImageView(context).apply {
                setImageResource(R.drawable.capybara_mascot)
                layoutParams=LinearLayout.LayoutParams(dp(if(compact) 44 else 64),dp(if(compact) 44 else 64))
                contentDescription="BaraPause"
            })
        }
        val labels=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(10),0,0,0)
        }
        labels.addView(TextView(context).apply {
            text=preferences.popupTitle.ifBlank { L10n.t(context,"break_title") }
            textSize=if(compact) 17f else 21f
            maxLines=2
            setTypeface(typeface,Typeface.BOLD)
            setTextColor(palette.text)
        })
        labels.addView(TextView(context).apply {
            text=preferences.popupBody.ifBlank { appLabel+L10n.t(context,"break_desc") }
                .replace("{app}",appLabel)
            textSize=if(compact) 12f else 14f
            maxLines=3
            setTextColor(palette.muted)
        })
        row.addView(labels,LinearLayout.LayoutParams(0,-2,1f))
        card.addView(row)

        countdown=TextView(context).apply {
            textSize=if(compact) 23f else 28f
            gravity=Gravity.CENTER
            setTypeface(typeface,Typeface.BOLD)
            setTextColor(palette.accent)
            setPadding(0,dp(if(compact) 3 else 10),0,dp(if(compact) 3 else 8))
        }
        card.addView(countdown)

        val actions=LinearLayout(context).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER }
        extendButton=Button(context).apply {
            text=L10n.t(context,"extend")+" "+duration(extensionSeconds)
            isAllCaps=false
            backgroundTintList=android.content.res.ColorStateList.valueOf(palette.button)
            setTextColor(palette.text)
            setOnClickListener { onExtend() }
        }
        val leave=Button(context).apply {
            text=L10n.t(context,"leave")
            isAllCaps=false
            backgroundTintList=android.content.res.ColorStateList.valueOf(palette.accent)
            setTextColor(Color.WHITE)
            setOnClickListener {
                hide()
                context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags=Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
        actions.addView(extendButton,LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(4)})
        actions.addView(leave,LinearLayout.LayoutParams(0,-2,1f).apply{marginStart=dp(4)})
        actions.visibility=if(preview) View.GONE else View.VISIBLE
        card.addView(actions)
        if(!compact) {
            card.addView(TextView(context).apply {
                text=L10n.t(context,"overlay_note")
                textSize=12f
                gravity=Gravity.CENTER
                setTextColor(palette.muted)
                setPadding(0,dp(5),0,0)
            })
        }
        outer.addView(card)
        return outer
    }

    fun update(remainingMs:Long,canExtend:Boolean,extensionSeconds:Int) {
        val t=(remainingMs.coerceAtLeast(0L)+999L)/1000L
        countdown?.text=L10n.t(context,"rest")+" %02d:%02d".format(t/60,t%60)
        extendButton?.apply {
            visibility=if(canExtend) View.VISIBLE else View.GONE
            text=L10n.t(context,"extend")+" "+duration(extensionSeconds)
        }
    }

    fun hide() {
        root?.let {
            runCatching { it.animate().cancel();wm.removeView(it) }
        }
        root=null;countdown=null;extendButton=null;currentPkg=null
    }

    private fun duration(seconds:Int):String=if(seconds%60==0)
        "${seconds/60} "+L10n.t(context,"minute") else "$seconds "+L10n.t(context,"seconds")
    private fun dp(x:Int):Int=Ui.dp(context,x)
}
