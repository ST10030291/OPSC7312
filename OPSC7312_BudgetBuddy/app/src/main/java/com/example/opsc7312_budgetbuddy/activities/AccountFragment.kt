package com.example.opsc7312_budgetbuddy.activities

import android.Manifest
import android.content.ContentValues
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.adapters.BudgetHistoryAdapter
import com.example.opsc7312_budgetbuddy.activities.interfaces.BudgetApi
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.example.opsc7312_budgetbuddy.activities.models.BudgetAdapter
import com.example.opsc7312_budgetbuddy.activities.models.BudgetItem
import com.example.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.example.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.example.opsc7312_budgetbuddy.activities.models.TransactionItem
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.example.opsc7312_budgetbuddy.activities.models.budgetCRUD
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.util.Locale
import kotlin.properties.Delegates

class AccountFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPrefs"
    private val LANGUAGE_KEY = "language_key"
    private lateinit var budgetApiService: BudgetApi
    private lateinit var transactionApiService: TransactionApi
    private lateinit var retrofit: Retrofit
    private lateinit var totalBudgetsTextView: TextView
    private lateinit var totalBudgetAmountTextView: TextView
    private lateinit var budgetAdapter: BudgetHistoryAdapter
    private var budgetList: MutableList<BudgetModel> = mutableListOf()
    private var imageUri: Uri? = null
    private  lateinit var BudgetCRUD: budgetCRUD
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var  storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference


    private var transactionList: MutableList<TransactionItem> = mutableListOf()
    private var budgetnList: MutableList<BudgetModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseSetup()
        loadProfileImageFromFirebaseStorage()
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        val savedLanguage = sharedPreferences.getString(LANGUAGE_KEY, "en")
        val languageSwitch = view.findViewById<SwitchCompat>(R.id.languageSwitch)
        val changeProfilePicBtn = view.findViewById<ImageView>(R.id.change_profile_icon)
        val exportButton = view.findViewById<Button>(R.id.exportBtn)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return
        val userEmail = user.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        profileImageView = view.findViewById(R.id.profile_image)

        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        BudgetCRUD = budgetCRUD()
        budgetApiService = retrofit.create(BudgetApi::class.java)
        transactionApiService = retrofit.create(TransactionApi::class.java)

        view.findViewById<TextView>(R.id.tv_name).text = userName
        view.findViewById<TextView>(R.id.tv_email).text = "$userEmail"

        // Load saved language preference
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

        changeProfilePicBtn.setOnClickListener{
            selectImageLauncher.launch("image/*")
        }



        budgetAdapter = BudgetHistoryAdapter(budgetList, requireContext())
        val budgetRecycler = view.findViewById<RecyclerView>(R.id.BudgetHistoryListView)
        budgetRecycler.layoutManager = LinearLayoutManager(requireContext())
        budgetRecycler.adapter = budgetAdapter
        loadBudgets()
        fetchTotalBudget()
        importBudget()
        importTransactions()
        totalBudgetsTextView = view.findViewById(R.id.tv_total_budgets)
        totalBudgetAmountTextView = view.findViewById(R.id.tv_total_budgeted)

        exportButton.setOnClickListener {
            exportData()
        }
    }

    private fun importBudget(){
        val budgetCRUD = budgetCRUD()
        budgetCRUD.getBudgets(
            onSuccess = { recipes ->
                budgetnList.clear()
                budgetnList.addAll(recipes)
            },
            onError = {

            }
        )
    }
    private fun importTransactions(){
        val transactionCRUD = TransactionCRUD()

        transactionCRUD.getTransactions(onSuccess = { transactionModels ->
            for (transactionModel in transactionModels) {
                transactionList.add(
                    TransactionItem(
                        name = transactionModel.transactionName,
                        category = transactionModel.categoryName,
                        amount = transactionModel.transactionAmount.toString(),
                        date = transactionModel.transactionDate
                    )
                )
            }

        }, onError = { errorMessage ->
            Log.e("API Error", errorMessage)
        })
    }


    private fun exportData() {
        var isExported = false
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Budget_Buddy_Report.csv")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { fileWriter ->

                    for (budget in budgetnList) {
                        fileWriter.append("${budget.categories}, ${budget.totalBudget}\n")
                        isExported = true
                    }
                    for (transaction in transactionList) {
                        fileWriter.append("${transaction.category}, ${transaction.amount}\n")
                        isExported = true
                    }

                    fileWriter.flush()
                }
            }

            if (isExported) {
                Toast.makeText(requireContext(), "CSV file saved successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "CSV export failed!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Failed to create file URI!", Toast.LENGTH_LONG).show()
        }
    }


    //Sets up the database to reference the Profile images folder
    private fun databaseSetup()
    {
        storageRef = FirebaseStorage.getInstance().reference.child("ProfileImages")
        auth = FirebaseAuth.getInstance()
        databaseRef = Firebase.database.reference.child("Users").child(auth.currentUser?.uid!!)
    }

    //This loads all the users budgets in a list for later use
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

    //This will get All the budgets and display the total budgeted
    //as well as the total budgets made by the user
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

    //This will Save the new image and call the uploadImageToFirebase() to save to the database
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            profileImageView.setImageURI(it)
            uploadImageToFirebase()
        }
    }

    //This will simply add the new image to the database and respond with the appropriate message
    private fun saveImageUriToDatabase(uri: Uri)
    {
        databaseRef.child("profileImageUri").setValue(uri.toString())
            .addOnSuccessListener{
                Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(), "Failed to save image URI", Toast.LENGTH_SHORT).show()
            }
    }
    //This will simply add the new image to the database and respond with the appropriate message
    private fun uploadImageToFirebase() {
        imageUri?.let {
            val userId = auth.currentUser?.uid ?: return
            val fileRef = storageRef.child("$userId.jpg")
            fileRef.putFile(it)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageUriToDatabase(uri)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //This will load the user profile image from firebase storage using Glide library
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



}
