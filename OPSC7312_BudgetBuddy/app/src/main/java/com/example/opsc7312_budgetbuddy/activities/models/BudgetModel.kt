package com.example.opsc7312_budgetbuddy.activities.models

import android.util.Log
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class BudgetModel(
    var id: String?,
    val userId: String = "",
    val month: String = "",
    val totalBudget: Double = 0.0,
    val categories: List<Category> = listOf()
)

data class Category(
    var categoryName: String = "",
    var amount: Double = 0.0
)

data class BudgetResponse(val id: String)

//The budgetCRUD class is responsible for CRUD operations for budgets
class budgetCRUD{

    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    val retrofit = Retrofit.Builder()
        .baseUrl("https://budgetapp-amber.vercel.app/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(BudgetApi::class.java)

    fun getBudgets(onSuccess: (List<BudgetModel>) -> Unit, onError: (String) -> Unit) {
        if (userId != null) {
            api.getBudgetsByUserId(userId).enqueue(object : Callback<List<BudgetModel>> {
                override fun onResponse(call: Call<List<BudgetModel>>, response: Response<List<BudgetModel>>) {
                    if (response.isSuccessful) {
                        val recipes = response.body() ?: emptyList()
                        onSuccess(recipes)
                    } else {
                        onError("Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                    onError("API Error: ${t.message}")
                }
            })
        }
    }

    
}

