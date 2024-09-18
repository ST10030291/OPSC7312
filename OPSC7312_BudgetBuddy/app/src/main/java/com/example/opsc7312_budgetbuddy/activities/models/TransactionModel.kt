package com.example.opsc7312_budgetbuddy.activities.models

import android.util.Log
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class TransactionModel(
    val userId: String = "",
    val transactionName: String= "",
    val transactionAmount: Double= 0.0,
    val categoryName: String= ""
)

//The TransactionCRUD class is responsible for CRUD operations for transactions
class TransactionCRUD{

    fun saveTransaction(transactionModel: TransactionModel) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TransactionApi::class.java)

        // This will check if the transaction has been added to Firestore
        // and will return an appropriate message
        api.addTransaction(transactionModel).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("TransactionApi", "Transaction added successfully")
                    // Update budget after transaction has been added
                    updateBudgetAfterTransaction(transactionModel)
                } else {
                    Log.e("TransactionApi", response.toString())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("TransactionApi", "Error: ${t.message}")
            }
        })
    }

    private fun updateBudgetAfterTransaction(transaction: TransactionModel) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BudgetApi::class.java)

        // Fetch the user's budget for the current month
        api.getBudgetsByUserId(transaction.userId).enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(call: Call<List<BudgetModel>>, response: Response<List<BudgetModel>>) {
                if (response.isSuccessful) {
                    val budgets = response.body()
                    if (!budgets.isNullOrEmpty()) {
                        // Assuming the first budget is for the current month (you might want to refine this)
                        val budget = budgets[0]

                        // Find the category in which the transaction was made
                        val category = budget.categories.find { it.categoryName == transaction.categoryName }

                        if (category != null) {
                            // Update the category's amount based on the transaction
                            category.amount -= transaction.transactionAmount

                            // Now, update the budget on the server
                            api.updateBudget(budget.id!!, budget).enqueue(object : Callback<BudgetResponse> {
                                override fun onResponse(call: Call<BudgetResponse>, response: Response<BudgetResponse>) {
                                    if (response.isSuccessful) {
                                        Log.d("BudgetApi", "Budget updated successfully")
                                    } else {
                                        Log.e("BudgetApi", "Error updating budget: ${response.code()} - ${response.message()}")
                                    }
                                }

                                override fun onFailure(call: Call<BudgetResponse>, t: Throwable) {
                                    Log.e("BudgetApi", "Failed to update budget", t)
                                }
                            })
                        } else {
                            Log.e("BudgetApi", "Category not found in the user's budget")
                        }
                    } else {
                        Log.e("BudgetApi", "No budget found for the user")
                    }
                } else {
                    Log.e("BudgetApi", "Error fetching budget: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("BudgetApi", "Failed to fetch budget", t)
            }
        })
    }
}
