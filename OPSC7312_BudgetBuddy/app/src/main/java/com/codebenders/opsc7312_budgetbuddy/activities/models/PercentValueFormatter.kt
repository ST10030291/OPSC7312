package com.codebenders.opsc7312_budgetbuddy.activities.models

import com.github.mikephil.charting.formatter.ValueFormatter


// Some of the code for this function was taken from a blog post
// Titled: Formatting Data and Axis Values
// Posted by: Weeklycoding.com
// Available at: https://weeklycoding.com/mpandroidchart-documentation/formatting-data-values/
// Accessed: 25 September 2024
class PercentValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${"%.1f".format(value)}%"
    }
}