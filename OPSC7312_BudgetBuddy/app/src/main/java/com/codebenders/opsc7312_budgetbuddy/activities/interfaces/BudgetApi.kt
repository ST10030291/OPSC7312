package com.codebenders.opsc7312_budgetbuddy.activities.interfaces

import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetResponse
import com.codebenders.opsc7312_budgetbuddy.activities.models.RemainingBudgetResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
// The code for this interface was taken from a geeksforgeeks blog
// Titled: How to GET Data From API using Retrofit Library in Android?
// Written by: Chinmaya121221
// Available at: https://www.geeksforgeeks.org/how-to-get-data-from-api-using-retrofit-library-in-android/
// Accessed 15 September 2024


//Actions for API POST AND GET for budgets
interface BudgetApi {
    @POST("budgets")
    fun addBudget(@Body budget: BudgetModel): Call<BudgetResponse>

    @GET("budgets")
    fun getBudgetsByUserId(@Query("userId") userId: String): Call<List<BudgetModel>>

    @GET("remaining-budget")
    fun getRemainingBudget(@Query("userId") userId: String): Call<RemainingBudgetResponse>
}