package com.wb.verbum.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.wb.verbum.R
import com.wb.verbum.databinding.ActivityStatsBinding
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.GameStatus
import com.wb.verbum.model.User
import com.wb.verbum.service.UserService
import com.wb.verbum.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max


class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var user: User
    private lateinit var userService: UserService
    private lateinit var game: ExerciseInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatsBinding.inflate(layoutInflater)

        userService = UserService(AppDatabase.getDatabase(this).userDao())

        val gameId = intent.getStringExtra(Constants.INTENT_GAME_STAT)

        val title: TextView = binding.statsTitle
        val description: TextView = binding.statsDescription
        val date: TextView = binding.statsDate
        val status: TextView = binding.gameStatus
        val roundsTotalNumber: TextView = binding.roundsTotalNumber
        val correctRoundsNumber: TextView = binding.correctRoundsNumber
        val wrongRoundsNumber: TextView = binding.wrongRoundsNumber
        val roundsChart: PieChart = binding.roundsChart
        val binaryCorrectAnswer: LineChart = binding.binaryCorrectAnswer
        val reactionTimeChart: LineChart = binding.reactionTimeChart

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                user = userService.getAllUsers()[0]
                game = user.exerciseHistory?.find { it.id == gameId }!!

                title.text = game.name
                description.text = game.description
                val time = LocalDateTime.parse(
                    game.startingTime,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                date.text = time.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
                if (game.status == GameStatus.INCOMPLETE) {
                    status.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.orange
                        )
                    )
                } else {
                    status.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
                }
                status.text = game.status?.displayName ?: ""

                val totalRounds = "Numar de runde: " + (game.rounds?.filter { it.isCompleted }?.size
                    ?: 0)
                val correctRounds = "Corecte: " + (game.rounds?.filter { it.isSuccess }?.size ?: 0)
                val wrongRounds = "Gresite: " + (game.rounds?.filter { !it.isSuccess }
                    ?.filter { it.isCompleted }?.size ?: 0)

                roundsTotalNumber.text = totalRounds
                correctRoundsNumber.text = correctRounds
                wrongRoundsNumber.text = wrongRounds

                if (game.rounds?.filter { it.isCompleted }?.size != 0){

                    setupRoundsChart(
                        roundsChart,
                        ((game.rounds?.filter { it.isSuccess }?.size ?: 0).toFloat()),
                        (game.rounds?.filter { !it.isSuccess }?.filter { it.isCompleted }?.size
                            ?: 0).toFloat()
                    )

                    setupBinaryCorrectAnswer(binaryCorrectAnswer, game)
                    setupReactionTimeChart(
                        reactionTimeChart,
                        user.exerciseHistory!!.filter { it.name == game.name }.toList(),
                        game
                    )
                }

            }
        }
        sleep(300)
        setContentView(binding.root)
    }

    private fun setupReactionTimeChart(
        reactionTimeChart: LineChart,
        history: List<ExerciseInfo>,
        game: ExerciseInfo
    ) {
        // Define the data for the first line chart
        var gameEntryCounter = 1f
        val gameEntries = ArrayList<Entry>()
        for (round in game.rounds!!) {
            if (round.endTime == null) {
                continue
            }
            val endTime = LocalDateTime.parse(round.endTime)
            val startTime = LocalDateTime.parse(round.startTime)
            val duration = Duration.between(startTime, endTime)
            val secondsWithTwoDecimals = (duration.toMillis() / 1000.0).toFloat()
            val roundedSeconds = "%.2f".format(secondsWithTwoDecimals).toFloat()

            gameEntries.add(Entry(gameEntryCounter, roundedSeconds))
            gameEntryCounter += 1
        }

        // Define the data for the second line chart
        val overallEntries = ArrayList<Entry>()
        for (i in 0..<gameEntries.size) {
            var reactionTimeTotalRound = 0f
            var totalPastGamesConsidered = 0
            for (pastGame in history) {
                if ((pastGame.rounds?.size ?: 0) > i) {
                    val pastRound = pastGame.rounds?.get(i)

                    val endTime =
                        LocalDateTime.parse(pastRound?.endTime ?: LocalDateTime.now().toString())
                    val startTime =
                        LocalDateTime.parse(pastRound?.startTime ?: LocalDateTime.now().toString())
                    val duration = Duration.between(startTime, endTime)
                    val secondsWithTwoDecimals = (duration.toMillis() / 1000.0).toFloat()
                    val roundedSeconds = "%.2f".format(secondsWithTwoDecimals).toFloat()

                    if (roundedSeconds > 0.1 && roundedSeconds < 600) {
                        reactionTimeTotalRound += roundedSeconds
                        totalPastGamesConsidered += 1
                    }

                }
            }
            val roundNo = i + 1
            overallEntries.add(
                Entry(
                    roundNo.toFloat(),
                    reactionTimeTotalRound / totalPastGamesConsidered
                )
            )

        }

        // Create data sets and specify properties
        val dataSet1 = LineDataSet(gameEntries, "Timpul de reactie al jocului").apply {
            color = Color.BLUE
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setDrawValues(false) // Disable drawing values on the chart
        }

        val dataSet2 = LineDataSet(overallEntries, "Timul de reactie overall").apply {
            color = Color.RED
            lineWidth = 2f
            setCircleColor(Color.RED)
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = Color.BLACK
            setDrawValues(false) // Disable drawing values on the chart
        }

        // Create a data object with both data sets
        val data = LineData(dataSet1, dataSet2)

        // Set the data and other properties for the line chart
        reactionTimeChart.data = data
        reactionTimeChart.description.isEnabled = false

        // Customize X-Axis
        val xAxis = reactionTimeChart.xAxis
        xAxis.granularity = 1f // One unit interval
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 1f // Set minimum value
        xAxis.granularity = 1f // One unit interval
        xAxis.setLabelCount(gameEntries.size, true)
        xAxis.setDrawGridLines(false) // Optional: remove grid lines

        val maximumPeriod =
            max(gameEntries.map { s -> s.y }.max(), overallEntries.map { s -> s.y }.max())

        // Customize Y-Axis (Left)
        val leftAxis = reactionTimeChart.axisLeft
        leftAxis.axisMinimum = 0f // Adjust minimum value according to your data
        leftAxis.axisMaximum = maximumPeriod // Adjust maximum value according to your data
        leftAxis.granularity = 1f
        leftAxis.setLabelCount(10, true) // Adjust label count according to your data

        // Customize Y-Axis (Right)
        val rightAxis = reactionTimeChart.axisRight
        rightAxis.isEnabled = false
        leftAxis.isGranularityEnabled = true

        val legend = reactionTimeChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        // Refresh the chart
        reactionTimeChart.invalidate()
    }
}

