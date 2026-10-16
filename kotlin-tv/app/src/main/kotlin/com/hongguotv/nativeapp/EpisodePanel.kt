// SPDX-License-Identifier: GPL-3.0-only
package com.hongguotv.nativeapp

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.*

/** A remote-accessible side panel; selecting a number is the only action that changes episodes. */
class EpisodePanel(
    private val activity: Activity,private val count: Int,private val current: Int,
    private val locate: (Int,View,(Int)->Unit)->Unit,
    private val select: (Int)->Unit,private val closed: ()->Unit
) {
    private val dialog=Dialog(activity)
    private var group=current/20
    private var focused=current
    private fun dp(n: Int)=(n*activity.resources.displayMetrics.density).toInt()
    private fun label(value: String,size: Float=17f)=TextView(activity).apply { text=value; textSize=size; setTextColor(TvStyle.text); setPadding(dp(8),dp(6),dp(8),dp(6)) }
    private fun button(value: String,active: Boolean=false,action: ()->Unit)=label(value,16f).apply {
        id=View.generateViewId(); gravity=Gravity.CENTER; minHeight=dp(46); isFocusable=true; isFocusableInTouchMode=true
        fun paint(focus: Boolean) { background=GradientDrawable().apply { setColor(if(focus) TvStyle.accent else if(active) Color.rgb(255,45,85) else Color.argb(30,255,255,255)); cornerRadius=dp(10).toFloat() } }
        paint(false); setOnFocusChangeListener { _,hasFocus -> paint(hasFocus) }; setOnClickListener { action() }
    }
    fun show() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnDismissListener { closed() }
        render(); dialog.show()
        val isPortrait=activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.65f)
            if(isPortrait) {
                setGravity(Gravity.BOTTOM)
                attributes=attributes.apply { x=0; y=0 }
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(activity.resources.displayMetrics.heightPixels*.72f).toInt())
            } else {
                setGravity(Gravity.END or Gravity.CENTER_VERTICAL)
                attributes=attributes.apply { x=(activity.resources.displayMetrics.widthPixels*.05f).toInt() }
                setLayout(minOf(dp(520),(activity.resources.displayMetrics.widthPixels*.8f).toInt()),(activity.resources.displayMetrics.heightPixels*.9f).toInt())
            }
        }
    }
    fun dismiss()=dialog.dismiss()
    private fun render() {
        val isPortrait=activity.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT
        val content=LinearLayout(activity).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(16),dp(12),dp(16),dp(14))
            background=GradientDrawable().apply {
                setColor(Color.rgb(13,19,34))
                if(isPortrait) cornerRadii=floatArrayOf(dp(20).toFloat(),dp(20).toFloat(),dp(20).toFloat(),dp(20).toFloat(),0f,0f,0f,0f)
                else cornerRadius=dp(12).toFloat()
            }
        }
        if(isPortrait) {
            val handle=View(activity).apply {
                background=GradientDrawable().apply { setColor(Color.argb(70,255,255,255)); cornerRadius=dp(3).toFloat() }
            }
            content.addView(handle,LinearLayout.LayoutParams(dp(40),dp(4)).apply { gravity=Gravity.CENTER_HORIZONTAL; bottomMargin=dp(10) })
        }
        val header=LinearLayout(activity).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        header.addView(label("选集 (共 $count 集)",17f).apply { setTypeface(null,Typeface.BOLD) },LinearLayout.LayoutParams(0,-2,1f))
        val closeBtn=label("✕",18f).apply {
            setPadding(dp(12),dp(6),dp(12),dp(6))
            gravity=Gravity.CENTER
            setTextColor(Color.rgb(148,163,184))
            setOnClickListener { dismiss() }
        }
        header.addView(closeBtn)
        content.addView(header)

        if(isPortrait && count>20) {
            val tabsScroll=HorizontalScrollView(activity).apply {
                isHorizontalScrollBarEnabled=false
                setPadding(0,dp(6),0,dp(8))
            }
            val tabsRow=LinearLayout(activity).apply { orientation=LinearLayout.HORIZONTAL }
            val totalGroups=(count+19)/20
            for(g in 0 until totalGroups) {
                val startEp=g*20+1
                val endEp=minOf((g+1)*20,count)
                val isCurGroup=(g==group)
                val pill=TextView(activity).apply {
                    text="$startEp-$endEp"
                    textSize=12f
                    setTypeface(null,if(isCurGroup) Typeface.BOLD else Typeface.NORMAL)
                    setTextColor(if(isCurGroup) Color.WHITE else Color.rgb(180,190,210))
                    gravity=Gravity.CENTER
                    setPadding(dp(14),dp(6),dp(14),dp(6))
                    background=GradientDrawable().apply {
                        cornerRadius=dp(16).toFloat()
                        setColor(if(isCurGroup) Color.rgb(255,45,85) else Color.argb(35,255,255,255))
                    }
                    setOnClickListener {
                        group=g
                        focused=g*20
                        render()
                    }
                }
                tabsRow.addView(pill,LinearLayout.LayoutParams(-2,-2).apply { rightMargin=dp(8) })
            }
            tabsScroll.addView(tabsRow)
            content.addView(tabsScroll)
        } else if(!isPortrait) {
            val actions=LinearLayout(activity).apply { setPadding(0,dp(6),0,dp(6)) }
            val previous=button("‹ 上一组") { group--; focused=group*20; render() }.apply { isEnabled=group>0; isFocusable=isEnabled; alpha=if(isEnabled) 1f else .4f }
            val next=button("下一组 ›") { group++; focused=group*20; render() }.apply { isEnabled=(group+1)*20<count; isFocusable=isEnabled; alpha=if(isEnabled) 1f else .4f }
            lateinit var jump: TextView
            jump=button("跳转") { locate(focused,jump) { target -> focused=target; group=target/20; render() } }
            listOf(previous,next,jump).forEach { actions.addView(it,LinearLayout.LayoutParams(0,-2,1f).apply { setMargins(dp(3),dp(3),dp(3),dp(3)) }) }
            content.addView(actions)
        }

        content.addView(label("第 ${group*20+1}—${minOf((group+1)*20,count)} 集 · 当前播放第 ${current+1} 集",12f).apply { setTextColor(Color.rgb(148,163,184)); setPadding(dp(8),dp(2),dp(8),dp(6)) })
        val scroll=ScrollView(activity).apply { isVerticalScrollBarEnabled=false }
        val grid=LinearLayout(activity).apply { orientation=LinearLayout.VERTICAL }
        scroll.addView(grid)
        content.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))

        val cells=mutableListOf<TextView>()
        (group*20 until minOf((group+1)*20,count)).toList().chunked(5).forEach { indexes ->
            val row=LinearLayout(activity)
            indexes.forEach { index ->
                val cell=button("${index+1}",index==current) {
                    select(index)
                    dismiss()
                }
                cell.contentDescription="第 ${index+1} 集"+(if(index==current) "，当前播放" else "")
                if(index==current) {
                    cell.background=GradientDrawable().apply { setColor(Color.rgb(255,45,85)); cornerRadius=dp(10).toFloat() }
                    cell.setTextColor(Color.WHITE)
                    cell.setTypeface(null,Typeface.BOLD)
                } else {
                    cell.background=GradientDrawable().apply {
                        setColor(Color.argb(20,255,255,255))
                        setStroke(dp(1),Color.argb(30,255,255,255))
                        cornerRadius=dp(10).toFloat()
                    }
                    cell.setTextColor(Color.rgb(240,243,250))
                }
                row.addView(cell,LinearLayout.LayoutParams(0,-2,1f).apply { setMargins(dp(4),dp(4),dp(4),dp(4)) }); cells+=cell
            }
            repeat(5-indexes.size) { row.addView(Space(activity),LinearLayout.LayoutParams(0,1,1f)) }
            grid.addView(row)
        }
        cells.forEachIndexed { i,cell ->
            cell.nextFocusLeftId=cells[if(i%5==0) i else i-1].id
            cell.nextFocusRightId=cells[if(i%5==4 || i==cells.lastIndex) i else i+1].id
            cell.nextFocusUpId=if(i>=5) cells[i-5].id else cell.id
            cell.nextFocusDownId=cells[if(i+5<cells.size) i+5 else if(i/5<cells.lastIndex/5) cells.lastIndex else i].id
        }
        dialog.setContentView(content)
        cells.getOrNull((focused-group*20).coerceIn(0,cells.lastIndex))?.requestFocus()
    }
}
