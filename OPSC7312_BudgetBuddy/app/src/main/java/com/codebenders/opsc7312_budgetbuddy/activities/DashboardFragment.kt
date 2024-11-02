package com.codebenders.opsc7312_budgetbuddy.activities

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codebenders.opsc7312_budgetbuddy.R
import com.codebenders.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.codebenders.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetItem
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.codebenders.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardFragment : Fragment() {

    // Global variables
    private var circularTotalBudget: Double = 0.0
    private var circularAvailableBudget: Double = 0.0

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var budgetItems: MutableList<BudgetItem>
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var totalBudgetTextView: TextView
    private lateinit var availableBudgetForMonth: TextView

    private lateinit var retrofit: Retrofit
    private lateinit var budgetApiService: BudgetApi
    private lateinit var transactionApiService: TransactionApi

    private lateinit var circularProgressBarBudget: CircularProgressBar
    private lateinit var circularProgressBarSpent: CircularProgressBar

    private lateinit var sqliteHelper: DatabaseHelper

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    companion object {
        var PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isOfflineMode = sharedPref.getBoolean("offlineMode", false)
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //Initialize elements
        InitElements(view)

        budgetApiService = retrofit.create(BudgetApi::class.java)
        transactionApiService = retrofit.create(TransactionApi::class.java)

        // Display a welcome message with the users name for added personalization
        view.findViewById<TextView>(R.id.welcomeTextView).text = "Welcome, $userName"

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        budgetItems = mutableListOf()
        budgetAdapter = BudgetAdapter(budgetItems)
        recyclerView.adapter = budgetAdapter

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, trigger the notification in updateBudget
                triggerNotification((circularAvailableBudget / circularTotalBudget) * 100, circularTotalBudget, circularAvailableBudget)
            } else {
                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if(isOfflineMode){
            handleOfflineMode(requireContext())
        }else{
            fetchTotalBudget()
            fetchTransactionCount()
            fetchRemainingBudget()
            fetchBudgets()
            updateCircularProgressBars(circularTotalBudget,circularAvailableBudget)
            loadProfileImageFromFirebaseStorage()
        }
    }

    private fun InitElements(view: View){
        sqliteHelper = DatabaseHelper(requireContext())

        circularProgressBarBudget = view.findViewById(R.id.circularProgressBarBudget)
        circularProgressBarSpent = view.findViewById(R.id.circularProgressBarSpent)
        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)
        profileImageView = view.findViewById(R.id.account_btn)
        recyclerView = view.findViewById(R.id.budgetBreakdownRecyclerView)
        totalBudgetTextView = view.findViewById(R.id.availableBudget)
        availableBudgetForMonth = view.findViewById(R.id.availableBudgetForMonth)
    }

    // Method to fetch Category Name and Amount from the API for Budget Breakdown
    private fun fetchBudgets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create an instance of budgetCRUD
        val budgetCRUD = budgetCRUD()

        budgetCRUD.getBudgets(onSuccess = { budgetModels ->
            // Extract categories from each BudgetModel
            val budgetItems = mutableListOf<BudgetItem>()

            for (budgetModel in budgetModels) {
                for (category in budgetModel.categories) {
                    budgetItems.add(
                        BudgetItem(
                            categoryName = category.categoryName,
                            amount = category.amount
                        )
                    )
                }
            }

            // Update the adapter with the new data
            if (budgetItems.isNotEmpty()) {
                val adapter = BudgetAdapter(budgetItems)
                recyclerView.adapter = adapter
            }
        }, onError = { errorMessage ->
            Log.e("API Error", errorMessage)
        })
    }

    // Method to fetch the total available budget for the month from the API
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
                        sqliteHelper.saveBudgetsToLocalDatabase(budgets)

                        val latestBudget = budgets.maxByOrNull { it.month }
                        val totalBudget = latestBudget?.totalBudget ?: 0.0
                        circularTotalBudget = totalBudget
                        totalBudgetTextView.text = "${totalBudget}"
                    } else {
                        totalBudgetTextView.text = "R0.00"
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

    // Method to fetch the transaction count from the API
    private fun fetchTransactionCount() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        transactionApiService.getAllTransactions(userId).enqueue(object : Callback<List<TransactionModel>> {
            override fun onResponse(
                call: Call<List<TransactionModel>>,
                response: Response<List<TransactionModel>>
            ) {
                if (response.isSuccessful) {
                    val transactions = response.body() ?: emptyList()
                    val transactionCount = transactions.size

                    sqliteHelper.saveTransactionsToLocalDatabase(transactions)

                    view?.findViewById<TextView>(R.id.totalTransactions)?.text = "$transactionCount"
                } else {
                    Log.e("API Error", "Error fetching transactions")
                }
            }

            override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch transactions", t)
            }
        })
    }

    // Method to fetch and display the remaining budget
    private fun fetchRemainingBudget() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val budgetCall = budgetApiService.getBudgetsByUserId(userId)
        val transactionCall = transactionApiService.getAllTransactions(userId)

        // Make parallel API calls
        budgetCall.enqueue(object : Callback<List<BudgetModel>> {
            override fun onResponse(
                call: Call<List<BudgetModel>>,
                response: Response<List<BudgetModel>>
            ) {
                if (response.isSuccessful) {
                    val budgets = response.body() ?: emptyList()
                    val latestBudget = budgets.maxByOrNull { it.month }
                    val totalBudget = latestBudget?.totalBudget ?: 0.0
                    circularTotalBudget = totalBudget // Update global variable

                    // Fetch transactions and calculate remaining budget
                    transactionCall.enqueue(object : Callback<List<TransactionModel>> {
                        override fun onResponse(
                            call: Call<List<TransactionModel>>,
                            response: Response<List<TransactionModel>>
                        ) {
                            if (response.isSuccessful) {
                                val transactions = response.body() ?: emptyList()
                                val totalSpent = transactions.sumOf { it.transactionAmount }
                                val remainingBudget = totalBudget - totalSpent
                                circularAvailableBudget = remainingBudget // Update global variable

                                // Update the correct TextView
                                totalBudgetTextView.text = "R$totalBudget"
                                availableBudgetForMonth.text = "R$remainingBudget"

                                // Now update the progress bars with the correct values
                                updateCircularProgressBars(circularTotalBudget, circularAvailableBudget)
                            } else {
                                Log.e("API Error", "Error fetching transactions for remaining budget")
                            }
                        }

                        override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                            Log.e("API Failure", "Failed to fetch transactions for remaining budget", t)
                        }
                    })
                } else {
                    Log.e("API Error", "Error fetching budget for remaining budget calculation")
                }
            }

            override fun onFailure(call: Call<List<BudgetModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch budget for remaining budget calculation", t)
            }
        })
    }

    // Method to update circular progress bar with budget values
    fun updateCircularProgressBars(totalBudget: Double, availableBudget: Double) {
        if (totalBudget > 0) {
            // Calculate percentage spent and remaining
            val val1 = totalBudget * 100
            val val2 = (availableBudget / totalBudget) * 100

            // Check permissions before triggering notification
            checkNotificationsPermissionsBeforeTrigger()

            // Update progress bars
            circularProgressBarSpent.setProgressWithAnimation(val1.toFloat(), 1000) // Spent amount
            circularProgressBarBudget.setProgressWithAnimation(val2.toFloat(), 1000) // Remaining amount
        } else {
            // Handle the case where totalBudget is 0 to avoid division by zero
            circularProgressBarSpent.setProgressWithAnimation(0f, 1000)
            circularProgressBarBudget.setProgressWithAnimation(0f, 1000)
        }
    }

    private fun loadProfileImageFromFirebaseStorage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("ProfileImages/$userId.jpg")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(profileImageView)

            sqliteHelper.saveProfilePicture(1, uri.toString())
        }.addOnFailureListener {
            Log.e("Firebase Storage", "Error loading image", it)
        }
    }

    //This will call all the offline methods specific to the local db
    private fun handleOfflineMode(currentContext: Context) {
        val storedUserId = sqliteHelper.getStoredUserId(currentContext)

        if (storedUserId == null) {
            showLoginReminder(currentContext)
        } else {
            //Gets all necessary data from the local db
            val categories = sqliteHelper.getCategoriesForBudget(storedUserId)
            val transactionCount = sqliteHelper.getTransactionCount(storedUserId)
            val totalBudget = sqliteHelper.getTotalBudgetForLatestMonth(storedUserId)
            val totalSpent = sqliteHelper.getTotalSpentFromLocalDatabase()
            val uri = sqliteHelper.getProfilePicture()

            //Displays the total budget
            if (totalBudget != 0.0) {
                circularTotalBudget = totalBudget
                totalBudgetTextView.text = "${totalBudget}"
            } else {
                totalBudgetTextView.text = "R0.00"
            }

            //Displays the Transaction count
            view?.findViewById<TextView>(R.id.totalTransactions)?.text = "$transactionCount"

            //Displays the budget items
            val budgetItems = mutableListOf<BudgetItem>()

            for (category in categories) {
                budgetItems.add(
                    BudgetItem(
                        categoryName = category.categoryName,
                        amount = category.amount
                    )
                )
            }

            if (budgetItems.isNotEmpty()) {
                val adapter = BudgetAdapter(budgetItems)
                recyclerView.adapter = adapter
            }

            //Displays the remaining budget and updates the progress bar
            val remainingBudget = totalBudget - totalSpent
            circularAvailableBudget = remainingBudget
            availableBudgetForMonth.text = "R$remainingBudget"
            updateCircularProgressBars(circularTotalBudget, circularAvailableBudget)

            //Displays the users profile picture
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(profileImageView)
        }
    }

    private fun showLoginReminder(currentContext: Context) {
        AlertDialog.Builder(currentContext)
            .setTitle("Login Required")
            .setMessage("You need to log in at least once while online to use offline features.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Blog post
    // Title: Create and manage notification channels
    // Posted by: Google for Developers
    // Available at: https://developer.android.com/develop/ui/views/notifications/channels#:~:text=To%20create%20a%20notification%20channel%2C%20follow%20these%20steps%3A,the%20notification%20channel%20by%20passing%20it%20to%20createNotificationChannel%28%29.
    private fun createNotificationChannel() {
        try {
            val channelId = "budget_tracker_channel"
            val channelName = "Budget Tracker Notifications"
            val channelDescription = "Notifications for your budget tracker"

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }

            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {
            Log.e("NotificationChannel", "Error creating notification channel: ${e.message}")
        }
    }

    // Blog post
    // Title: Create and manage notification channels
    // Posted by: Google for Developers
    // Available at: https://developer.android.com/develop/ui/views/notifications/channels#:~:text=To%20create%20a%20notification%20channel%2C%20follow%20these%20steps%3A,the%20notification%20channel%20by%20passing%20it%20to%20createNotificationChannel%28%29.
    private fun showNotification(title: String, message: String) {
        try {
            val notificationId = 1
            val channelId = "budget_tracker_channel"

            val intent = Intent(requireContext(), DashboardFragment::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("FRAGMENT_TO_LOAD", "NOTIFICATIONS")
            }

            val pendingIntent = PendingIntent.getActivity(
                requireContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e("ShowNotification", "Error showing notification: ${e.message}")
        }
    }

    private fun triggerNotification(val2: Double, totalBudget: Double, availableBudget: Double) {
        createNotificationChannel()
        val amountRemaining = totalBudget - availableBudget

        // Blog post
        // Title: Kotlin when expression
        // Posted by: Praveenruhil
        // Available at: https://www.geeksforgeeks.org/kotlin-when-expression/
        when {
            val2 == 90.0 -> showNotification("Budget Alert", "You have R$$amountRemaining remaining of your set budget for the month!")
            val2 == 100.0 -> showNotification("Budget Alert", "You have reached your set budget for the month!")
            val2 > 100 -> showNotification("Budget Alert", "You have exceeded your budget for the month!")
        }
    }

    // Blog post
    // Title: Create and manage notification channels
    // Posted by: Google for Developers
    // Available at: https://developer.android.com/develop/ui/views/notifications/channels#:~:text=To%20create%20a%20notification%20channel%2C%20follow%20these%20steps%3A,the%20notification%20channel%20by%20passing%20it%20to%20createNotificationChannel%28%29.
    private fun obtainPermissions(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Call the provided callback if permission is already granted
                onPermissionGranted()
            }
        } else {
            // If below API level Tiramisu, no need to request permission
            onPermissionGranted()
        }
    }

    private fun checkNotificationsPermissionsBeforeTrigger() {
        obtainPermissions {
            triggerNotification((circularAvailableBudget / circularTotalBudget) * 100, circularTotalBudget, circularAvailableBudget)
        }
    }
}