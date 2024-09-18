package com.example.opsc7312_budgetbuddy.activities.interfaces

import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

//Actions for API POST AND GET for transactions
interface TransactionApi {
    @POST("transactions")
    fun addTransaction(@Body transactionModel: TransactionModel): Call<Void>

    @GET("transactions")
    fun getAllTransactions(@Query("userId") userId: String): Call<List<TransactionModel>>

}