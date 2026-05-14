package com.example.vitruvianredux.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * App shape system.
 *
 * The app is an operational workout tool, so surfaces should feel controlled
 * and durable rather than soft and bubbly.
 */
object ExpressiveShapeValues {
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(20.dp)
}

/**
 * MaterialTheme shapes.
 *
 * The exported name is kept for compatibility with existing imports.
 */
val ExpressiveShapes = Shapes(
    extraSmall = ExpressiveShapeValues.ExtraSmall,
    small = ExpressiveShapeValues.Small,
    medium = ExpressiveShapeValues.Medium,
    large = ExpressiveShapeValues.Large,
    extraLarge = ExpressiveShapeValues.ExtraLarge
)

