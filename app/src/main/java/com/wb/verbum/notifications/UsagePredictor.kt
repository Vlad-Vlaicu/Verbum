package com.wb.verbum.notifications

import com.wb.verbum.model.User
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class UsagePredictor {
    fun computeLinearRegressionCoefficients(user: User): Pair<Float, Float>? {
        val usageList = user.logInTimes
        if (usageList.size < 2) {
            return Pair(0f, defaultTo19PM())
        }

        val features = usageList.map { LocalDateTime.parse(it).toEpochSecond(ZoneOffset.UTC).toFloat() }
        val labels = features.indices.map { it.toFloat() }

        val xMean = features.average().toFloat()
        val yMean = labels.average().toFloat()

        var numerator = 0f
        var denominator = 0f
        for (i in features.indices) {
            numerator += (features[i] - xMean) * (labels[i] - yMean)
            denominator += (features[i] - xMean) * (features[i] - xMean)
        }
        val slope = numerator / denominator
        val intercept = yMean - (slope * xMean)

        return Pair(slope, intercept)
    }

     fun predictNextUsage(user: User): Long? {
        val (slope, intercept) = computeLinearRegressionCoefficients(user) ?: return null
        val latestUsage = LocalDateTime.parse(user.logInTimes.last()).toEpochSecond(ZoneOffset.UTC).toFloat()
        val prediction = (slope * latestUsage) + intercept

        return prediction.toLong()
    }


    fun defaultTo19PM(): Float {
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val defaultTime = today.plusHours(19).toEpochSecond(ZoneOffset.UTC).toFloat()
        return defaultTime
    }
}