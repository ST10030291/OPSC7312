package com.example.opsc7312_budgetbuddy.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.databinding.ActivityDashboardBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        loadFragment(DashboardFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(DashboardFragment())
                    true
                }

                R.id.navigation_transaction -> {
                    loadFragment(TransactionsFragment())
                    true
                }

                R.id.navigation_analytics -> {
                    loadFragment(AnalyticsFragment())
                    true
                }

                R.id.navigation_account -> {
                    loadFragment(AccountFragment())
                    true
                }

                else -> false
            }
        }

        binding.fabPopupTray.setOnClickListener { view ->
            showPopupMenu()
        }
    }

    private fun showPopupMenu() {
        val popup = Dialog(this)
        popup.setContentView(R.layout.custom_popup_layout)
        popup.setCancelable(true)

        popup.show()

        val window = popup.window
        val layoutParams = window?.attributes

        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.x = 0
        layoutParams?.y = 200

        window?.attributes = layoutParams

        val setBudgetButton = popup.findViewById<Button>(R.id.setBudgetButton)
        val logTransactionButton = popup.findViewById<Button>(R.id.logTransactionButton)

        setBudgetButton.setOnClickListener {
            showSetBudgetBottomDialog()
            popup.dismiss()
        }

        logTransactionButton.setOnClickListener {
            showAddTransactionBottomDialog()
            popup.dismiss()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun showSetBudgetBottomDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_set_budget)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showAddTransactionBottomDialog() {
        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_log_transaction)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

}

