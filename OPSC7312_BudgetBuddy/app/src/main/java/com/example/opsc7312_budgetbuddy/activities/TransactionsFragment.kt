package com.example.opsc7312_budgetbuddy.activities

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import android.media.audiofx.BassBoost
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.AnalyticsFragment.Companion.PERMISSION_REQUEST_CODE
import com.example.opsc7312_budgetbuddy.activities.models.TransactionAdapter
import com.example.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.example.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.Executor

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TransactionsFragment : Fragment() {
    private var transactionList: List<TransactionItem> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var TransactionItems: MutableList<TransactionItem>
    private lateinit var TransactionAdapter: TransactionAdapter
    private lateinit var profileImageView: ShapeableImageView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private lateinit var authenticationButton: Button
    private lateinit var sqliteHelper: DatabaseHelper

    companion object {
        var PERMISSION_REQUEST_CODE = 100
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isOfflineMode = sharedPref.getBoolean("offlineMode", false)

        sqliteHelper = DatabaseHelper(requireContext())
        profileImageView = view.findViewById(R.id.transactions_account_btn)
        recyclerView = view.findViewById(R.id.recentTransactionsRecyclerView)
        authenticationButton = view.findViewById(R.id.authenticateButton)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        TransactionItems = mutableListOf()
        TransactionAdapter = TransactionAdapter(TransactionItems)
        recyclerView.adapter = TransactionAdapter
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

        authenticationButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.USE_BIOMETRIC)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.USE_BIOMETRIC),
                    PERMISSION_REQUEST_CODE
                )
            }
            else{
                biometricPrompt.authenticate(promptInfo)
            }
        }

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

    /*fun biometricAuth(view: View) {
        val executor = ContextCompat.getMainExecutor(requireContext())

// Use 'this' if you're in a fragment
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(requireContext(), "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(requireContext(), "Authentication success!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // BiometricPrompt.PromptInfo configuration
        val promptInfo = PromptInfo.Builder()
            .setTitle("Biometric login for app")
            .setSubtitle("")
            .setNegativeButtonText("Login with password")
            .setConfirmationRequired(false)
            .build()

        // Trigger biometric authentication
        biometricPrompt.authenticate(promptInfo)
    }*/
}