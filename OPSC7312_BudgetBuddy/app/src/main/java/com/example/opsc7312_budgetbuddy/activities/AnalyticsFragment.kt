package com.example.opsc7312_budgetbuddy.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.opsc7312_budgetbuddy.R
import com.example.opsc7312_budgetbuddy.activities.interfaces.TransactionApi
import com.example.opsc7312_budgetbuddy.activities.models.TransactionModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar

class AnalyticsFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var horizontalBarChart: HorizontalBarChart
    private lateinit var retrofit: Retrofit
    private lateinit var transactionApiService: TransactionApi
    private var categoryNames = ArrayList<String>()
    private lateinit var tooltip: PopupWindow
    private lateinit var currentMonthYear: TextView
    private lateinit var previousImgView: ImageView
    private lateinit var nextImgView: ImageView
    private lateinit var saveGraphBtn : Button
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.notification_btn)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl("https://budgetapp-amber.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        transactionApiService = retrofit.create(TransactionApi::class.java)

        pieChart = view.findViewById(R.id.pieChart1)
        horizontalBarChart = view.findViewById(R.id.horizontalBarGraph)

        val dateFormat = SimpleDateFormat("MMMM, yyyy")
        val calendar = Calendar.getInstance()

        currentMonthYear = view.findViewById(R.id.current_Month_Year)
        currentMonthYear.text = dateFormat.format(calendar.time)

        previousImgView = view.findViewById(R.id.previous_Img_View)
        previousImgView.setOnClickListener()
        {
            calendar.add(Calendar.MONTH, -1)
            currentMonthYear.text = dateFormat.format(calendar.time)
        }

        nextImgView = view.findViewById(R.id.next_Img_View)
        nextImgView.setOnClickListener()
        {
            calendar.add(Calendar.MONTH, 1)
            currentMonthYear.text = dateFormat.format(calendar.time)
        }

        // The issue with the highlighted value not disappearing until you click twice
        // might be due to the way the onNothingSelected() method is being triggered.
        // In the MPAndroidChart library, the onNothingSelected() method is called only when a selection is cleared,
        // which often requires a second click outside the currently highlighted value.
        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is PieEntry && h != null) {
                    showTooltip(e.label, e.value, h)
                }
                else{
                    pieChart.highlightValue(null)
                }
            }

            override fun onNothingSelected() {
                // Dismiss tooltip when nothing is selected
                tooltip.dismiss()
                // Clear the highlighted slice
                pieChart.highlightValue(null)
                // Force the chart to redraw
                // pieChart.invalidate()
            }
        })

        saveGraphBtn = view.findViewById(R.id.saveGraphBtn)
        saveGraphBtn.setOnClickListener()
        {
            saveChartToStorage()
        }

        fetchTransactions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupTooltip(inflater)
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    private fun generatePieChartCenterText(): SpannableString {
        val s = SpannableString("EXPENSES")
        s.setSpan(RelativeSizeSpan(2f), 0, 8, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 8, s.length,0)
        return s
    }

    private fun initialisePieChart(){
        pieChart.description.isEnabled = false

        pieChart.centerText = generatePieChartCenterText()
        pieChart.setCenterTextSize(10f)

        // radius of the center hole in percent of maximum radius
        pieChart.holeRadius = 45f
        pieChart.transparentCircleRadius = 50f

        pieChart.isHighlightPerTapEnabled = true

        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.setDragDecelerationFrictionCoef(0.95f)

        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textSize = 12f
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 7f
        l.yOffset = 0f
    }

    private fun setPieChartDataSet(entries : ArrayList<PieEntry>) {
        val dataSet1 = PieDataSet(entries, "Monthly Expenses")
        dataSet1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        dataSet1.sliceSpace = 2f
        dataSet1.valueTextColor = R.color.white
        dataSet1.valueTextSize = 14f

        val data = PieData(dataSet1)

        pieChart.data = data
        //pieChart.data.setValueFormatter(PercentValueFormatter())
        pieChart.data.setDrawValues(false) // Hide data
        pieChart.setDrawEntryLabels(false) // Hide entry labels

        pieChart.invalidate()
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.setEntryLabelColor(Color.BLACK)

        //pieChart.setUsePercentValues(true)
    }

    private fun initialiseHorizontalBarGraph(){
        horizontalBarChart.setDrawBarShadow(false)

        val description = Description()
        description.text = ""
        horizontalBarChart.description = description
        horizontalBarChart.legend.isEnabled = false
        horizontalBarChart.setPinchZoom(false)
        horizontalBarChart.setDrawValueAboveBar(false)

        //Display the axis on the left (contains the labels 1*, 2* and so on)
        val xAxis = horizontalBarChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(categoryNames)
        xAxis.isEnabled = true
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 12f
        xAxis.granularity = 1f
//        xAxis.setCenterAxisLabels(true)
        xAxis.yOffset = 20f

        val yLeft = horizontalBarChart.axisLeft

        //Set the minimum and maximum bar lengths as per the values that they represent
        yLeft.axisMinimum = 0f
        yLeft.isEnabled = false

        val yRight = horizontalBarChart.axisRight
        yRight.setDrawAxisLine(true)
        yRight.setDrawGridLines(false)
        yRight.isEnabled = false
    }

    private fun setHorizontalBarGraphData(entries: ArrayList<BarEntry>){
        val barDataSet = BarDataSet(entries, "Bar Data Set")

        //Set bar shadows
        horizontalBarChart.setDrawBarShadow(true)
        barDataSet.barShadowColor = Color.argb(40, 150, 150, 150)
        barDataSet.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        barDataSet.valueTextSize = 12f

        barDataSet.valueFormatter = object : ValueFormatter(){
            override fun getBarLabel(barEntry: BarEntry?): String {
                // https://stackoverflow.com/questions/60564707/kotlin-float-number-with-2-decimals-to-string-without-precision-loss
                return "${"%.1f".format(barEntry?.y)}%"
            }
        }

        val data = BarData(barDataSet)

        //Set the bar width
        //Note : To increase the spacing between the bars set the value of barWidth to < 1f
        data.barWidth = 0.6f

        //Finally set the data and refresh the graph
        horizontalBarChart.data = data

        horizontalBarChart.animateY(2000)

        horizontalBarChart.setFitBars(true)

        horizontalBarChart.invalidate()
    }

    // Method to fetch the transaction count from the API
    private fun fetchTransactions() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        transactionApiService.getAllTransactions(userId).enqueue(object :
            Callback<List<TransactionModel>> {
            override fun onResponse(
                call: Call<List<TransactionModel>>,
                response: Response<List<TransactionModel>>
            ) {
                if (response.isSuccessful) {
                    val transactions = response.body() ?: emptyList()
                    if(transactions.isEmpty()){
                        Toast.makeText(activity, "No chart data available. Please add transactions first", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val sumsOfTransactions = getTransactionSums(transactions)

                        val pieEntries = ArrayList<PieEntry>()
                        val barEntries = ArrayList<BarEntry>()
                        var positionInGraph = 0f

                        for ((key, value) in sumsOfTransactions.entries) {
                            pieEntries.add(PieEntry(value, key))
                            barEntries.add(BarEntry(positionInGraph, value))
                            if (!categoryNames.contains(key)){
                                categoryNames.add(key)
                            }
                            positionInGraph++
                        }
                        initialisePieChart()
                        initialiseHorizontalBarGraph()
                        setPieChartDataSet(pieEntries)
                        setHorizontalBarGraphData(barEntries)
                    }
                }
                else {
                    Log.e("API Error", "Error fetching transactions")
                }
            }
            override fun onFailure(call: Call<List<TransactionModel>>, t: Throwable) {
                Log.e("API Failure", "Failed to fetch transactions", t)
            }
        })
    }

    private fun getTransactionSums(transactions : List<TransactionModel>): MutableMap<String, Float> {
        val categorySums: MutableMap<String, Float> = HashMap()

        val totalTransactionAmount = transactions.sumOf { it.transactionAmount }
        for (transaction in transactions) {
            val category: String = transaction.categoryName
            val amount: Float = transaction.transactionAmount.toFloat()
            val percentageAmount = ( (amount/totalTransactionAmount) * 100).toFloat()
            categorySums[category] = categorySums.getOrDefault(category, 0f) + percentageAmount
        }
        return categorySums
    }

    private fun setupTooltip(inflater: LayoutInflater) {
        // Inflate the tooltip layout
        val tooltipView = inflater.inflate(R.layout.pie_tooltip, null)

        // Create the PopupWindow
        tooltip = PopupWindow(tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true)

        // Set up the TextView for the tooltip
        tooltipView.findViewById<TextView>(R.id.tooltip_text)
    }

    private fun showTooltip(label: String, value: Float, highlight: Highlight?) {
        // Prepare the tooltip text
        val tooltipText = "$label: ${"%.1f".format(value)}%"
        tooltip.contentView.findViewById<TextView>(R.id.tooltip_text).text = tooltipText

        // Calculate the position for the tooltip based on the highlight
        // X offset from the pie slice
        val tooltipXOffset = 20
        // Y offset to position above the slice
        val tooltipYOffset = -20

        // Get the position of the pie chart
        val location = IntArray(2)
        pieChart.getLocationOnScreen(location)

        // Calculate the tooltip position based on the highlight coordinates
        val tooltipX = location[0] + highlight!!.x.toInt() + tooltipXOffset
        val tooltipY = location[1] + highlight.y.toInt() + tooltipYOffset

        // Check if the tooltip is already showing
        // Reduce overhead and improves performance
        if (tooltip.isShowing) {
            // Update position
            tooltip.update(tooltipX, tooltipY, -1, -1)
        } else {
            tooltip.showAtLocation(pieChart, Gravity.NO_GRAVITY, tooltipX, tooltipY)
        }
    }

    // https://code.tutsplus.com/add-charts-to-your-android-app-using-mpandroidchart--cms-23335t
    private fun saveChartToStorage(){
        try {
            pieChart.saveToGallery("BudgetBuddy_PieChart1.jpg", 100) // 100 is the quality of the image
            horizontalBarChart.saveToGallery("BudgetBuddy_HorizontalBarChart1.jpg", 100)
            // https://stackoverflow.com/questions/10770055/use-toast-inside-fragment
            Toast.makeText(activity, "Graph saved successfully to storage.", Toast.LENGTH_SHORT).show()
        }
        catch (e : Exception){
            Toast.makeText(activity, "Failed to save graph to storage.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up references to avoid memory leaks

        // Detach listeners to avoid memory leaks
        previousImgView.setOnClickListener(null)
        nextImgView.setOnClickListener(null)
        saveGraphBtn.setOnClickListener(null)
        // Remove listener
        pieChart.setOnChartValueSelectedListener(null)

        // Optionally nullify references
        pieChart.clear()
        horizontalBarChart.clear()
    }
}
