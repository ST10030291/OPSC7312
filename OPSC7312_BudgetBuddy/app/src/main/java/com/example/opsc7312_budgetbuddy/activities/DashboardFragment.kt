package com.example.opsc7312_budgetbuddy.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.example.opsc7312_budgetbuddy.activities.models.BudgetItem
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.example.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    // Global variables
    private var circularTotalBudget: Double = 0.0
    private var circularAvailableBudget: Double = 0.0

    private lateinit var recyclerView: RecyclerView
    private lateinit var BudgetItems: MutableList<BudgetItem>
    private lateinit var BudgetAdapter: BudgetAdapter
    private lateinit var totalBudgetTextView: TextView
    private lateinit var availableBudgetForMonth: TextView
    private lateinit var retrofit: Retrofit
    private lateinit var budgetApiService: BudgetApi
    private lateinit var transactionApiService: TransactionApi
    private lateinit var circularProgressBarBudget: CircularProgressBar
    private lateinit var circularProgressBarSpent: CircularProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Initialize Circular Progress Bars
        circularProgressBarBudget = view.findViewById(R.id.circularProgressBarBudget)
        circularProgressBarSpent = view.findViewById(R.id.circularProgressBarSpent)

        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)

        budgetApiService = retrofit.create(BudgetApi::class.java)
        transactionApiService = retrofit.create(TransactionApi::class.java)

        // Display a welcome message with the users name for added personalization
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.welcomeTextView).text = "Welcome, $userName"

        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        BudgetItems = mutableListOf()
        BudgetAdapter = BudgetAdapter(BudgetItems)
        recyclerView.adapter = BudgetAdapter

        // Display Total Budget set for the month
        totalBudgetTextView = view.findViewById(R.id.availableBudget)
        availableBudgetForMonth = view.findViewById(R.id.availableBudgetForMonth)

        // Update Total Available Budget and Amount of Transactions
        fetchTotalBudget()
        fetchTransactionCount()
        fetchRemainingBudget()
        fetchBudgets()
        updateCircularProgressBars(circularTotalBudget,circularAvailableBudget)
    }

    // Method to fetch Category Name and Amount from the API for Budget Breakdown
    private fun fetchBudgets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create an instance of budgetCRUD
        val budgetCRUD = budgetCRUD()

        budgetCRUD.getBudgets(onSuccess = { budgetModels ->
            // Extract categories from each BudgetModel
            val budgetItems = mutableListOf<BudgetItem>()

            for (budgetModel in budgetModels) {
                for (category in budgetModel.categories) {
                    budgetItems.add(
                        BudgetItem(
                            categoryName = category.categoryName,
                            amount = category.amount
                        )
                    )
                }
            }

            // Update the adapter with the new data
            if (budgetItems.isNotEmpty()) {
                val adapter = BudgetAdapter(budgetItems)
                recyclerView.adapter = adapter
            }
        }, onError = { errorMessage ->
            Log.e("API Error", errorMessage)
        })
    }


    // Method to fetch the total available budget for the month from the API
    private fun fetchTotalBudget() {

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        budgetApiService.getBudgetsByUserId(userId).enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(
                call: Call<List<BudgetModel>>,
                response: Response<List<BudgetModel>>
            ) {
                if (response.isSuccessful) {
                    val budgets = response.body() ?: emptyList()
                    if (budgets.isNotEmpty()) {
                        val latestBudget = budgets.maxByOrNull { it.month }
                        val totalBudget = latestBudget?.totalBudget ?: 0.0
                        circularTotalBudget = totalBudget
                        totalBudgetTextView.text = "${totalBudget}"
                    } else {
                        totalBudgetTextView.text = "R0.00"
                    }
                } else {
                    Log.e("API Error", "Error fetching total budget")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch total budget", t)
            }
        })
    }

    // Method to fetch the transaction count from the API
    private fun fetchTransactionCount() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        transactionApiService.getAllTransactions(userId).enqueue(object : Callback<List<TransactionModel>> {
            override fun onResponse(
                call: Call<List<TransactionModel>>,
                response: Response<List<TransactionModel>>
            ) {
                if (response.isSuccessful) {
                    val transactions = response.body() ?: emptyList()
                    val transactionCount = transactions.size
                    // Update the UI with the transaction count
                    view?.findViewById<TextView>(R.id.totalTransactions)?.text = "$transactionCount"
                } else {
                    Log.e("API Error", "Error fetching transactions")
                }
            }

            override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch transactions", t)
            }
        })
    }

    // Method to fetch and display the remaining budget
    private fun fetchRemainingBudget() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val budgetCall = budgetApiService.getBudgetsByUserId(userId)
        val transactionCall = transactionApiService.getAllTransactions(userId)

        // Make parallel API calls
        budgetCall.enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(
                call: Call<List<BudgetModel>>,
                response: Response<List<BudgetModel>>
            ) {
                if (response.isSuccessful) {
                    val budgets = response.body() ?: emptyList()
                    val latestBudget = budgets.maxByOrNull { it.month }
                    val totalBudget = latestBudget?.totalBudget ?: 0.0
                    circularTotalBudget = totalBudget // Update global variable

                    // Fetch transactions and calculate remaining budget
                    transactionCall.enqueue(object : Callback<List<TransactionModel>> {
                        override fun onResponse(
                            call: Call<List<TransactionModel>>,
                            response: Response<List<TransactionModel>>
                        ) {
                            if (response.isSuccessful) {
                                val transactions = response.body() ?: emptyList()
                                val totalSpent = transactions.sumOf { it.transactionAmount }
                                val remainingBudget = totalBudget - totalSpent
                                circularAvailableBudget = remainingBudget // Update global variable

                                // Update the correct TextView
                                totalBudgetTextView.text = "R$totalBudget"
                                availableBudgetForMonth.text = "R$remainingBudget"

                                // Now update the progress bars with the correct values
                                updateCircularProgressBars(circularTotalBudget, circularAvailableBudget)
                            } else {
                                Log.e("API Error", "Error fetching transactions for remaining budget")
                            }
                        }

                        override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                            Log.e("API Failure", "Failed to fetch transactions for remaining budget", t)
                        }
                    })
                } else {
                    Log.e("API Error", "Error fetching budget for remaining budget calculation")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch budget for remaining budget calculation", t)
            }
        })
    }

    // Method to update circular progress bar with budget values
    fun updateCircularProgressBars(totalBudget: Double, availableBudget: Double) {
        if (totalBudget > 0) {
            // Calculate percentage spent and remaining
            val val1 = totalBudget * 100
            val val2 = (availableBudget / totalBudget) * 100

            // Update progress bars
            circularProgressBarSpent.setProgressWithAnimation(val1.toFloat(), 1000) // Spent amount
            circularProgressBarBudget.setProgressWithAnimation(val2.toFloat(), 1000) // Remaining amount
        } else {
            // Handle the case where totalBudget is 0 to avoid division by zero
            circularProgressBarSpent.setProgressWithAnimation(0f, 1000)
            circularProgressBarBudget.setProgressWithAnimation(0f, 1000)
        }
    }
}