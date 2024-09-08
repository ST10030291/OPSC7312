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
import com.example.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.example.opsc7312_budgetbuddy.activities.models.BudgetItem
import com.example.opsc7312_budgetbuddy.activities.models.TransactionAdapter
import com.example.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var transactionsListener: ListenerRegistration? = null
    private var budgetBreakdownListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the current user's email and display the first part as the name
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.welcomeTextView).text = "Welcome, $userName"

    }

}