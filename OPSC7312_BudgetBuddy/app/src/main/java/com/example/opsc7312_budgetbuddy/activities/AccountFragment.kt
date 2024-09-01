package com.example.opsc7312_budgetbuddy.activities

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // notification button click listener
        view.findViewById<ImageView>(R.id.notification_btn)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        // language switch
        val languageSwitch = view.findViewById<SwitchCompat>(R.id.languageSwitch)
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setLocale("af") // Switch to Afrikaans
            } else {
                setLocale("en") // Switch to English
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)

        requireActivity().recreate()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }
}
