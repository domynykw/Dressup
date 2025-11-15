package com.example.dressup.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dressup.R
import com.example.dressup.ui.suitcase.DailyPackingSuggestion
import com.example.dressup.ui.suitcase.GeoLocation
import com.example.dressup.ui.suitcase.TravelPlan
import com.example.dressup.ui.suitcase.formatDateForDisplay
import com.example.dressup.ui.suitcase.formatDateRange
import com.example.dressup.ui.suitcase.prepareTravelPlan
import com.example.dressup.ui.suitcase.searchDestination
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuitcaseModeScreen() {
    val trips = remember { mutableStateListOf<TravelPlan>() }
    var plannerVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.suitcase_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.suitcase_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
            Button(onClick = { plannerVisible = true }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.action_new_trip))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (trips.isEmpty()) {
            EmptySuitcaseState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(trips, key = { it.id }) { trip ->
                    TravelPlanCard(travelPlan = trip)
                }
            }
        }
    }

    if (plannerVisible) {
        SuitcasePlannerDialog(
            onDismiss = { plannerVisible = false },
            onPlanReady = { plan ->
                trips.add(0, plan)
                plannerVisible = false
            }
        )
    }
}

@Composable
private fun EmptySuitcaseState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.suitcase_empty_state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TravelPlanCard(travelPlan: TravelPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = travelPlan.location.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatDateRange(travelPlan.startDate, travelPlan.endDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                text = stringResource(id = R.string.suitcase_weather_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            travelPlan.forecasts.forEach { forecast ->
                Text(
                    text = forecast.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                text = stringResource(id = R.string.suitcase_outfits_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            travelPlan.packingSuggestions.forEach { suggestion ->
                OutfitSuggestionCard(suggestion)
            }
            if (travelPlan.shoppingTips.isNotEmpty()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Text(
                    text = stringResource(id = R.string.suitcase_shopping_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                travelPlan.shoppingTips.forEach { tip ->
                    Text(
                        text = stringResource(id = R.string.suitcase_shopping_hint, tip),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Text(
                    text = stringResource(id = R.string.suitcase_no_shopping_needed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OutfitSuggestionCard(suggestion: DailyPackingSuggestion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                id = R.string.suitcase_day_label,
                suggestion.displayDate,
                suggestion.forecastSummary
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        suggestion.outfit.pieces.forEach { piece ->
            Text(
                text = "• $piece",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = suggestion.outfit.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(id = R.string.suitcase_reason_prefix, suggestion.reason),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(
                id = R.string.styling_clothing_style,
                suggestion.outfit.clothingStyle.label
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                id = R.string.styling_language_style,
                suggestion.outfit.languageStyle.label
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                id = R.string.styling_interior_style,
                suggestion.outfit.interiorStyle.label
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuitcasePlannerDialog(
    onDismiss: () -> Unit,
    onPlanReady: (TravelPlan) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<GeoLocation>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<GeoLocation?>(null) }
    var startDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var endDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var isLoadingWeather by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var preparedPlan by remember { mutableStateOf<TravelPlan?>(null) }
    val scrollState = rememberScrollState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { location ->
            val update = CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 10f)
            )
            cameraPositionState.animate(update)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.suitcase_new_trip_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.suitcase_search_label)) },
                    placeholder = { Text(text = stringResource(id = R.string.suitcase_search_hint)) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null) }
                )
                Button(
                    onClick = {
                        if (query.isBlank()) return@Button
                        isSearching = true
                        preparedPlan = null
                        coroutineScope.launch {
                            try {
                                val results = searchDestination(query)
                                searchResults = results
                                errorMessage = if (results.isEmpty()) {
                                    stringResource(id = R.string.suitcase_no_results)
                                } else {
                                    null
                                }
                            } catch (error: Exception) {
                                errorMessage = stringResource(id = R.string.suitcase_error_search)
                            } finally {
                                isSearching = false
                            }
                        }
                    },
                    enabled = !isSearching,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(text = stringResource(id = R.string.suitcase_search_button))
                }

                if (searchResults.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.suitcase_search_results),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        searchResults.forEach { location ->
                            LocationResultCard(
                                location = location,
                                isSelected = selectedLocation == location
                            ) {
                                selectedLocation = location
                                preparedPlan = null
                                errorMessage = null
                            }
                        }
                    }
                }

                selectedLocation?.let { location ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.suitcase_selected_location),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = location.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            GoogleMap(
                                modifier = Modifier.matchParentSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                                    title = location.displayName
                                )
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.suitcase_map_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(visible = selectedLocation != null) {
                    TravelDatesSection(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateSelected = {
                            startDate = it
                            if (endDate != null && it != null && endDate!!.isBefore(it)) {
                                endDate = it
                            }
                            preparedPlan = null
                        },
                        onEndDateSelected = {
                            endDate = it
                            preparedPlan = null
                        }
                    )
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        val location = selectedLocation
                        val start = startDate
                        val end = endDate
                        when {
                            location == null -> errorMessage = stringResource(id = R.string.suitcase_error_no_location)
                            start == null || end == null -> errorMessage = stringResource(id = R.string.suitcase_error_no_dates)
                            end.isBefore(start) -> errorMessage = stringResource(id = R.string.suitcase_error_date_order)
                            else -> {
                                errorMessage = null
                                isLoadingWeather = true
                                coroutineScope.launch {
                                    try {
                                        preparedPlan = prepareTravelPlan(location, start, end)
                                        errorMessage = null
                                    } catch (error: Exception) {
                                        errorMessage = stringResource(id = R.string.suitcase_error_fetching)
                                    } finally {
                                        isLoadingWeather = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoadingWeather && selectedLocation != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoadingWeather) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(text = stringResource(id = R.string.suitcase_plan_trip))
                }

                preparedPlan?.let { plan ->
                    SuitcasePlanPreview(plan = plan)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.suitcase_cancel))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            preparedPlan?.let(onPlanReady)
                        },
                        enabled = preparedPlan != null
                    ) {
                        Text(text = stringResource(id = R.string.suitcase_confirm))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationResultCard(
    location: GeoLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val container = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = location.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelDatesSection(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate?) -> Unit,
    onEndDateSelected: (LocalDate?) -> Unit
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.suitcase_dates_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        DateRow(
            label = stringResource(id = R.string.suitcase_pick_start),
            value = startDate?.format(formatter) ?: "—",
            onClick = { showStartPicker = true }
        )
        DateRow(
            label = stringResource(id = R.string.suitcase_pick_end),
            value = endDate?.format(formatter) ?: "—",
            onClick = { showEndPicker = true }
        )
    }

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate?.let { it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() })
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStartDateSelected(formatDateForDisplay(state.selectedDateMillis))
                        showStartPicker = false
                    }
                ) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate?.let { it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() })
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEndDateSelected(formatDateForDisplay(state.selectedDateMillis))
                        showEndPicker = false
                    }
                ) { Text(text = "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DateRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onClick) {
            Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
        }
    }
}

@Composable
private fun SuitcasePlanPreview(plan: TravelPlan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = plan.location.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatDateRange(plan.startDate, plan.endDate),
            style = MaterialTheme.typography.bodyMedium
        )
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Text(
            text = stringResource(id = R.string.suitcase_weather_section),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        plan.forecasts.forEach { forecast ->
            Text(
                text = forecast.summary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Text(
            text = stringResource(id = R.string.suitcase_outfits_section),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        plan.packingSuggestions.forEach { suggestion ->
            Text(
                text = stringResource(
                    id = R.string.suitcase_day_label,
                    suggestion.displayDate,
                    suggestion.forecastSummary
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            suggestion.outfit.pieces.forEach { piece ->
                Text(text = "• $piece", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = stringResource(id = R.string.suitcase_reason_prefix, suggestion.reason),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (plan.shoppingTips.isNotEmpty()) {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                text = stringResource(id = R.string.suitcase_shopping_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            plan.shoppingTips.forEach { tip ->
                Text(text = "• $tip", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
