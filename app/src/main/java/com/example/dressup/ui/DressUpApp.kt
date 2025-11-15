package com.example.dressup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dressup.R
import com.example.dressup.ui.components.DressUpGradientBackground
import com.example.dressup.ui.components.SettingsSheet
import com.example.dressup.ui.navigation.DressUpDestination
import com.example.dressup.ui.navigation.dressUpDestinations
import com.example.dressup.ui.screens.ClosetScreen
import com.example.dressup.ui.screens.FeedScreen
import com.example.dressup.ui.screens.ShopScreen
import com.example.dressup.ui.screens.StylingScreen
import com.example.dressup.ui.state.DressUpViewModel
import com.example.dressup.ui.theme.AquaPrimary
import com.example.dressup.ui.theme.AquaSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DressUpApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        showSplash = false
    }

    AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut()) {
        DressUpSplashScreen()
    }

    if (!showSplash) {
        val navController = rememberNavController()
        val dressUpViewModel: DressUpViewModel = viewModel()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val destinations = dressUpDestinations
        val currentScreen = destinations.firstOrNull { destination ->
            currentDestination.isRouteSelected(destination.route)
        } ?: DressUpDestination.Closet
        var showSettings by rememberSaveable { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                DressUpBottomBar(
                    destinations = destinations,
                    currentDestination = currentDestination
                ) { destination ->
                    coroutineScope.launch {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        ) { innerPadding ->
            DressUpGradientBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = DressUpDestination.Closet.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(DressUpDestination.Closet.route) {
                        ClosetScreen(
                            viewModel = dressUpViewModel,
                            onOpenSettings = { showSettings = true }
                        )
                    }
                    composable(DressUpDestination.Stylings.route) {
                        StylingScreen(
                            viewModel = dressUpViewModel,
                            onOpenSettings = { showSettings = true }
                        )
                    }
                    composable(DressUpDestination.Shop.route) {
                        ShopScreen(viewModel = dressUpViewModel)
                    }
                    composable(DressUpDestination.Feed.route) {
                        FeedScreen()
                    }
                }
            }
        }

        if (showSettings) {
            SettingsSheet(onDismiss = { showSettings = false })
        }
    }
}

@Composable
private fun DressUpBottomBar(
    destinations: List<DressUpDestination>,
    currentDestination: NavDestination?,
    onDestinationSelected: (DressUpDestination) -> Unit
) {
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(AquaSecondary.copy(alpha = 0.3f), Color.White.copy(alpha = 0.8f))
                )
            )
    ) {
        destinations.filter { it.showOnBottomBar }.forEach { destination ->
            val selected = currentDestination.isRouteSelected(destination.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    val painter = painterResource(id = destination.iconRes)
                    Box(
                        modifier = Modifier
                            .size(if (selected) 48.dp else 44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    Brush.radialGradient(listOf(AquaPrimary, AquaSecondary))
                                } else {
                                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = stringResource(id = destination.titleRes),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = destination.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}

@Composable
private fun DressUpSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AquaSecondary, Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.dressup_logo),
                contentDescription = stringResource(id = R.string.accessibility_brand_logo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .size(280.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(id = R.string.splash_tagline),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
