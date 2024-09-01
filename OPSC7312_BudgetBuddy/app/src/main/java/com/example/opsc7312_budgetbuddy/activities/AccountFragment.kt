package com.example.opsc7312_budgetbuddy.activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import com.example.opsc7312_budgetbuddy.R
import java.util.Locale

class AccountFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPrefs"
    private val LANGUAGE_KEY = "language_key"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0)

        // Load saved language preference
        val savedLanguage = sharedPreferences.getString(LANGUAGE_KEY, "en")
        val languageSwitch = view.findViewById<SwitchCompat>(R.id.languageSwitch)
        languageSwitch.isChecked = savedLanguage == "af"

        // notification button click listener
        view.findViewById<ImageView>(R.id.notification_btn)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        // language switch
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val languageCode = if (isChecked) "af" else "en"
            setLocale(languageCode)
            saveLanguagePreference(languageCode)
            restartFragment()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
    }

    private fun saveLanguagePreference(languageCode: String) {
        with(sharedPreferences.edit()) {
            putString(LANGUAGE_KEY, languageCode)
            apply()
        }
    }

    private fun restartFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AccountFragment())
            .commit()
    }
}
