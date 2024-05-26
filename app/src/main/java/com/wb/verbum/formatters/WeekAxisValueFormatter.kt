package com.wb.verbum.formatters

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class WeekAxisValueFormatter(private val labels: Array<String>) : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        // Ensure that the index is within the bounds of the labels array
        val index = value.toInt().coerceIn(labels.indices)
        return labels[index]
    }
}