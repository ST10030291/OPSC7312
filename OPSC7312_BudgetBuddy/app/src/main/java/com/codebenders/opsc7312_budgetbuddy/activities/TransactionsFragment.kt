package com.codebenders.opsc7312_budgetbuddy.activities

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codebenders.opsc7312_budgetbuddy.R
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionAdapter
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.Executor

class TransactionsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionItems: MutableList<TransactionItem>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var profileImageView: ShapeableImageView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private lateinit var sqliteHelper: DatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isOfflineMode = sharedPref.getBoolean("offlineMode", false)

        sqliteHelper = DatabaseHelper(requireContext())
        profileImageView = view.findViewById(R.id.transactions_account_btn)
        recyclerView = view.findViewById(R.id.recentTransactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        transactionItems = mutableListOf()
        transactionAdapter = TransactionAdapter(transactionItems)
        recyclerView.adapter = transactionAdapter
        loadProfileImageFromFirebaseStorage()

        if(isOfflineMode){
            handleOfflineMode()
        }else {
            fetchTransactions()
        }

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(requireActivity(), executor,
            object : AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(),"Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(requireContext(),"Authentication succeeded!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }

    private fun handleOfflineMode() {
        val transactions = sqliteHelper.getLocalTransactions()
        val transactionItems = mutableListOf<TransactionItem>()

        for (transactionModel in transactions) {
            transactionItems.add(
                TransactionItem(
                    name = transactionModel.transactionName,
                    category = transactionModel.categoryName,
                    amount = transactionModel.transactionAmount.toString(),
                    date = transactionModel.transactionDate
                )
            )
        }
        if (transactionItems.isNotEmpty()) {
            val adapter = TransactionAdapter(transactionItems)
            recyclerView.adapter = adapter
        }
    }

    private fun fetchTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val transactionCRUD = TransactionCRUD()

        transactionCRUD.getTransactions(onSuccess = { transactionModels ->
            val transactionItems = mutableListOf<TransactionItem>()

            for (transactionModel in transactionModels) {
                transactionItems.add(
                    TransactionItem(
                        name = transactionModel.transactionName,
                        category = transactionModel.categoryName,
                        amount = transactionModel.transactionAmount.toString(),
                        date = transactionModel.transactionDate
                    )
                )
            }
            if (transactionItems.isNotEmpty()) {
                val adapter = TransactionAdapter(transactionItems)
                recyclerView.adapter = adapter
            }
        }, onError = { errorMessage ->
            Log.e("API Error", errorMessage)
        })
    }
    private fun loadProfileImageFromFirebaseStorage() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("ProfileImages/$userId.jpg")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(profileImageView)
        }.addOnFailureListener {
            Log.e("Firebase Storage", "Error loading image", it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }
}