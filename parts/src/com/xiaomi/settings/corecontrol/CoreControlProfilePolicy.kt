/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.corecontrol

import android.content.Context
import android.os.SystemClock
import androidx.preference.PreferenceManager
import com.xiaomi.settings.thermal.ThermalProfileFragment
import com.xiaomi.settings.utils.FileUtils

object CoreControlProfilePolicy {

    const val PREF_CORE_CONTROL_ENABLED = "core_control_enabled"
    const val PREF_CORE_STATE_CHANGE = "core_control_state_change"

    private const val CPU_ONLINE_PATH = "/sys/devices/system/cpu/cpu"
    private const val MAX_ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 25L

    fun applyForProfile(context: Context, profile: Int) {
        if (profile == ThermalProfileFragment.THERMAL_PROFILE_MBATTERY) {
            setAllCoresOnlineInternal()
            setCoreOnline(7, false)
            setCoreOnline(6, false)
        } else {
            setAllCoresOnlineInternal()
        }

        notifyCoreStateChanged(context)
    }

    fun setAllCoresOnline(context: Context) {
        setAllCoresOnlineInternal()
        notifyCoreStateChanged(context)
    }

    private fun setAllCoresOnlineInternal() {
        (0..7).forEach { cpu ->
            setCoreOnline(cpu, true)
        }
    }

    fun setCoreOnline(cpu: Int, online: Boolean): Boolean {
        val path = "$CPU_ONLINE_PATH$cpu/online"

        if (!FileUtils.fileExists(path)) {
            return false
        }

        val target = if (online) 1 else 0

        repeat(MAX_ATTEMPTS) {
            if (readCoreState(path) == target) {
                return true
            }

            FileUtils.writeLine(path, target.toString())
            SystemClock.sleep(RETRY_DELAY_MS)

            if (readCoreState(path) == target) {
                return true
            }
        }

        return readCoreState(path) == target
    }

    fun isCoreOnline(cpu: Int): Boolean {
        val path = "$CPU_ONLINE_PATH$cpu/online"
        return readCoreState(path) != 0
    }

    private fun readCoreState(path: String): Int? =
        FileUtils.readOneLine(path)?.trim()?.toIntOrNull()

    private fun notifyCoreStateChanged(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val nextValue = prefs.getLong(PREF_CORE_STATE_CHANGE, 0L) + 1L

        prefs.edit()
            .putLong(PREF_CORE_STATE_CHANGE, nextValue)
            .apply()
    }
}
