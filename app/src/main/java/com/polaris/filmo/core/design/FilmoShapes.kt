package com.polaris.filmo.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object FilmoRadius {
    val None = 0.dp
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Full = 999.dp
}

val FilmoShapes = Shapes(
    extraSmall = RoundedCornerShape(FilmoRadius.Xs),
    small = RoundedCornerShape(FilmoRadius.Sm),
    medium = RoundedCornerShape(FilmoRadius.Md),
    large = RoundedCornerShape(FilmoRadius.Md),
    extraLarge = RoundedCornerShape(FilmoRadius.Md)
)
