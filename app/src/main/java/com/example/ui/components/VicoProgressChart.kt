package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter

@Composable
fun VicoStudyTrendsChart(studyHoursPerDay: List<Float>) {
    val chartEntryModelProducer = remember(studyHoursPerDay) {
        val entries = studyHoursPerDay.mapIndexed { index, value ->
            FloatEntry(x = index.toFloat(), y = value)
        }
        ChartEntryModelProducer(entries)
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val bottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val idx = ((value.toInt() % 7) + 7) % 7
        daysOfWeek.getOrElse(idx) { "" }
    }

    Chart(
        chart = columnChart(),
        chartModelProducer = chartEntryModelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp)
    )
}
