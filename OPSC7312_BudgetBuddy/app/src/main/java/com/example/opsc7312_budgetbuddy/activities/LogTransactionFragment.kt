package com.example.opsc7312_budgetbuddy.activities

import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.opsc7312_budgetbuddy.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogTransactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogTransactionFragment: Fragment() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_transaction, container, false)
        val dbHelper = DatabaseHelper(requireContext())
        val transactionName: EditText = view.findViewById(R.id.transactionNameInput)
        val transactionAmount: EditText = view.findViewById(R.id.amountInput)
        val categorySpinner: Spinner = view.findViewById(R.id.categoryInput)

        val createButton: Button = view.findViewById(R.id.createTransactionButton)

        createButton.setOnClickListener {
            val name = transactionName.text.toString()
            val amountText = transactionAmount.text.toString()
            val amount: Double = amountText.toDouble()
            val category = categorySpinner.selectedItem.toString()

            // Get current date, month, year
            val dateFormat = SimpleDateFormat("dd MMMM yyyy") // Changed to include day
            val calendar = Calendar.getInstance()
            val currentDate = dateFormat.format(calendar.time)

        }
        return view
    }


}