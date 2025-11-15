package com.example.dressup.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.dressup.R

sealed class DressUpDestination(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val showOnBottomBar: Boolean = true
) {
    data object Closet : DressUpDestination(
        route = "closet",
        titleRes = R.string.destination_closet,
        icon = Icons.Filled.Checkroom
    )

    data object Stylings : DressUpDestination(
        route = "stylings",
        titleRes = R.string.destination_stylings,
        icon = Icons.Filled.AutoAwesome
    )

    data object Suitcase : DressUpDestination(
        route = "suitcase",
        titleRes = R.string.destination_suitcase,
        icon = Icons.Filled.FlightTakeoff
    )

    data object ColorAnalysis : DressUpDestination(
        route = "color_analysis",
        titleRes = R.string.destination_color_analysis,
        icon = Icons.Filled.Palette
    )

    data object Calendar : DressUpDestination(
        route = "calendar",
        titleRes = R.string.destination_calendar,
        icon = Icons.Filled.CalendarMonth
    )

    data object Feed : DressUpDestination(
        route = "feed",
        titleRes = R.string.destination_feed,
        icon = Icons.Filled.Explore
    )

    data object Profile : DressUpDestination(
        route = "profile",
        titleRes = R.string.destination_profile,
        icon = Icons.Filled.Person
    )

    data object Settings : DressUpDestination(
        route = "settings",
        titleRes = R.string.destination_settings,
        icon = Icons.Filled.Settings,
        showOnBottomBar = false
    )
}

val dressUpDestinations = listOf(
    DressUpDestination.Closet,
    DressUpDestination.Stylings,
    DressUpDestination.Suitcase,
    DressUpDestination.ColorAnalysis,
    DressUpDestination.Calendar,
    DressUpDestination.Feed,
    DressUpDestination.Profile
)
