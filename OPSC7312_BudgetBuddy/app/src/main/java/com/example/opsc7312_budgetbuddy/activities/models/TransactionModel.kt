package com.example.opsc7312_budgetbuddy.activities.models

import android.util.Log
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class TransactionModel(
    val transactionName: String= "",
    val transactionAmount: Double= 0.0,
    val categoryName: String= ""
)

class TransactionCRUD{

    public fun saveTransaction(transactionModel: TransactionModel) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TransactionApi::class.java)

        api.addTransaction(transactionModel).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("TransactionApi", "Transaction added successfully")
                } else {
                    Log.e("TransactionApi", response.toString())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("TransactionApi", "Error: ${t.message}")
            }
        })
    }
}
