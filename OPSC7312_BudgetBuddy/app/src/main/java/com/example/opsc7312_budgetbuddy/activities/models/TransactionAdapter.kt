package com.example.opsc7312_budgetbuddy.activities.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R

class TransactionAdapter(private val transactions: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionName: TextView = itemView.findViewById(R.id.categoryExpense)
        val transactionAmount: TextView = itemView.findViewById(R.id.displayCost)
        val transactionCategory: TextView = itemView.findViewById(R.id.displayCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.transactionName.text = transaction.name
        holder.transactionAmount.text = "R" + transaction.amount
        holder.transactionCategory.text = transaction.category
    }

    override fun getItemCount(): Int = transactions.size
}

data class TransactionItem(val name: String, val category: String, val amount: String)
