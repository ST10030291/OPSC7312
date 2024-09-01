package com.example.opsc7312_budgetbuddy.activities

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    companion object{
        private val DATABASE_NAME = "budgetBuddy.db"
        private val DATABASE_VERSION = 1

        //Table 1
        const val TRANSACTION_TABLE = "transaction"
        const val TRANSACTION_ID = "id"
        const val TRANSACTION_NAME = "transactionName"
        const val TRANSACTION_AMOUNT = "transactionAmount"
        const val TRANSACTION_CATEGORY = "transactionCategory"
        const val TRANSACTION_MONTH = "transactionMonth"
        const val USER_ID = "userId"

        //Table 2
        const val EXPENSE_TABLE = "expense"
        const val EXPENSE_ID = "id"
        const val EXPENSE_CATEGORY = "expenseCategory"
        const val EXPENSE_AMOUNT = "expenseAmount"
        //const val USER_ID = "userId"

        //Table 3
        const val BUDGET_TABLE = "budget"
        const val BUDGET_ID = "id"
        const val BUDGET_AMOUNT = "budgetAmount"
        //const val USER_ID = "userId"

        private val ID = "id"
        //private val TRANSACTION_NAME = "transactionName"
        //private val TRANSACTION_AMOUNT = "transactionAmount"
        //private val TRANSACTION_CATEGORY = "transactionCategory"
       // private val USER_ID = "userId"

    }


    override fun onCreate(db: SQLiteDatabase?) {
        // Create the first table
        db?.execSQL(
            "CREATE TABLE $TRANSACTION_TABLE (" +
                    "$TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TRANSACTION_NAME TEXT NOT NULL," +
                    "$TRANSACTION_AMOUNT DOUBLE NOT NULL," +
                    "$TRANSACTION_CATEGORY TEXT NOT NULL," +
                    "$TRANSACTION_MONTH TEXT NOT NULL,"+
                    "$USER_ID TEXT NOT NULL)"
        )

        // Create the second table
        db?.execSQL(
            "CREATE TABLE $EXPENSE_TABLE (" +
                    "$BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$EXPENSE_CATEGORY TEXT NOT NULL, " +
                    "$EXPENSE_AMOUNT DOUBLE NOT NULL," +
                    "$USER_ID TEXT NOT NULL)"
        )

        // Create the third table
        db?.execSQL(
            "CREATE TABLE $BUDGET_TABLE (" +
                    "$EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$EXPENSE_CATEGORY TEXT NOT NULL, " +
                    "$BUDGET_AMOUNT DOUBLE NOT NULL, " +
                    "$USER_ID TEXT NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop old tables if they exist
        db?.execSQL("DROP TABLE IF EXISTS $TRANSACTION_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $EXPENSE_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $BUDGET_TABLE")

        onCreate(db)
    }
}