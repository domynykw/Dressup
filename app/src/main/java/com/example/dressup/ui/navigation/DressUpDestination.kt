package com.example.dressup.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.dressup.R

sealed class DressUpDestination(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val showOnBottomBar: Boolean = true
) {
    data object Closet : DressUpDestination(
        route = "closet",
        titleRes = R.string.destination_closet,
        iconRes = R.drawable.ic_outline_closet
    )

    data object Stylings : DressUpDestination(
        route = "stylings",
        titleRes = R.string.destination_stylings,
        iconRes = R.drawable.ic_outline_style
    )

    data object Shop : DressUpDestination(
        route = "shop",
        titleRes = R.string.destination_shop,
        iconRes = R.drawable.ic_outline_shop
    )

    data object Feed : DressUpDestination(
        route = "feed",
        titleRes = R.string.destination_feed,
        iconRes = R.drawable.ic_outline_feed
    )

    data object Settings : DressUpDestination(
        route = "settings",
        titleRes = R.string.destination_settings,
        iconRes = R.drawable.ic_outline_settings,
        showOnBottomBar = false
    )
}

val dressUpDestinations = listOf(
    DressUpDestination.Closet,
    DressUpDestination.Stylings,
    DressUpDestination.Shop,
    DressUpDestination.Feed
)
