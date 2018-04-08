package com.xiaopo.flying.whenindoors.ui.page.setting

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceGroup
import com.xiaopo.flying.whenindoors.R

/**
 * @author wupanjie
 */
class SettingFragment : PreferenceFragment() {

  companion object {
    fun newInstance() = SettingFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.wifi_scan_preference)
  }

  override fun onResume() {
    super.onResume()

    fillEntriesAndSummaries(preferenceScreen)
    preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
      setSummary(findPreference(key))
    }
  }

  private fun fillEntriesAndSummaries(group: PreferenceGroup) {
    for (i in 0 until group.preferenceCount) {
      val pref = group.getPreference(i)
      if (pref is PreferenceGroup) {
        fillEntriesAndSummaries(pref)
      }
      setEntries(pref)
      setSummary(pref)
    }
  }

  private fun setEntries(pref: Preference) {
    if (pref !is ListPreference) {
      return
    }


  }

  private fun setSummary(pref: Preference) {
    if (pref !is ListPreference) {
      return
    }

    pref.setSummary(pref.entry)

  }
}