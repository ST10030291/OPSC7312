package com.example.opsc7312_budgetbuddy.activities.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R

class BudgetAdapter(private val budgetItems: List<BudgetItem>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
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
        holder.categoryIcon.setImageResource(item.iconRes)
        holder.categoryName.text = item.category
        holder.categoryAmount.text = item.amount
    }

    override fun getItemCount(): Int = budgetItems.size
}

data class BudgetItem(val iconRes: Int, val category: String, val amount: String)
