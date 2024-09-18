package com.example.opsc7312_budgetbuddy.activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.adapters.BudgetHistoryAdapter
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class AccountFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPrefs"
    private val LANGUAGE_KEY = "language_key"
    private lateinit var budgetApiService: BudgetApi
    private lateinit var retrofit: Retrofit
    private lateinit var totalBudgetsTextView: TextView
    private lateinit var totalBudgetAmountTextView: TextView
    private lateinit var budgetAdapter: BudgetHistoryAdapter
    private var budgetList: MutableList<BudgetModel> = mutableListOf()
    private  lateinit var BudgetCRUD: budgetCRUD

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        BudgetCRUD = budgetCRUD()
        budgetApiService = retrofit.create(BudgetApi::class.java)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0)

        val userEmail = user.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.tv_name).text = userName
        view.findViewById<TextView>(R.id.tv_email).text = "$userEmail"

        // Load saved language preference
        val savedLanguage = sharedPreferences.getString(LANGUAGE_KEY, "en")
        val languageSwitch = view.findViewById<SwitchCompat>(R.id.languageSwitch)
        languageSwitch.isChecked = savedLanguage == "af"

        // notification button click listener
        view.findViewById<ImageView>(R.id.notification_btn)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        // language switch
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val languageCode = if (isChecked) "af" else "en"
            setLocale(languageCode)
            saveLanguagePreference(languageCode)
            restartFragment()
        }

        budgetAdapter = BudgetHistoryAdapter(budgetList, requireContext())
        val budgetRecycler = view.findViewById<RecyclerView>(R.id.BudgetHistoryListView)
        budgetRecycler.layoutManager = LinearLayoutManager(requireContext())
        budgetRecycler.adapter = budgetAdapter
        loadBudgets()
        fetchTotalBudget()
        totalBudgetsTextView = view.findViewById(R.id.tv_total_budgets)
        totalBudgetAmountTextView = view.findViewById(R.id.tv_total_budgeted)
    }

    private fun loadBudgets() {
        BudgetCRUD.getBudgets(
            onSuccess = { recipes ->
                budgetList.clear()
                budgetList.addAll(recipes)
                budgetAdapter.notifyDataSetChanged()
            },
            onError = {

            }
        )
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
    }

    private fun saveLanguagePreference(languageCode: String) {
        with(sharedPreferences.edit()) {
            putString(LANGUAGE_KEY, languageCode)
            apply()
        }
    }

    private fun restartFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AccountFragment())
            .commit()
    }

    private fun fetchTotalBudget() {

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        budgetApiService.getBudgetsByUserId(userId).enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(
                call: Call<List<BudgetModel>>,
                response: Response<List<BudgetModel>>
            ) {
                if (response.isSuccessful) {
                    val budgets = response.body() ?: emptyList()
                    if (budgets.isNotEmpty()) {
                        val totalBudgets = budgets.size
                        val totalBudgeted = budgets.sumOf { it.totalBudget }
                        totalBudgetsTextView.text = "${totalBudgets}"
                        totalBudgetAmountTextView.text = "$totalBudgeted"
                    } else {
                        totalBudgetAmountTextView.text = "No budgets found."
                    }
                } else {
                    Log.e("API Error", "Error fetching total budget")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch total budget", t)
            }
        })
    }
}
