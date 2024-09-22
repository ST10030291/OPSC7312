package com.example.opsc7312_budgetbuddy.activities

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BudgetWidgetProvider : AppWidgetProvider() {

    private lateinit var budgetApiService: BudgetApi
    private lateinit var transactionApiService: TransactionApi

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            fetchDataAndUpdateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun fetchDataAndUpdateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        budgetApiService = retrofit.create(BudgetApi::class.java)
        transactionApiService = retrofit.create(TransactionApi::class.java)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch total budget
        budgetApiService.getBudgetsByUserId(userId).enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(call: Call<List<BudgetModel>>, response: Response<List<BudgetModel>>) {
                if (response.isSuccessful) {
                    val budgets = response.body() ?: emptyList()
                    val latestBudget = budgets.maxByOrNull { it.month }
                    val totalBudget = latestBudget?.totalBudget ?: 0.0

                    // Fetch transaction count
                    transactionApiService.getAllTransactions(userId).enqueue(object : Callback<List<TransactionModel>> {
                        override fun onResponse(call: Call<List<TransactionModel>>, response: Response<List<TransactionModel>>) {
                            if (response.isSuccessful) {
                                val transactions = response.body() ?: emptyList()
                                val transactionCount = transactions.size
                                val remainingBudget = totalBudget - transactions.sumOf { it.transactionAmount }

                                // Update widget UI
                                updateWidgetUI(context, appWidgetManager, appWidgetId, totalBudget, remainingBudget, transactionCount)
                            } else {
                                Log.e("Widget Error", "Failed to fetch transactions")
                            }
                        }

                        override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                            Log.e("Widget Error", "Failed to fetch transactions", t)
                        }
                    })
                } else {
                    Log.e("Widget Error", "Failed to fetch budget")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("Widget Error", "Failed to fetch budget", t)
            }
        })
    }

    private fun updateWidgetUI(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, totalBudget: Double, availableBudget: Double, transactionCount: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Update the widget views
        views.setTextViewText(R.id.availableBudgetForMonth, "R$availableBudget")
        views.setTextViewText(R.id.availableBudget, "R$totalBudget")
        views.setTextViewText(R.id.totalTransactions, "$transactionCount")

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
