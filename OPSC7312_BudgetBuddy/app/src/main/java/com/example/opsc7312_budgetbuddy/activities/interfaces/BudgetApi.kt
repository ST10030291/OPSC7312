package com.example.opsc7312_budgetbuddy.activities.interfaces

import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.TotalBudgetResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

//Actions for API POST AND GET for budgets
interface BudgetApi {
    @POST("budgets")
    fun addBudget(@Body budget: BudgetModel): Call<Void>

    @GET("users/{userId}/totalBudget")
    fun getTotalBudget(@Path("userId") userId: String): Call<TotalBudgetResponse>

    @GET("budgets/{id}")
    fun getBudgetById(@Path("id") id: String): Call<BudgetModel>
}