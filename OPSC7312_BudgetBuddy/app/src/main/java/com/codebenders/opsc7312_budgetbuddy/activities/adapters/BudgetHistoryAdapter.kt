package com.codebenders.opsc7312_budgetbuddy.activities.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codebenders.opsc7312_budgetbuddy.R
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetModel

class BudgetHistoryAdapter (private val budgets: MutableList<BudgetModel>,
                            private val context: Context
) : RecyclerView.Adapter<BudgetHistoryAdapter.BudgetViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_history_item, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val recipe = budgets[position]
        holder.bind(recipe, position)

    }

    override fun getItemCount(): Int {
        return budgets.size
    }

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val budgetMonth: TextView = itemView.findViewById(R.id.budget_month)
        private val budgetAmount: TextView = itemView.findViewById(R.id.budget_amount)

        @SuppressLint("SetTextI18n")
        fun bind(budget: BudgetModel, position: Int) {
            budgetMonth.text = budget.month
            budgetAmount.text = "R ${budget.totalBudget}"

        }
    }
}