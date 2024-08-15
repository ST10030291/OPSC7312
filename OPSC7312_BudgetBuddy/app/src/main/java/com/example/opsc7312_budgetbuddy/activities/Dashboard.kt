package com.example.opsc7312_budgetbuddy.activities

import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.databinding.ActivityDashboardBinding

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        // Create a PopupMenu
        val popup = PopupMenu(this, view)
        // Inflate the popup menu
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popupmenu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.setBudget -> {
                    loadFragment(SetBudgetFragment())
                    true
                }
                R.id.logTransaction -> {
                    loadFragment(LogTransactionFragment())
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
