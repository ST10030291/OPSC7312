package com.example.opsc7312_budgetbuddy.activities.models

import com.github.mikephil.charting.formatter.ValueFormatter

class PercentValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${"%.1f".format(value)}%"
    }
}