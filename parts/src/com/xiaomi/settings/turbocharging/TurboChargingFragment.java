/*
 * Copyright (C) 2025 LineageOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.settings.turbocharging;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.SelectorWithWidgetPreference;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.xiaomi.settings.R;

public class TurboChargingFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener,
        SelectorWithWidgetPreference.OnClickListener {

    private static final String TAG = "TurboChargingFragment";
    private static final String CHARGE_CURRENT_FILE = "/sys/class/power_supply/battery/constant_charge_current";
    private static final String USB_ONLINE_FILE = "/sys/class/power_supply/usb/online";
    private static final String PREF_TURBO_ENABLED = "turbo_enable";
    private static final String PREF_TURBO_CURRENT = "turbo_current";

    private static final String KEY_CATEGORY_CURRENT = "turbo_current_category";
    private static final String KEY_MODE_18W = "turbo_mode_18w";
    private static final String KEY_MODE_30W = "turbo_mode_30w";
    private static final String KEY_MODE_67W = "turbo_mode_67w";

    private static final String VAL_18W = "4500000";
    private static final String VAL_30W = "7000000";
    private static final String VAL_67W = "22000000";

    private MainSwitchPreference mTurboEnabled;
    private PreferenceCategory mCurrentCategory;
    private SelectorWithWidgetPreference mMode18W;
    private SelectorWithWidgetPreference mMode30W;
    private SelectorWithWidgetPreference mMode67W;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.charge_panel, rootKey);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mTurboEnabled = (MainSwitchPreference) findPreference(PREF_TURBO_ENABLED);
        mTurboEnabled.setOnPreferenceChangeListener(this);

        mCurrentCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_CURRENT);

        mMode18W = (SelectorWithWidgetPreference) findPreference(KEY_MODE_18W);
        mMode30W = (SelectorWithWidgetPreference) findPreference(KEY_MODE_30W);
        mMode67W = (SelectorWithWidgetPreference) findPreference(KEY_MODE_67W);

        mMode18W.setOnClickListener(this);
        mMode30W.setOnClickListener(this);
        mMode67W.setOnClickListener(this);

        mCurrentCategory.setEnabled(mTurboEnabled.isChecked());

        String currentValue = prefs.getString(PREF_TURBO_CURRENT, VAL_67W);
        updateRadioButtons(currentValue);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTurboEnabled) {
            boolean enabled = (boolean) newValue;
            mCurrentCategory.setEnabled(enabled);
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putBoolean(PREF_TURBO_ENABLED, enabled)
                    .apply();

            // Always update the charge current when the toggle changes
            updateChargeCurrent();

            return true;
        }
        return false;
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference emiter) {
        String value = VAL_67W;
        if (emiter == mMode18W) {
            value = VAL_18W;
        } else if (emiter == mMode30W) {
            value = VAL_30W;
        } else if (emiter == mMode67W) {
            value = VAL_67W;
        }

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(PREF_TURBO_CURRENT, value)
                .apply();

        updateRadioButtons(value);

        // Update the charge current when the option is changed
        updateChargeCurrent();
    }

    private void updateRadioButtons(String currentValue) {
        mMode18W.setChecked(VAL_18W.equals(currentValue));
        mMode30W.setChecked(VAL_30W.equals(currentValue));
        mMode67W.setChecked(VAL_67W.equals(currentValue));
    }

    private void updateChargeCurrent() {
        boolean turboEnabled = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(PREF_TURBO_ENABLED, false);
        Log.i(TAG, "isTurbo=" + turboEnabled);
        String defaultValue = "21000000";
        if (turboEnabled) {
            String currentValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PREF_TURBO_CURRENT, "22000000");
            Log.i(TAG, "currentValue=" + currentValue);
            writeChargeCurrent(currentValue);
        } else {
            writeChargeCurrent(defaultValue);
        }
    }

    private void writeChargeCurrent(String value) {
        try {
            Integer.parseInt(value);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHARGE_CURRENT_FILE))) {
                writer.write(value);
                Log.i(TAG, "Updated Charging current to " + value);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid charge current value: " + value, e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to update charge current", e);
        }
    }
}