fun setupRoundsChart(pieChart: PieChart, correctNumber: Float, wrongNumber: Float) {
    val entries = ArrayList<PieEntry>()
    entries.add(PieEntry(correctNumber, "Corecte"))
    entries.add(PieEntry(wrongNumber, "Gresite"))

    // Create a data set and specify colors
    val dataSet = PieDataSet(entries, "")
    dataSet.setColors(*intArrayOf(Color.BLUE, Color.YELLOW))

    // Create a data object with the data set
    val data = PieData(dataSet)
    data.setValueTextSize(10f)
    data.setValueTextColor(Color.RED)

    // Set the data and other properties for the pie chart
    pieChart.setData(data)
    pieChart.setUsePercentValues(true)
    pieChart.setEntryLabelColor(Color.WHITE)
    pieChart.setEntryLabelTextSize(12f)
    pieChart.getDescription().setEnabled(false)
    pieChart.setHoleRadius(30f)
    pieChart.setTransparentCircleRadius(35f)
    pieChart.setDrawEntryLabels(false)

    val legend = pieChart.legend
    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    legend.setDrawInside(false);

    // Refresh the chart
    pieChart.invalidate()
}

fun setupBinaryCorrectAnswer(lineChart: LineChart, game: ExerciseInfo) {

    // Define the data for the line chart
    val entries = ArrayList<Entry>()
    var noEntry = 1f;
    for (round in game.rounds!!) {
        if (!round.isCompleted) {
            continue
        }
        var yElement = 0f
        if (round.isSuccess) {
            yElement = 1f
        }

        entries.add(Entry(noEntry, yElement))
        noEntry += 1
    }

    // Create a data set and specify properties
    val dataSet = LineDataSet(entries, "Runde").apply {
        color = Color.BLUE
        lineWidth = 2f
        setCircleColor(Color.BLUE)
        circleRadius = 3f
        setDrawCircleHole(false)
        valueTextSize = 10f
        valueTextColor = Color.BLACK
        setDrawValues(false) // Disable drawing values on the chart
    }

    // Create a data object with the data set
    val data = LineData(dataSet)

    // Set the data and other properties for the line chart
    lineChart.data = data
    lineChart.description.isEnabled = false

    // Customize X-Axis
    val xAxis = lineChart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.granularity = 1f // One unit interval
    xAxis.setLabelCount(entries.size, true)
    xAxis.axisMinimum = 1f // Set minimum value
    xAxis.axisMaximum = (entries.size).toFloat() // Set maximum value
    xAxis.setDrawGridLines(false) // Optional: remove grid lines
    xAxis.setDrawAxisLine(true) // Optional: draw axis line
    xAxis.valueFormatter = XAxisValueFormatter("")

    // Customize Y-Axis (Left)
    val leftAxis = lineChart.axisLeft
    leftAxis.axisMinimum = -0.1f // Slightly below 0
    leftAxis.axisMaximum = 1.1f // Slightly above 1
    leftAxis.granularity = 1f // One unit interval
    leftAxis.setLabelCount(2, true) // Only 0 and 1
    leftAxis.setDrawGridLines(false) // Optional: remove grid lines
    leftAxis.setDrawAxisLine(true) // Optional: draw axis line
    leftAxis.valueFormatter = YAxisValueFormatter()

    // Customize Y-Axis (Right)
    val rightAxis = lineChart.axisRight
    rightAxis.isEnabled = false

    // Refresh the chart
    lineChart.invalidate()
}

class XAxisValueFormatter(private val prefix: String) :
    com.github.mikephil.charting.formatter.ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "$prefix ${value.toInt()}"
    }
}

class YAxisValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return if (value == -0.1f) "Gresit" else "Corect"
    }
}
