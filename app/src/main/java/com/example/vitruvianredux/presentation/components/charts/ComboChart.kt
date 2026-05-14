package com.example.vitruvianredux.presentation.components.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import com.example.vitruvianredux.ui.theme.appChartColors
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

/**
 * Material 3 Expressive Combo Chart
 * Combines column and line charts for multi-metric comparisons
 * Perfect for comparing volume (columns) with weight progression (line)
 */
@Composable
fun ComboChart(
    columnData: List<Pair<String, Float>>, // Label to value pairs for columns
    lineData: List<Pair<String, Float>>, // Label to value pairs for line
    modifier: Modifier = Modifier,
    columnLabel: String = "Volume",
    lineLabel: String = "Weight"
) {
    // Data validation - Material 3 Expressive: Handle empty/invalid data gracefully
    if (columnData.isEmpty() && lineData.isEmpty()) {
        EmptyChartState(
            message = "No data available",
            modifier = modifier
        )
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val columnColor = MaterialTheme.appChartColors.primary
    val lineColor = MaterialTheme.appChartColors.comparison

    LaunchedEffect(columnData, lineData) {
        if (columnData.isEmpty() && lineData.isEmpty()) return@LaunchedEffect

        modelProducer.runTransaction {
            val sessionXByLabel = (columnData.map { it.first } + lineData.map { it.first })
                .distinct()
                .withIndex()
                .associate { (index, label) -> label to index.toFloat() }
            val columnX = columnData.mapIndexed { index, (label, _) ->
                sessionXByLabel[label] ?: index.toFloat()
            }
            val lineX = lineData.mapIndexed { index, (label, _) ->
                sessionXByLabel[label] ?: index.toFloat()
            }

            // Column series (e.g., volume)
            if (columnData.isNotEmpty()) {
                columnSeries {
                    series(
                        x = columnX,
                        y = columnData.map { it.second.toDouble() }
                    )
                }
            }
            
            // Line series (e.g., weight progression)
            if (lineData.isNotEmpty()) {
                lineSeries {
                    series(
                        x = lineX,
                        y = lineData.map { it.second.toDouble() }
                    )
                }
            }
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(label = columnLabel, color = columnColor)
                ChartLegendItem(label = lineLabel, color = lineColor)
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    // Column layer
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                fill(columnColor),
                                8.dp,
                                shape = CorneredShape.rounded(3f)
                            )
                        ),
                        columnCollectionSpacing = 8.dp
                    ),
                    // Line layer
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.Line(
                                fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                                stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 2.25f),
                                pointProvider = LineCartesianLayer.PointProvider.single(
                                    LineCartesianLayer.Point(
                                        rememberShapeComponent(
                                            fill = fill(lineColor),
                                            shape = CorneredShape.Pill,
                                            strokeFill = fill(MaterialTheme.colorScheme.surfaceContainerHighest),
                                            strokeThickness = 1.dp
                                        ),
                                        sizeDp = 7f
                                    )
                                )
                            )
                        )
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
    label: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * Empty state for charts when no data is available
 */
@Composable
private fun EmptyChartState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "No data available",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

