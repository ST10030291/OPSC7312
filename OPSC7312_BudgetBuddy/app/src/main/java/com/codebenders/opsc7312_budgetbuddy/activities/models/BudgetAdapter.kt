package com.codebenders.opsc7312_budgetbuddy.activities.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codebenders.opsc7312_budgetbuddy.R

class BudgetAdapter(private val budgetItems: List<BudgetItem>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryAmount: TextView = itemView.findViewById(R.id.category_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val item = budgetItems[position]
        holder.categoryName.text = item.categoryName
        holder.categoryAmount.text = "R${item.amount}"
    }

    override fun getItemCount(): Int = budgetItems.size
}

data class BudgetItem(
    val categoryName: String = "",
    val amount: Double = 0.0
)
