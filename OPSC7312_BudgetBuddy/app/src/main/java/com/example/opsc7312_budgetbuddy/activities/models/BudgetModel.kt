package com.example.opsc7312_budgetbuddy.activities.models

import android.util.Log
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class BudgetModel(
    val month: String = "",
    val totalBudget: Double = 0.0,
    val categories: List<Category> = listOf()
)

data class Category(
    val categoryName: String = "",
    val amount: Double = 0.0
)

//The budgetCRUD class is responsible for CRUD operations for budgets
class budgetCRUD{
    fun saveBudget(budgetModel: BudgetModel) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(BudgetApi::class.java)

        //This will check if the budget has been added to firestore
        // and will return an appropriate message
        api.addBudget(budgetModel).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("BudgetApi", "Budget added successfully")
                } else {
                    Log.e("BudgetApi", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("BudgetApi", "Error: ${t.message}")
            }
        })
    }
}

