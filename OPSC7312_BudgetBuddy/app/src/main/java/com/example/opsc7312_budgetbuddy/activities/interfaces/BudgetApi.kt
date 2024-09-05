package com.example.opsc7312_budgetbuddy.activities.interfaces

import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BudgetApi {
    @POST("budgets")
    fun addBudget(@Body budget: BudgetModel): Call<Void>

    @GET("budgets")
    fun getAllBudgets(): Call<List<BudgetModel>>

    @GET("budgets/{id}")
    fun getBudgetById(@Path("id") id: String): Call<BudgetModel>
}