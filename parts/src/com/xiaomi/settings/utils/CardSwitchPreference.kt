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
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import java.util.WeakHashMap

class CardSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwitchPreferenceCompat(context, attrs) {

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

        val density = viewContext.resources.displayMetrics.density
        val radius = 28f * density

        val isNight = (viewContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val nightKey = if (isNight) 1 else 0

        val cachedDrawable = if (nightModeMap[itemView] == nightKey && drawableMap[itemView] != null) {
            drawableMap[itemView]!!
        } else {
            createSingleCardDrawable(viewContext, isNight, radius).also {
                drawableMap[itemView] = it
                nightModeMap[itemView] = nightKey
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

        val marginVertical = (4 * density).toInt()

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
            lp.topMargin = marginVertical
            lp.bottomMargin = marginVertical
        }
    }

    private fun createSingleCardDrawable(
        ctx: Context,
        isNight: Boolean,
        radius: Float
    ): Drawable {
        val cardColor = if (isNight) {
            ContextCompat.getColor(ctx, android.R.color.system_neutral1_800)
        } else {
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

        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(cardColor)
            cornerRadius = radius
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

    companion object {
        private val paddingMap = WeakHashMap<View, IntArray>()
        private val marginMap = WeakHashMap<View, IntArray>()
        private val drawableMap = WeakHashMap<View, Drawable>()
        private val nightModeMap = WeakHashMap<View, Int>()
    }
}
