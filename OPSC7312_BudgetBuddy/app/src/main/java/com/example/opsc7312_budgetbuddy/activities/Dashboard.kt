package com.example.opsc7312_budgetbuddy.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.Category
import com.example.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.example.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.example.opsc7312_budgetbuddy.databinding.ActivityDashboardBinding

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var transactionCRUD: TransactionCRUD
    private lateinit var budgetCRUD: budgetCRUD
    private var expenseList: MutableList<Category> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var addExpenseAdapter: AddExpenseAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionCRUD = TransactionCRUD()
        budgetCRUD = budgetCRUD()

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

        val totalBudgetInput = dialog.findViewById<EditText>(R.id.totalBudgetInput)
        val saveButton = dialog.findViewById<Button>(R.id.createBudgetButton)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)
        val month = Calendar.MONTH.toString()
        val addExpense = dialog.findViewById<Button>(R.id.addExpenseButton)

        var recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        addExpenseAdapter = AddExpenseAdapter(expenseList)
        recyclerView.adapter = addExpenseAdapter

        addExpense.setOnClickListener {
            val newExpense = Category()
            expenseList.add(newExpense)
            addExpenseAdapter.notifyItemInserted(expenseList.size - 1)
        }





        //TODO Kaushil
        //val month = getmonth
        //val category = recyclercategory

        /*val categoryList = listOf(
            Category(categoryName = "Groceries", amount = 500.0),
            Category(categoryName = "Rent", amount = 1200.0),
            Category(categoryName = "Entertainment", amount = 300.0)
        )*/

        saveButton.setOnClickListener {
            val totalBudget = totalBudgetInput.text.toString().toDoubleOrNull() ?:0.0

            val budgetModel = BudgetModel(month,totalBudget, expenseList)

            budgetCRUD.saveBudget(budgetModel)

            dialog.dismiss()
        }

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

        val transactionName = dialog.findViewById<EditText>(R.id.transactionNameInput)
        val transactionAmount = dialog.findViewById<EditText>(R.id.amountInput)
        val categorySpinner = dialog.findViewById<Spinner>(R.id.categoryInput)
        val saveButton = dialog.findViewById<Button>(R.id.createTransactionButton)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        saveButton.setOnClickListener {
            val name = transactionName.text.toString()
            val amountText = transactionAmount.text.toString()
            val amount: Double = amountText.toDoubleOrNull() ?: 0.0
            val category = categorySpinner.selectedItem.toString()

            val transactionModel = TransactionModel(name, amount, category)

            transactionCRUD.saveTransaction(transactionModel)

            dialog.dismiss()
        }

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

