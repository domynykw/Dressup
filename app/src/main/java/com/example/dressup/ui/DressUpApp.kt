package com.example.dressup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dressup.R
import com.example.dressup.ui.components.SettingsSheet
import com.example.dressup.ui.navigation.DressUpDestination
import com.example.dressup.ui.navigation.dressUpDestinations
import com.example.dressup.ui.screens.ClosetScreen
import com.example.dressup.ui.screens.FeedScreen
import com.example.dressup.ui.screens.ShopScreen
import com.example.dressup.ui.screens.StylingScreen
import com.example.dressup.ui.state.DressUpViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DressUpApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        showSplash = false
    }

    if (showSplash) {
        DressUpSplashScreen()
    } else {
        val navController = rememberNavController()
        val dressUpViewModel: DressUpViewModel = viewModel()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val destinations = dressUpDestinations
        val currentScreen = destinations.firstOrNull { destination ->
            currentDestination.isRouteSelected(destination.route)
        } ?: DressUpDestination.Closet

        val isClosetScreen = currentScreen == DressUpDestination.Closet
        var showSettings by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            topBar = {
                if (!isClosetScreen) {
                    DressUpTopBar(
                        title = stringResource(id = currentScreen.titleRes),
                        onSettingsClick = { showSettings = true }
                    )
                }
            },
            bottomBar = {
                DressUpBottomBar(
                    destinations = destinations,
                    currentDestination = currentDestination,
                ) { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DressUpDestination.Closet.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(DressUpDestination.Closet.route) {
                    ClosetScreen(
                        viewModel = dressUpViewModel,
                        onOpenSettings = { showSettings = true }
                    )
                }
                composable(DressUpDestination.Stylings.route) {
                    StylingScreen(viewModel = dressUpViewModel)
                }
                composable(DressUpDestination.Shop.route) {
                    ShopScreen(viewModel = dressUpViewModel)
                }
                composable(DressUpDestination.Feed.route) {
                    FeedScreen()
                }
            }
        }

        if (showSettings) {
            SettingsSheet(onDismiss = { showSettings = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DressUpTopBar(
    title: String,
    onSettingsClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    TopAppBar(
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.dressup_logo),
                contentDescription = stringResource(id = R.string.accessibility_brand_logo),
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier
                    .padding(start = 12.dp, end = 8.dp)
                    .size(36.dp)
            )
        },
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = DressUpDestination.Settings.icon,
                    contentDescription = stringResource(id = R.string.accessibility_settings)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun DressUpBottomBar(
    destinations: List<DressUpDestination>,
    currentDestination: NavDestination?,
    onDestinationSelected: (DressUpDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        destinations.filter { it.showOnBottomBar }.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.isRouteSelected(destination.route),
                onClick = { onDestinationSelected(destination) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(id = destination.titleRes)
                    )
                },
                label = { Text(text = stringResource(id = destination.titleRes)) }
            )
        }
    }
}

@Composable
private fun DressUpSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6F2)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dressup_logo),
            contentDescription = stringResource(id = R.string.accessibility_brand_logo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
