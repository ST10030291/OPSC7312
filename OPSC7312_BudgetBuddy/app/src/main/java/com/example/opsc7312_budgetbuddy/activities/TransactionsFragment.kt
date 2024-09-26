package com.example.opsc7312_budgetbuddy.activities

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.models.TransactionAdapter
import com.example.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.example.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileImageView = view.findViewById(R.id.transactions_account_btn)
        recyclerView = view.findViewById(R.id.recentTransactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        TransactionItems = mutableListOf()
        TransactionAdapter = TransactionAdapter(TransactionItems)
        recyclerView.adapter = TransactionAdapter
        loadProfileImageFromFirebaseStorage()
        fetchTransactions()
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