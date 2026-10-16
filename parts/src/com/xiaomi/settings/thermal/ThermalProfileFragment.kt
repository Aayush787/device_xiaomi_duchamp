/*
 * Copyright (C) 2024 Paranoid Android
 * Copyright (C) 2026 Luxured
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.thermal

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SelectorWithWidgetPreference
import com.xiaomi.settings.R
import com.xiaomi.settings.utils.FileUtils

class ThermalProfileFragment : PreferenceFragment(),
    SelectorWithWidgetPreference.OnClickListener,
    Preference.OnPreferenceChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mSwitchAutoMode: SwitchPreferenceCompat
    private val mProfilePrefs = mutableMapOf<Int, SelectorWithWidgetPreference>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.thermal_profile_settings, rootKey)

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)

        val profileKeys = mapOf(
            THERMAL_PROFILE_DEFAULT      to KEY_PROFILE_DEFAULT,
            THERMAL_PROFILE_MPERFORMANCE to KEY_PROFILE_PERFORMANCE,
            THERMAL_PROFILE_MBATTERY     to KEY_PROFILE_BATTERY,
            THERMAL_PROFILE_MGAME        to KEY_PROFILE_GAME
        )

        profileKeys.forEach { (profile, key) ->
            findPreference<SelectorWithWidgetPreference>(key)?.let { pref ->
                pref.setOnClickListener(this)
                mProfilePrefs[profile] = pref
            }
        }

        mSwitchAutoMode = findPreference(PREF_AUTO_MODE)!!
        mSwitchAutoMode.isChecked = mSharedPrefs.getBoolean(PREF_AUTO_MODE, false)
        mSwitchAutoMode.onPreferenceChangeListener = this

        syncFromSysfs()
    }

    override fun onResume() {
        super.onResume()
        syncFromSysfs()
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onRadioButtonClicked(emiter: SelectorWithWidgetPreference) {
        val selectedProfile = mProfilePrefs.entries.find { it.value == emiter }?.key
            ?: THERMAL_PROFILE_DEFAULT
        selectProfile(selectedProfile)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference == mSwitchAutoMode) {
            val checked = newValue as Boolean
            val serviceIntent = Intent(activity, ThermalAutoModeService::class.java)
            if (checked) activity.startService(serviceIntent)
            else         activity.stopService(serviceIntent)
            return true
        }
        return false
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (key == PREF_THERMAL_PROFILE) {
            val profile = prefs.getString(PREF_THERMAL_PROFILE, THERMAL_PROFILE_DEFAULT.toString())
                ?.toIntOrNull() ?: THERMAL_PROFILE_DEFAULT
            updateUi(profile)
        }
    }

    private fun selectProfile(profile: Int) {
        FileUtils.writeLine(THERMAL_PROFILE_PATH, profile)
        mSharedPrefs.edit().putString(PREF_THERMAL_PROFILE, profile.toString()).apply()
        updateUi(profile)
    }

    private fun syncFromSysfs() {
        val current = FileUtils.readLineInt(THERMAL_PROFILE_PATH)
        if (mSharedPrefs.getString(PREF_THERMAL_PROFILE, null) != current.toString()) {
            mSharedPrefs.edit().putString(PREF_THERMAL_PROFILE, current.toString()).apply()
        }
        updateUi(current)
    }

    private fun updateUi(activeProfile: Int) {
        mProfilePrefs.forEach { (profile, pref) ->
            pref.isChecked = (profile == activeProfile)
        }
    }

    companion object {
        const val THERMAL_PROFILE_PATH         = "/sys/class/thermal/thermal_message/sconfig"
        const val PREF_THERMAL_PROFILE         = "thermal_profile"
        const val PREF_AUTO_MODE               = "thermal_auto_mode"
        const val THERMAL_PROFILE_DEFAULT      = 50
        const val THERMAL_PROFILE_MPERFORMANCE = 18
        const val THERMAL_PROFILE_MBATTERY     = 700
        const val THERMAL_PROFILE_MGAME        = 19

        private const val KEY_PROFILE_DEFAULT     = "thermal_profile_default"
        private const val KEY_PROFILE_PERFORMANCE = "thermal_profile_performance"
        private const val KEY_PROFILE_BATTERY     = "thermal_profile_battery"
        private const val KEY_PROFILE_GAME        = "thermal_profile_game"
    }
}
