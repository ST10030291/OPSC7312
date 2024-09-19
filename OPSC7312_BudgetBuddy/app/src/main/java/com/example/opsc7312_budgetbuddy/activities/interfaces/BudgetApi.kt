package com.example.opsc7312_budgetbuddy.activities.interfaces

import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.BudgetResponse
import com.example.opsc7312_budgetbuddy.activities.models.RemainingBudgetResponse
import com.example.opsc7312_budgetbuddy.activities.models.TotalBudgetResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

//Actions for API POST AND GET for budgets
interface BudgetApi {
    @POST("budgets")
    fun addBudget(@Body budget: BudgetModel): Call<BudgetResponse>

    @GET("budgets")
    fun getBudgetsByUserId(@Query("userId") userId: String): Call<List<BudgetModel>>

    @GET("remaining-budget")
    fun getRemainingBudget(@Query("userId") userId: String): Call<RemainingBudgetResponse>
}