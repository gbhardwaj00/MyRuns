package com.stressmeter.myruns

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class FragmentSettings : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val manager : PreferenceManager = preferenceManager
        manager.sharedPreferencesName = getString(R.string.settings_preference_key)
        return
    }
}
