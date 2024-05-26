package com.wb.verbum.activities.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.wb.verbum.R
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.formatters.DayAxisValueFormatter
import com.wb.verbum.formatters.MonthAxisValueFormatter
import com.wb.verbum.formatters.WeekAxisValueFormatter
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Locale
import java.util.Random

class HomeFragmentStats : Fragment() {

    private lateinit var view: View
    private lateinit var userService: UserService
    private lateinit var user: User
    private lateinit var lineChart: LineChart
    private lateinit var games: MutableList<ExerciseInfo>

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.home_stats_layout, container, false)

        userService = UserService(AppDatabase.getDatabase(view.context).userDao())


        GlobalScope.launch(Dispatchers.Main) {
            user = withContext(Dispatchers.IO) {
                userService.getAllUsers()[0]
            }

            games = user.exerciseHistory!!

            lineChart = view.findViewById(R.id.lineChart)
            setupChart()
            loadDailyData()

            view.findViewById<ImageView>(R.id.buttonDaily).setOnClickListener {
                loadDailyData()
            }

            view.findViewById<ImageView>(R.id.buttonWeekly).setOnClickListener {
                loadWeeklyData()
            }

            view.findViewById<ImageView>(R.id.buttonMonthly).setOnClickListener {
                loadMonthlyData()
            }
        }

        return view
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.xAxis.setDrawGridLines(false)
    }

    private fun loadDailyData() {
        val (entries, labels) = getDailyData()
        val lineDataSet = LineDataSet(entries, "Rounds Played").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            setDrawCircleHole(false)
            setDrawCircles(true)
            setDrawValues(true)
        }
        val lineData = LineData(lineDataSet)
        lineChart.apply {
            data = lineData
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = DayAxisValueFormatter(labels.toTypedArray())
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            xAxis.spaceMax = 0.5f

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                setDrawInside(false)
            }

            invalidate()
        }
    }

    private fun loadWeeklyData() {
        val (entries, labels) = getWeeklyData()

        val lineDataSet = LineDataSet(entries, "Rounds Played").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            setDrawCircleHole(false)
            setDrawCircles(true)
            setDrawValues(true)
        }

        val lineData = LineData(lineDataSet)

        lineChart.apply {
            data = lineData
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = WeekAxisValueFormatter(labels.toTypedArray())
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            xAxis.spaceMax = 0.5f

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                setDrawInside(false)
            }

            invalidate()
        }
    }

    private fun loadMonthlyData() {
        val (entries, labels) = getMonthlyData()
        val lineDataSet = LineDataSet(entries, "Rounds Played").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            setDrawCircleHole(false)
            setDrawCircles(true)
            setDrawValues(true)
        }

        val lineData = LineData(lineDataSet)

        lineChart.apply {
            data = lineData
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = WeekAxisValueFormatter(labels.toTypedArray())
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            xAxis.spaceMax = 0.5f

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                setDrawInside(false)
            }

            invalidate()
        }
    }

    private fun getDailyData(): Pair<List<Entry>, List<String>> {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        val today = LocalDate.now()

        // Group games by date
        val groupedGames = games.groupBy { LocalDate.parse(it.startingTime?.substring(0, 10)) }

        // Iterate over the past 7 days
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.toString().substring(0, 3)
            val gamesOfDay = groupedGames[date] ?: emptyList()
            val roundsPlayed = gamesOfDay.sumBy { it.rounds?.size ?: 0 }
            entries.add(Entry((6 - i).toFloat(), roundsPlayed.toFloat()))
            labels.add(dayOfWeek)
        }

        return Pair(entries, labels)
    }

    private fun getWeeklyData(): Pair<List<Entry>, List<String>> {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())

        val groupedGames: Map<LocalDate, List<ExerciseInfo>> = games.groupBy {
            LocalDate.parse(it.startingTime?.substring(0, 10))
                .with(weekFields.dayOfWeek(), 1)
        }

        for (i in 5 downTo 1) {
            val weekStart = today.minusWeeks((i - 1).toLong()).with(weekFields.dayOfWeek(), 1)
            val weekLabel = weekStart.format(DateTimeFormatter.ofPattern("MMM d"))
            val gamesOfWeek = groupedGames[weekStart] ?: emptyList()
            val roundsPlayed = gamesOfWeek.sumBy { it.rounds?.size ?: 0 }
            entries.add(Entry((5 - i).toFloat(), roundsPlayed.toFloat()))
            labels.add(weekLabel)
        }

        return Pair(entries, labels)
    }

    private fun getMonthlyData(): Pair<List<Entry>, List<String>> {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        val today = LocalDate.now()

        // Group games by month
        val groupedGames: Map<Int, List<ExerciseInfo>> = games.groupBy {
            LocalDate.parse(it.startingTime?.substring(0, 10)).monthValue
        }

        // Iterate over the past 6 months, including the current month
        for (i in 5 downTo 0) {
            val monthStart = today.minusMonths(i.toLong()).with(TemporalAdjusters.firstDayOfMonth())
            val monthLabel = monthStart.format(DateTimeFormatter.ofPattern("MMM"))
            val gamesOfMonth = groupedGames[monthStart.monthValue] ?: emptyList()
            val roundsPlayed = gamesOfMonth.sumBy { it.rounds?.size ?: 0 }
            entries.add(Entry((5 - i).toFloat(), roundsPlayed.toFloat()))
            labels.add(monthLabel)
        }

        return Pair(entries, labels)
    }
}