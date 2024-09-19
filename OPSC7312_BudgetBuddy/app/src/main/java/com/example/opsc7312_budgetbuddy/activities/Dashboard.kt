package com.example.opsc7312_budgetbuddy.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.BudgetResponse
import com.example.opsc7312_budgetbuddy.activities.models.Category
import com.example.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.example.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.example.opsc7312_budgetbuddy.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat

class Dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var transactionCRUD: TransactionCRUD
    private lateinit var budgetCRUD: budgetCRUD
    private var budgetList: MutableList<BudgetModel> = mutableListOf()
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
        loadBudgets()

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

        val cal = Calendar.getInstance()
        val month_date = SimpleDateFormat("MMMM")
        val month = month_date.format(cal.time)
        val addExpense = dialog.findViewById<Button>(R.id.addExpenseButton)

        var recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        addExpenseAdapter = AddExpenseAdapter(expenseList)
        recyclerView.adapter = addExpenseAdapter

        addExpense.setOnClickListener {
            val newExpense = Category()
            expenseList.add(newExpense)
            addExpenseAdapter.notifyItemInserted(expenseList.size - 1)
        }


        saveButton.setOnClickListener {
            val totalBudget = totalBudgetInput.text.toString().toDoubleOrNull() ?:0.0

            val retrofit = Retrofit.Builder()
                .baseUrl("https://budgetapp-amber.vercel.app/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(BudgetApi::class.java)

            val budgetModel = BudgetModel(id = null,userId, month ,totalBudget, expenseList)

            api.addBudget(budgetModel).enqueue(object : Callback<BudgetResponse> {
                override fun onResponse(call: Call<BudgetResponse>, response: Response<BudgetResponse>) {
                    if (response.isSuccessful) {
                        val budgetID = response.body()?.id
                        if (budgetID != null) {
                            budgetModel.id = budgetID
                            budgetList.add(budgetModel)

                            Log.d("BudgetApi", "Budget added successfully")
                        } else {
                            Log.e("BudgetApi", "No ID received from the server")
                        }
                    } else {
                        Log.e("BudgetApi", "Error: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<BudgetResponse>, t: Throwable) {
                    Log.e("BudgetApi", "Failed to add budget", t)
                }
            })

            // Reload the page for the budget to reflect the new budget
            val intent = Intent(this, javaClass)
            finish()
            startActivity(intent)
            dialog.dismiss()

            Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show()
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
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        saveButton.setOnClickListener {
            val name = transactionName.text.toString()
            val amountText = transactionAmount.text.toString()
            val amount: Double = amountText.toDoubleOrNull() ?: 0.0
            val category = categorySpinner.selectedItem.toString()

            val transactionModel = TransactionModel(userId,name, amount, category)

            transactionCRUD.saveTransaction(transactionModel)

            // Reload the page for transaction count to reflect the new transaction
            val intent = Intent(this, javaClass)
            finish()
            startActivity(intent)
            dialog.dismiss()

            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
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

    private fun loadBudgets() {
        budgetCRUD.getBudgets(
            onSuccess = { recipes ->
                budgetList.clear()
                budgetList.addAll(recipes)
            },
            onError = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }
}