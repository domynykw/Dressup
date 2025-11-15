package com.example.dressup.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
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
        icon = Icons.Outlined.Checkroom
    )

    data object Stylings : DressUpDestination(
        route = "stylings",
        titleRes = R.string.destination_stylings,
        icon = Icons.Outlined.AutoAwesome
    )

    data object Feed : DressUpDestination(
        route = "feed",
        titleRes = R.string.destination_feed,
        icon = Icons.Outlined.Explore
    )

    data object Shop : DressUpDestination(
        route = "shop",
        titleRes = R.string.destination_shop,
        icon = Icons.Outlined.ShoppingCart
    )

    data object Settings : DressUpDestination(
        route = "settings",
        titleRes = R.string.destination_settings,
        icon = Icons.Outlined.Settings,
        showOnBottomBar = false
    )
}

val dressUpDestinations = listOf(
    DressUpDestination.Closet,
    DressUpDestination.Stylings,
    DressUpDestination.Shop,
    DressUpDestination.Feed
)
