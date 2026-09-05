package com.example.lifehelper.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

/** 底部导航五个目的地 */
sealed class Destination(
    val route: String,
    val icon: ImageVector
) {
    data object Goal : Destination("goal", Icons.Filled.Checklist)
    data object Course : Destination("course", Icons.Filled.CalendarMonth)
    data object Transaction : Destination("transaction", Icons.Filled.AccountBalanceWallet)
    data object Translate : Destination("translate", Icons.Filled.Translate)
    data object Profile : Destination("profile", Icons.Filled.Person)
}

val bottomDestinations = listOf(
    Destination.Goal,
    Destination.Course,
    Destination.Transaction,
    Destination.Translate,
    Destination.Profile
)
