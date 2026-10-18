/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.corecontrol

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.FileUtils

class CoreControlFragment : PreferenceFragment(),
    Preference.OnPreferenceChangeListener {

    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mEnableSwitch: SwitchPreferenceCompat
    private val mCorePrefs = linkedMapOf<Int, SwitchPreferenceCompat>()

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.core_control_settings, rootKey)

        mSharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(requireActivity())

        mEnableSwitch = findPreference(PREF_CORE_CONTROL_ENABLED)!!
        mEnableSwitch.isChecked =
            mSharedPrefs.getBoolean(PREF_CORE_CONTROL_ENABLED, false)
        mEnableSwitch.onPreferenceChangeListener = this

        CORE_IDS.forEach { cpu ->
            val pref =
                findPreference<SwitchPreferenceCompat>(coreKey(cpu))!!

            pref.onPreferenceChangeListener = this
            mCorePrefs[cpu] = pref
        }

        syncFromSysfs()
    }

    override fun onResume() {
        super.onResume()
        syncFromSysfs()
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {
        val enabled = newValue as? Boolean ?: return false

        if (preference == mEnableSwitch) {
            if (!enabled) {
                setAllCoresOnline()
            }

            mSharedPrefs.edit()
                .putBoolean(PREF_CORE_CONTROL_ENABLED, enabled)
                .apply()

            updateUi(enabled)
            return true
        }

        val cpu = mCorePrefs.entries
            .firstOrNull { it.value == preference }
            ?.key ?: return false

        if (!mEnableSwitch.isChecked) {
            return false
        }

        if (
            cpu in SMALL_CORE_IDS &&
            !enabled &&
            onlineSmallCoreCount() <= MIN_SMALL_CORES_ONLINE
        ) {
            updateUi(true)
            return false
        }

        if (!setCoreOnline(cpu, enabled)) {
            updateUi(mEnableSwitch.isChecked)
            return false
        }

        updateUi(mEnableSwitch.isChecked)
        return true
    }

    private fun syncFromSysfs() {
        val enabled = mSharedPrefs.getBoolean(
            PREF_CORE_CONTROL_ENABLED,
            false
        )

        mEnableSwitch.isChecked = enabled

        if (!enabled) {
            setAllCoresOnline()
        }

        updateUi(enabled)
    }

    private fun updateUi(controlEnabled: Boolean) {
        val smallOnline = onlineSmallCoreCount()

        mCorePrefs.forEach { (cpu, pref) ->
            val online = isCoreOnline(cpu)
            pref.isChecked = online

            pref.isEnabled = controlEnabled &&
                (
                    cpu !in SMALL_CORE_IDS ||
                    !online ||
                    smallOnline > MIN_SMALL_CORES_ONLINE
                )
        }
    }

    private fun onlineSmallCoreCount(): Int =
        SMALL_CORE_IDS.count(::isCoreOnline)

    private fun setAllCoresOnline() {
        CORE_IDS.forEach { cpu ->
            setCoreOnline(cpu, true)
        }
    }

    private fun setCoreOnline(
        cpu: Int,
        online: Boolean
    ): Boolean {
        val path = coreOnlinePath(cpu)

        if (!FileUtils.fileExists(path)) {
            return online && isCoreOnline(cpu)
        }

        if (!FileUtils.writeLine(path, if (online) "1" else "0")) {
            return false
        }

        return isCoreOnline(cpu) == online
    }

    private fun isCoreOnline(cpu: Int): Boolean {
        return when (
            FileUtils.readOneLine(coreOnlinePath(cpu))?.trim()
        ) {
            "0" -> false
            "1" -> true
            else -> true
        }
    }

    private fun coreOnlinePath(cpu: Int): String =
        "/sys/devices/system/cpu/cpu$cpu/online"

    private fun coreKey(cpu: Int): String =
        "core_cpu$cpu"

    companion object {
        private const val PREF_CORE_CONTROL_ENABLED =
            "core_control_enabled"

        private const val MIN_SMALL_CORES_ONLINE = 2

        private val CORE_IDS = 0..7
        private val SMALL_CORE_IDS = 0..3
    }
}
