/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceViewHolder
import com.android.settingslib.widget.SelectorWithWidgetPreference
import java.util.WeakHashMap

class CardSelectorPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SelectorWithWidgetPreference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.isDividerAllowedAbove = false
        holder.isDividerAllowedBelow = false

        val itemView = holder.itemView
        val viewContext = itemView.context

        val savedPadding = paddingMap.getOrPut(itemView) {
            intArrayOf(
                itemView.paddingStart,
                itemView.paddingTop,
                itemView.paddingEnd,
                itemView.paddingBottom
            )
        }

        // Move the RadioButton container from the left (start) to the right (end) once per view
        moveRadioButtonToRight(itemView)

        val isTop = key == "thermal_profile_default" || key == "turbo_mode_18w"
        val isBottom = key == "thermal_profile_game" || key == "turbo_mode_67w"

        val density = viewContext.resources.displayMetrics.density
        val outerRadius = 28f * density
        val innerRadius = 4f * density

        val positionType = when {
            isTop -> 1
            isBottom -> 2
            else -> 0
        }

        val isNight = (viewContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val cacheKey = (positionType shl 4) or (if (isNight) 1 else 0)

        val cachedDrawable = if (drawableKeyMap[itemView] == cacheKey && drawableMap[itemView] != null) {
            drawableMap[itemView]!!
        } else {
            createCardDrawable(viewContext, isNight, isTop, isBottom, outerRadius, innerRadius).also {
                drawableMap[itemView] = it
                drawableKeyMap[itemView] = cacheKey
            }
        }

        if (itemView.background !== cachedDrawable) {
            itemView.background = cachedDrawable
        }

        itemView.setPaddingRelative(
            savedPadding[0],
            savedPadding[1],
            savedPadding[2],
            savedPadding[3]
        )
        itemView.clipToOutline = true

        val gap = (1 * density).toInt()
        val edgeMargin = (4 * density).toInt()

        val lp = itemView.layoutParams as? ViewGroup.MarginLayoutParams
        if (lp != null) {
            val savedHorizMargin = marginMap.getOrPut(itemView) {
                val fallback = (16 * density).toInt()
                val start = if (lp.marginStart > 0) lp.marginStart else fallback
                val end = if (lp.marginEnd > 0) lp.marginEnd else fallback
                intArrayOf(start, end)
            }

            lp.marginStart = savedHorizMargin[0]
            lp.marginEnd = savedHorizMargin[1]
            lp.topMargin = if (isTop) edgeMargin else gap
            lp.bottomMargin = if (isBottom) edgeMargin else gap
        }
    }

    private fun createCardDrawable(
        ctx: Context,
        isNight: Boolean,
        isTop: Boolean,
        isBottom: Boolean,
        outerRadius: Float,
        innerRadius: Float
    ): Drawable {
        val cardColor = if (isNight) {
            ContextCompat.getColor(ctx, android.R.color.system_neutral1_800)
        } else {
            // In Light Mode, if the window background is light/white, use system_neutral2_100
            // (or system_neutral1_0 if window background is darker than neutral2_100)
            val winBg = resolveColorAttr(ctx, android.R.attr.colorBackground)
            val neutral0 = ContextCompat.getColor(ctx, android.R.color.system_neutral1_0)
            val neutral2_50 = ContextCompat.getColor(ctx, android.R.color.system_neutral2_50)
            val neutral2_100 = ContextCompat.getColor(ctx, android.R.color.system_neutral2_100)
            when (winBg) {
                neutral0 -> neutral2_50
                neutral2_50 -> neutral0
                else -> if (winBg == neutral2_100) neutral0 else neutral2_100
            }
        }

        val topR = if (isTop) outerRadius else innerRadius
        val bottomR = if (isBottom) outerRadius else innerRadius

        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(cardColor)
            cornerRadii = floatArrayOf(
                topR, topR,
                topR, topR,
                bottomR, bottomR,
                bottomR, bottomR
            )
        }

        val rippleColor = resolveColorAttr(ctx, android.R.attr.colorControlHighlight)
        return RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null)
    }

    private fun resolveColorAttr(ctx: Context, attr: Int): Int {
        val tv = TypedValue()
        return if (ctx.theme.resolveAttribute(attr, tv, true)) {
            if (tv.resourceId != 0) ContextCompat.getColor(ctx, tv.resourceId) else tv.data
        } else {
            0x1F888888
        }
    }

    private fun moveRadioButtonToRight(itemView: View) {
        if (movedMap[itemView] == true) return

        val radioButton = findRadioButton(itemView) ?: return
        val rowLayout = findHorizontalRow(itemView, radioButton) ?: return

        var radioTopContainer: View = radioButton
        while (radioTopContainer.parent != null && radioTopContainer.parent != rowLayout) {
            radioTopContainer = radioTopContainer.parent as View
        }

        if (radioTopContainer.parent == rowLayout) {
            rowLayout.removeView(radioTopContainer)
            val lp = (radioTopContainer.layoutParams as? LinearLayout.LayoutParams)
                ?: LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            lp.gravity = Gravity.CENTER_VERTICAL
            lp.marginStart = (12 * itemView.context.resources.displayMetrics.density).toInt()
            lp.marginEnd = 0
            radioTopContainer.layoutParams = lp
            rowLayout.addView(radioTopContainer)
            movedMap[itemView] = true
        }
    }

    private fun findRadioButton(view: View): CompoundButton? {
        if (view is CompoundButton) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findRadioButton(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private fun findHorizontalRow(root: View, target: View): LinearLayout? {
        var current: View? = target.parent as? View
        var horizontalParent: LinearLayout? = null
        while (current != null) {
            if (current is LinearLayout && current.orientation == LinearLayout.HORIZONTAL) {
                horizontalParent = current
            }
            if (current == root) break
            current = current.parent as? View
        }
        return horizontalParent
    }

    companion object {
        private val paddingMap = WeakHashMap<View, IntArray>()
        private val marginMap = WeakHashMap<View, IntArray>()
        private val drawableMap = WeakHashMap<View, Drawable>()
        private val drawableKeyMap = WeakHashMap<View, Int>()
        private val movedMap = WeakHashMap<View, Boolean>()
    }
}
