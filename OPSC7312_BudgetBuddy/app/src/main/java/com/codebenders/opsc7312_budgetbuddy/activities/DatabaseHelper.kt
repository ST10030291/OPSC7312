package com.codebenders.opsc7312_budgetbuddy.activities

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import com.codebenders.opsc7312_budgetbuddy.activities.models.BudgetModel
import com.codebenders.opsc7312_budgetbuddy.activities.models.Category
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionCRUD
import com.codebenders.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.google.gson.Gson

/*Code Attribution:
This code was taken from a YouTube Video
Upload by: Stevdza-San
Titled: SQLite + Android - Create Database Schema (Book Library App) | Part 1
Available at: https://youtu.be/hJPk50p7xwA?si=wfK7upSTeex7bvX3
Accessed: 16 October 2024*/

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "budgetBuddy.db"
        private const val DATABASE_VERSION = 4

        //Profile table
        private const val TABLE_PROFILE = "Profile"
        private const val COLUMN_PROFILE_ID = "id"
        private const val COLUMN_PROFILE_PICTURE = "profile_picture"

        //Budget table
        private const val TABLE_BUDGET = "Budget"
        private const val COLUMN_BUDGET_ID = "budget_id"
        private const val COLUMN_BUDGET_USER_ID = "user_id"
        private const val COLUMN_BUDGET_MONTH = "month"
        private const val COLUMN_BUDGET_TOTAL = "total_budget"
        private const val COLUMN_CATEGORIES = "categories"

        //Transaction table
        private const val TABLE_TRANSACTION = "Transactions"
        private const val COLUMN_TRANSACTION_USER_ID = "user_id"
        private const val COLUMN_TRANSACTION_NAME = "transaction_name"
        private const val COLUMN_TRANSACTION_AMOUNT = "transaction_amount"
        private const val COLUMN_TRANSACTION_CATEGORY_NAME = "category_name"
        private const val COLUMN_TRANSACTION_DATE = "transaction_date"
        private const val COLUMN_IS_SYNCED = "is_synced"

    }

    //This will be used to create all the tables for the local db
    override fun onCreate(db: SQLiteDatabase) {
        val createBudgetTable = """CREATE TABLE $TABLE_BUDGET (
            $COLUMN_BUDGET_ID TEXT ,
            $COLUMN_BUDGET_USER_ID TEXT,
            $COLUMN_BUDGET_MONTH TEXT,
            $COLUMN_BUDGET_TOTAL REAL,
            $COLUMN_CATEGORIES TEXT
        )"""
        db.execSQL(createBudgetTable)

        val createTransactionTable = """CREATE TABLE $TABLE_TRANSACTION (
            $COLUMN_TRANSACTION_USER_ID TEXT,
            $COLUMN_TRANSACTION_NAME TEXT NOT NULL,
            $COLUMN_TRANSACTION_AMOUNT REAL NOT NULL,
            $COLUMN_TRANSACTION_CATEGORY_NAME TEXT NOT NULL,
            $COLUMN_TRANSACTION_DATE TEXT NOT NULL,
            $COLUMN_IS_SYNCED INTEGER DEFAULT 1
        )"""
        db.execSQL(createTransactionTable)

        val createProfileTable = """
            CREATE TABLE $TABLE_PROFILE (
                $COLUMN_PROFILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROFILE_PICTURE TEXT
            );
        """.trimIndent()
        db.execSQL(createProfileTable)
    }

    //This will delete the tables in the local db and recreate them
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTION")
        onCreate(db)
    }

    //This will simply add a budget into the budgets table
    fun addBudget(budget: BudgetModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BUDGET_USER_ID, budget.userId)
            put(COLUMN_BUDGET_MONTH, budget.month)
            put(COLUMN_BUDGET_TOTAL, budget.totalBudget)

        }
        val id = db.insert(TABLE_BUDGET, null, values)
        db.close()
        return id
    }

    //This gets the categories of the latest budget
    fun getCategoriesForBudget(userId: String): List<Category> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BUDGET,
            arrayOf(COLUMN_CATEGORIES),
            "$COLUMN_BUDGET_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )

        var categories: List<Category> = listOf()
        if (cursor.moveToFirst()) {
            val categoriesJson = cursor.getString(cursor.run { getColumnIndex(COLUMN_CATEGORIES) })
            Log.d("",categoriesJson.toString())
            categories = Gson().fromJson(categoriesJson, Array<Category>::class.java).toList()
            Log.d("",categories.toString())
        }

        cursor.close()
        return categories
    }

    //This syncs the firebase db to the local db for budgets
    fun saveBudgetsToLocalDatabase(budgets: List<BudgetModel>) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            db.execSQL("DELETE FROM $TABLE_BUDGET")

            val gson = Gson()

            for (budget in budgets) {
                val categoriesJson = gson.toJson(budget.categories).toString()

                val contentValues = ContentValues().apply {
                    put(COLUMN_BUDGET_ID, budget.id)
                    put(COLUMN_BUDGET_USER_ID, budget.userId)
                    put(COLUMN_BUDGET_MONTH, budget.month)
                    put(COLUMN_BUDGET_TOTAL, budget.totalBudget)
                    put(COLUMN_CATEGORIES, categoriesJson)
                }

                db.insertWithOnConflict(TABLE_BUDGET, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    //This gets the latest budgets total value
    fun getTotalBudgetForLatestMonth(userId: String): Double {
        val db = readableDatabase

        val cursor = db.query(
            TABLE_BUDGET,
            arrayOf(COLUMN_BUDGET_TOTAL, COLUMN_BUDGET_MONTH),
            "$COLUMN_BUDGET_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            "$COLUMN_BUDGET_MONTH DESC",
            "1"
        )

        var totalBudget = 0.0
        if (cursor.moveToFirst()) {
            totalBudget = cursor.getDouble(cursor.run { getColumnIndex(COLUMN_BUDGET_TOTAL) })
        }

        cursor.close()
        return totalBudget
    }

    //This will simply add a transaction into the transactions table
    fun addTransaction(transaction: TransactionModel): Long {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TRANSACTION_USER_ID, transaction.userId)
            put(COLUMN_TRANSACTION_NAME, transaction.transactionName)
            put(COLUMN_TRANSACTION_AMOUNT, transaction.transactionAmount)
            put(COLUMN_TRANSACTION_CATEGORY_NAME, transaction.categoryName)
            put(COLUMN_TRANSACTION_DATE, transaction.transactionDate)
            put(COLUMN_IS_SYNCED, 0)
        }
        val id = db.insert(TABLE_TRANSACTION, null, values)
        db.close()
        return id
    }

    //This gets the total number of transactions the user has created
    fun getTransactionCount(userId: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTION,
            arrayOf("COUNT(*) AS count"),
            "$COLUMN_TRANSACTION_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )

        var transactionCount = 0
        if (cursor.moveToFirst()) {
            transactionCount = cursor.getInt(cursor.run { getColumnIndex("count") })
        }

        cursor.close()
        return transactionCount
    }

    //This gets the transactions from the local db
    fun getLocalTransactions(): List<TransactionModel> {
        val transactions = mutableListOf<TransactionModel>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTION", null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.run { getColumnIndex(COLUMN_TRANSACTION_USER_ID) })
                val name = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_NAME)})
                val amount = cursor.getDouble(cursor.run {getColumnIndex(COLUMN_TRANSACTION_AMOUNT)})
                val category = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_CATEGORY_NAME)})
                val date = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_DATE)})

                val transactionModel = TransactionModel(userId, name, amount, category, date)
                transactions.add(transactionModel)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return transactions
    }

    //This syncs the local db to the firebase db
    fun syncOfflineTransactions() {
        val db = writableDatabase
         val transactionCRUD = TransactionCRUD()
         val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTION WHERE $COLUMN_IS_SYNCED = 0", null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.run { getColumnIndex(COLUMN_TRANSACTION_USER_ID) })
                val name = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_NAME)})
                val amount = cursor.getDouble(cursor.run {getColumnIndex(COLUMN_TRANSACTION_AMOUNT)})
                val category = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_CATEGORY_NAME)})
                val date = cursor.getString(cursor.run {getColumnIndex(COLUMN_TRANSACTION_DATE)})

                val transactionModel = TransactionModel(userId, name, amount, category, date)

                transactionCRUD.saveTransaction(transactionModel)

                val contentValues = ContentValues().apply {
                    put(COLUMN_IS_SYNCED, 1)
                }
                db.update(TABLE_TRANSACTION, contentValues, "$COLUMN_IS_SYNCED = 0", null)

            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    //This syncs the firebase db to the local db for transactions
    fun saveTransactionsToLocalDatabase(transactions: List<TransactionModel>) {
        val db = writableDatabase
        db.beginTransaction()

        try {
            db.execSQL("DELETE FROM $TABLE_TRANSACTION")

            for (transaction in transactions) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_TRANSACTION_USER_ID, transaction.userId)
                    put(COLUMN_TRANSACTION_AMOUNT, transaction.transactionAmount)
                    put(COLUMN_TRANSACTION_DATE, transaction.transactionDate)
                    put(COLUMN_TRANSACTION_NAME, transaction.transactionName)
                    put(COLUMN_TRANSACTION_CATEGORY_NAME, transaction.categoryName)
                    put(COLUMN_IS_SYNCED, 1)
                }

                db.insertWithOnConflict(TABLE_TRANSACTION, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    //This sums up all the transactions from the local db
    fun getTotalSpentFromLocalDatabase(): Double {
        val db = readableDatabase
        var totalSpent = 0.0

        val cursor = db.rawQuery("SELECT $COLUMN_TRANSACTION_AMOUNT FROM $TABLE_TRANSACTION", null)

        if (cursor.moveToFirst()) {
            do {
                val transactionAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT))
                totalSpent += transactionAmount
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return totalSpent
    }


    ////This stores tge user id in app settings after the user has logged in
    fun getStoredUserId(currentContext: Context): String? {
        val sharedPref = currentContext.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPref.getString("userId", null)
    }

    //Saves the profile picture to local db
    fun saveProfilePicture(profileId: Int, profilePictureUri: String): Long {
        val db = writableDatabase

        db.execSQL("DELETE FROM $TABLE_PROFILE")

        val values = ContentValues().apply {
            put(COLUMN_PROFILE_ID, profileId)
            put(COLUMN_PROFILE_PICTURE, profilePictureUri)
        }
        return db.insert(TABLE_PROFILE, null, values)
    }

    fun getProfilePicture(): Uri? {
        val db = readableDatabase
        var profilePictureUri: Uri? = null

        val cursor = db.rawQuery("SELECT $COLUMN_PROFILE_PICTURE FROM $TABLE_PROFILE", null)
        if (cursor.moveToFirst()) {
            val profilePictureString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PICTURE))
            profilePictureUri = Uri.parse(profilePictureString)
        }

        cursor.close()
        db.close()

        return profilePictureUri
    }


}