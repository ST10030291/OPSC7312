package com.example.opsc7312_budgetbuddy.activities.models

import android.util.Log
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class TransactionModel(
    val userId: String = "",
    val transactionName: String= "",
    val transactionAmount: Double= 0.0,
    val categoryName: String= "",
    val transactionDate: String
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

                } else {
                    Log.e("TransactionApi", response.toString())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("TransactionApi", "Error: ${t.message}")
            }
        })
    }


    fun getTransactions(onSuccess: (List<TransactionModel>) -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        val retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TransactionApi::class.java)

        if (userId != null) {
            api.getAllTransactions(userId).enqueue(object : Callback<List<TransactionModel>> {
                override fun onResponse(call: Call<List<TransactionModel>>, response: Response<List<TransactionModel>>) {
                    if (response.isSuccessful) {
                        val recipes = response.body() ?: emptyList()
                        onSuccess(recipes)
                    } else {
                        onError("Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                    onError("API Error: ${t.message}")
                }
            })
        }
    }
}