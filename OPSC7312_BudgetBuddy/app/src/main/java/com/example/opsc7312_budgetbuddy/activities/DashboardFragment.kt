package com.example.opsc7312_budgetbuddy.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.example.opsc7312_budgetbuddy.activities.models.BudgetItem
import com.example.opsc7312_budgetbuddy.activities.models.TotalBudgetResponse
import com.example.opsc7312_budgetbuddy.activities.models.TransactionAdapter
import com.example.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import retrofit2.Call

class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var BudgetItems: MutableList<BudgetItem>
    private lateinit var BudgetAdapter: BudgetAdapter
    private lateinit var totalBudgetTextView: TextView
    private lateinit var retrofit: Retrofit
    private lateinit var budgetApiService: BudgetApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)

        budgetApiService = retrofit.create(BudgetApi::class.java)

        // Get the current user's email and display the first part as the name
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.welcomeTextView).text = "Welcome, $userName"

        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        BudgetItems = mutableListOf()
        BudgetAdapter = BudgetAdapter(BudgetItems)
        recyclerView.adapter = BudgetAdapter

        totalBudgetTextView = view.findViewById(R.id.availableBudget)

        fetchTotalBudget()
    }

    private fun fetchTotalBudget() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        budgetApiService.getTotalBudget(userId).enqueue(object : retrofit2.Callback<TotalBudgetResponse> {
            override fun onResponse(
                call: Call<TotalBudgetResponse>,
                response: retrofit2.Response<TotalBudgetResponse>
            ) {
                if (response.isSuccessful) {
                    val totalBudget = response.body()?.totalBudget ?: 0.0
                    totalBudgetTextView.text = "Total Budget: $${totalBudget}"
                } else {
                    Log.e("API Error", "Error fetching total budget")
                }
            }

            override fun onFailure(call: Call<TotalBudgetResponse>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch total budget", t)
            }
        })
    }
}