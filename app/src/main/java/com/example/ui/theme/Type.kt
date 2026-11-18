package com.example.ui.theme
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val IBMPlexMonoFontFamily = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = IBMPlexMonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    )
)
