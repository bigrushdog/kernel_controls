/*
 * Copyright (C) 2013 Randall Rushing aka Bigrushdog
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

package com.brd.apps.kernelcontrols;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class Performance extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static Performance newInstance() {
        return new Performance();
    }

    public Performance() {
    }

    private static final String CLOCK_CATEGORY = "cpu_clocks";
    private static final String FC_CATEGORY = "eos_settings_fast_charge";
    private static final String S2W_CATEGORY = "eos_settings_sweep2wake";
    private static final String ZRAM_CATEGORY = "eos_settings_zram";

    private CheckBoxPreference mClocksOnBootPreference;
    private CheckBoxPreference mBootloopPrevent;
    private CheckBoxPreference mZramPref;

    private ListPreference mClocksMinPreference;
    private ListPreference mClocksMaxPreference;
    private ListPreference mClocksMaxScreenOff;
    private ListPreference mClocksGovPreference;
    private ListPreference mIoSchedPreference;
    private CheckBoxPreference mFastChargePreference;
    private CheckBoxPreference mEnableSweep2Wake;

    private Context mContext;

    private boolean permsSet = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_settings);

        mContext = (Context) getActivity();

        mClocksOnBootPreference = (CheckBoxPreference) findPreference("eos_performance_cpu_set_on_boot");
        mClocksOnBootPreference.setOnPreferenceChangeListener(this);
        mClocksOnBootPreference.setChecked(Utils
                .readPrefBoolValue(mContext, Utils.CLOCKS_ON_BOOT_PREF));

        mBootloopPrevent = (CheckBoxPreference) findPreference("bootloop_prevent_enable");
        mBootloopPrevent.setOnPreferenceChangeListener(this);
        mBootloopPrevent.setChecked(Utils
                .readPrefBoolValue(mContext, Utils.BOOTLOOP_TIMEOUT));

        mClocksMinPreference = (ListPreference) findPreference("eos_performance_cpu_min");
        mClocksMaxPreference = (ListPreference) findPreference("eos_performance_cpu_max");
        mClocksMinPreference.setOnPreferenceChangeListener(this);
        mClocksMaxPreference.setOnPreferenceChangeListener(this);

        mIoSchedPreference = (ListPreference) findPreference("eos_performance_iosched");
        mIoSchedPreference.setOnPreferenceChangeListener(this);

        String[] frequencies = Utils.readKernelList(Utils.CPU_AVAIL_FREQ);

        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = Utils.appendClockSuffix(frequencies[i]);
        }
        mClocksMinPreference.setEntries(frequencies);
        mClocksMinPreference.setEntryValues(frequencies);
        mClocksMaxPreference.setEntries(frequencies);
        mClocksMaxPreference.setEntryValues(frequencies);

        mClocksGovPreference = (ListPreference) findPreference("eos_performance_cpu_governor");
        mClocksGovPreference.setOnPreferenceChangeListener(this);

        String[] governors = Utils.readKernelList(Utils.CPU_AVAIL_GOV);
        mClocksGovPreference.setEntries(governors);
        mClocksGovPreference.setEntryValues(governors);

        mClocksMaxScreenOff = (ListPreference) findPreference("eos_performance_cpu_max_screen_off");

        if (!Utils.hasKernelFeature(Utils.CPU_MAX_SCREEN_OFF)) {
            final PreferenceCategory clocks = (PreferenceCategory) getPreferenceScreen()
                    .findPreference(CLOCK_CATEGORY);
            clocks.removePreference(mClocksMaxScreenOff);
            mClocksMaxScreenOff = null;
        } else {
            mClocksMaxScreenOff.setOnPreferenceChangeListener(this);
            mClocksMaxScreenOff.setEntries(frequencies);
            mClocksMaxScreenOff.setEntryValues(frequencies);
        }

        if (!Utils.hasKernelFeature(Utils.FFC_PATH)) {
            final PreferenceCategory fc = (PreferenceCategory) getPreferenceScreen()
                    .findPreference(FC_CATEGORY);
            getPreferenceScreen().removePreference(fc);
        } else {
            mFastChargePreference = (CheckBoxPreference) findPreference("eos_performance_fast_charge");
            mFastChargePreference.setOnPreferenceChangeListener(this);
        }

        if (!Utils.hasKernelFeature(Utils.S2W_PATH)) {
            final PreferenceCategory s2w = (PreferenceCategory) getPreferenceScreen()
                    .findPreference(S2W_CATEGORY);
            getPreferenceScreen().removePreference(s2w);
        } else {
            mEnableSweep2Wake = (CheckBoxPreference) findPreference("eos_performance_sweep2wake_enable");
            mEnableSweep2Wake.setOnPreferenceChangeListener(this);
        }

        if (!Utils.hasKernelFeature(Utils.ZRAM)) {
            final PreferenceCategory zram = (PreferenceCategory) getPreferenceScreen()
                    .findPreference(ZRAM_CATEGORY);
            getPreferenceScreen().removePreference(zram);
        } else {
            mZramPref = (CheckBoxPreference) findPreference("eos_performance_zram");
            mZramPref.setChecked(Utils.readPrefBoolValue(mContext, Utils.ZRAM_PREF));
            mZramPref.setOnPreferenceChangeListener(this);
            Utils.checkZramScripts(mContext);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getRoot()) {
            if (!permsSet) {
                Utils.setPerms();
                permsSet = true;
            }
        }
        updateCpuPreferenceValues(mClocksMinPreference, CLOCK_TYPE.MIN);
        updateCpuPreferenceValues(mClocksMaxPreference, CLOCK_TYPE.MAX);
        updateCpuPreferenceValues(mClocksGovPreference, CLOCK_TYPE.GOV);
        if (mClocksMaxScreenOff != null) {
            updateCpuPreferenceValues(mClocksMaxScreenOff, CLOCK_TYPE.SCREEN_MAX);
        }
        updateSchedulerPrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
        /* a quick settings toggle could have changed these values */
        if (mFastChargePreference != null)
            mFastChargePreference.setChecked(Utils.isKernelFeatureEnabled(Utils.FFC_PATH));
        if (mEnableSweep2Wake != null)
            mEnableSweep2Wake.setChecked(Utils.isKernelFeatureEnabled(Utils.S2W_PATH));
    }

    private void updateSchedulerPrefs() {
        String[] schedulers = Utils.getSchedulers();
        String defSched = Utils.getCurrentScheduler();
        mIoSchedPreference.setEntries(schedulers);
        mIoSchedPreference.setEntryValues(schedulers);
        mIoSchedPreference.setSummary("Current value: " + defSched);
        mIoSchedPreference.setValue(defSched);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference.equals(mClocksOnBootPreference)) {
            boolean enabled = ((Boolean) objValue).booleanValue();
            Utils.writePrefBoolValue(mContext, Utils.CLOCKS_ON_BOOT_PREF, enabled);

            if (enabled) {
                Utils.writePrefValue(mContext, Utils.MIN_PREF,
                        Utils.removeClockSuffix(mClocksMinPreference.getValue()));
                Utils.writePrefValue(mContext, Utils.MAX_PREF,
                        Utils.removeClockSuffix(mClocksMaxPreference.getValue()));
                Utils.writePrefValue(mContext, Utils.GOV_PREF, mClocksGovPreference.getValue());
                Utils.writePrefValue(mContext, Utils.IOSCHED_PREF, mIoSchedPreference.getValue());
                if (mClocksMaxScreenOff != null) {
                    Utils.writePrefValue(mContext, Utils.MAX_SCREEN_OFF_PREF,
                            mClocksMaxScreenOff.getValue());
                }
                if (mZramPref != null) {
                    Utils.writePrefBoolValue(mContext, Utils.ZRAM_PREF, mZramPref.isChecked());
                }
                if (mFastChargePreference != null) {
                    Utils.writePrefBoolValue(mContext, Utils.FFC_PREF,
                            mFastChargePreference.isChecked());
                }
                if (mEnableSweep2Wake != null) {
                    Utils.writePrefBoolValue(mContext, Utils.S2W_PREF,
                            mEnableSweep2Wake.isChecked());
                }
            }
        } else if (preference.equals(mBootloopPrevent)) {
            Utils.writePrefBoolValue(mContext, Utils.BOOTLOOP_TIMEOUT,
                    ((Boolean) objValue).booleanValue());
        } else if (preference.equals(mClocksMinPreference)
                || preference.equals(mClocksMaxPreference)
                || preference.equals(mClocksMaxScreenOff)) {
            CLOCK_TYPE clockType = null;
            String bootFile = null;
            if (preference.equals(mClocksMinPreference)) {
                clockType = CLOCK_TYPE.MIN;
                bootFile = Utils.MIN_PREF;
            }
            else if (preference.equals(mClocksMaxPreference)) {
                clockType = CLOCK_TYPE.MAX;
                bootFile = Utils.MAX_PREF;
            } else {
                clockType = CLOCK_TYPE.SCREEN_MAX;
                bootFile = Utils.MAX_SCREEN_OFF_PREF;
            }

            String output = Utils.removeClockSuffix((String) objValue);

            Utils.writePrefValue(mContext, bootFile, output);
            writeToCpuFiles(clockType, output);
            updateCpuPreferenceValues((ListPreference) preference, clockType);
        } else if (preference.equals(mClocksGovPreference)) {
            String newValue = (String) objValue;
            Utils.writePrefValue(mContext, Utils.GOV_PREF, newValue);
            writeToCpuFiles(CLOCK_TYPE.GOV, newValue);
            updateCpuPreferenceValues(mClocksGovPreference, CLOCK_TYPE.GOV);
        } else if (preference.equals(mIoSchedPreference)) {
            Utils.writeKernelValue(Utils.IO_SCHED, (String) objValue);
            Utils.writePrefValue(mContext, Utils.IOSCHED_PREF, ((String) objValue));
            updateSchedulerPrefs();
        } else if (preference.equals(mZramPref)) {
            Utils.writePrefBoolValue(mContext, Utils.ZRAM_PREF, ((Boolean) objValue).booleanValue());
            Utils.enableZram(((Boolean) objValue).booleanValue());
        } else if (preference.equals(mEnableSweep2Wake)) {
            Utils.writeKernelValue(Utils.S2W_PATH, ((Boolean) objValue).booleanValue() ? "1" : "0");
            Utils.writePrefBoolValue(mContext, Utils.S2W_PREF, ((Boolean) objValue).booleanValue());
        } else if (preference.equals(mFastChargePreference)) {
            Utils.writeKernelValue(Utils.FFC_PATH, ((Boolean) objValue).booleanValue() ? "1" : "0");
            Utils.writePrefBoolValue(mContext, Utils.FFC_PREF, ((Boolean) objValue).booleanValue());
        }
        return true;
    }

    private void writeToCpuFiles(CLOCK_TYPE clockType, String contents) {
        String outputFile = null;

        switch (clockType) {
            case MIN:
                outputFile = Utils.CPU_MIN_SCALE;
                break;
            case MAX:
                outputFile = Utils.CPU_MAX_SCALE;
                break;
            case GOV:
                outputFile = Utils.CPU_GOV;
                break;
            case SCREEN_MAX:
                outputFile = Utils.CPU_MAX_SCREEN_OFF;
                break;
        }

        Utils.writeKernelValue(outputFile, contents);
    }

    private void updatePreferenceSummary(Preference preference, String currentValue) {
        if (preference == null || currentValue == null)
            return;

        StringBuilder newSummary = new StringBuilder();
        newSummary.append(getResources().getString(R.string.eos_performance_current_value));
        newSummary.append(" ");
        newSummary.append(currentValue);
        preference.setSummary(newSummary.toString());
    }

    private void updateCpuPreferenceValues(ListPreference preference, CLOCK_TYPE clockType) {
        String input = null, currentValue = null, currentClockFile = null;

        switch (clockType) {
            case MIN:
                currentClockFile = Utils.CPU_MIN_SCALE;
                break;
            case MAX:
                currentClockFile = Utils.CPU_MAX_SCALE;
                break;
            case GOV:
                currentClockFile = Utils.CPU_GOV;
                break;
            case SCREEN_MAX:
                currentClockFile = Utils.CPU_MAX_SCREEN_OFF;
                break;
        }

        input = Utils.readKernelValue(mContext, currentClockFile);
        if (clockType == CLOCK_TYPE.MIN || clockType == CLOCK_TYPE.MAX
                || clockType == CLOCK_TYPE.SCREEN_MAX) {
            currentValue = Utils.appendClockSuffix(input);
        } else {
            currentValue = input;
        }
        preference.setValue(currentValue);
        updatePreferenceSummary(preference, currentValue);
    }

    private enum CLOCK_TYPE {
        MIN, MAX, SCREEN_MAX, GOV;
    }
}
