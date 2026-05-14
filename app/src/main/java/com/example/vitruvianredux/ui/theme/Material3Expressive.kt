package com.example.vitruvianredux.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Shared motion specs.
 */
object ExpressiveMotion {
    /**
     * Standard spring for most interactions.
     */
    val SpringDefault = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Snappy spring for quick transitions (toggles, checkboxes)
     */
    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Bouncy spring for emphasis states.
     */
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Shared card colors.
 */
@Composable
fun expressiveCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
)

/**
 * Shared card shape.
 */
val expressiveCardShape = RoundedCornerShape(12.dp)

/**
 * Shared card elevation.
 */
@Composable
fun expressiveCardElevation(pressed: Boolean = false) = CardDefaults.cardElevation(
    defaultElevation = if (pressed) 1.dp else 2.dp
)

/**
 * Shared card border.
 */
@Composable
fun expressiveCardBorder() = BorderStroke(
    1.dp,
    MaterialTheme.colorScheme.outlineVariant
)

/**
 * Shared button shape.
 */
val expressiveButtonShape = RoundedCornerShape(10.dp)

/**
 * Shared button elevation.
 */
@Composable
fun expressiveButtonElevation() = androidx.compose.material3.ButtonDefaults.buttonElevation(
    defaultElevation = 1.dp,
    pressedElevation = 0.dp
)

