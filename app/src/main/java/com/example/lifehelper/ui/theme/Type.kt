package com.example.lifehelper.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val defaultFont = FontFamily.Default

val Typography = Typography(
    headlineMedium = TextStyle(
        fontFamily = defaultFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = defaultFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = defaultFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = defaultFont,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = defaultFont,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = defaultFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
