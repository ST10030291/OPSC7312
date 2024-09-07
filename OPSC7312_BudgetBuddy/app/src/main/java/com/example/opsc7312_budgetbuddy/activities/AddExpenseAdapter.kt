package com.example.opsc7312_budgetbuddy.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.models.Category

class AddExpenseAdapter(private val expenseList: MutableList<Category>) :
    RecyclerView.Adapter<AddExpenseAdapter.AddExpenseViewHolder>() {

     class AddExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categorySpinner: Spinner = itemView.findViewById(R.id.categoryExpense)
        val enterAmount: EditText = itemView.findViewById(R.id.enterAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_expense_item, parent, false)
        return AddExpenseViewHolder(itemView)
    }
    /*override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item_layout, parent, false)
        return CategoryViewHolder(view)
    }*/

    override fun onBindViewHolder(holder: AddExpenseViewHolder, position: Int) {
        val expense = expenseList[position]

        holder.categorySpinner.setSelection(getCategoryIndex(expense.categoryName))
        holder.enterAmount.setText(expense.amount.toString())



        holder.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                expense.categoryName = parent?.getItemAtPosition(pos).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val amountText = holder.enterAmount
        val amountString = amountText.toString()
        val amountEntered = amountString.toDouble()
        expense.amount = amountEntered
    }

    override fun getItemCount(): Int = expenseList.size

    private fun getCategoryIndex(category: String): Int {
        return 0
    }
}

/*
* class CategoryAdapter(private val categoryList: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //declare all variables here
        val categoryName: Button = itemView.findViewById(R.id.CategoryBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item_layout, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        //this is where you can set your values
        val category = categoryList[position]
        holder.categoryName.setText(category.categoryName)
        holder.categoryName.setBackgroundColor(colorChange(category.color).toColorInt())
        holder.categoryName.setTextColor(colorChangeText(category.color).toColorInt())
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

}
* */