package com.example.dressup.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ai.FashionStyle
import com.example.dressup.ai.PersonalStyleProfile
import com.example.dressup.data.ClosetItem
import com.example.dressup.ui.suitcase.DailyPackingSuggestion
import com.example.dressup.ui.suitcase.GeoLocation
import com.example.dressup.ui.suitcase.TravelActivity
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private data class ActivityPreset(
    val id: String,
    val labelRes: Int,
    val styles: List<FashionStyle>
)

private val activityPresets = listOf(
    ActivityPreset(
        id = "swim",
        labelRes = R.string.travel_activity_swim,
        styles = listOf(FashionStyle.SPORTY, FashionStyle.CASUAL)
    ),
    ActivityPreset(
        id = "hike",
        labelRes = R.string.travel_activity_hike,
        styles = listOf(FashionStyle.SPORTY, FashionStyle.BOHO)
    ),
    ActivityPreset(
        id = "city",
        labelRes = R.string.travel_activity_city,
        styles = listOf(FashionStyle.SMART_CASUAL, FashionStyle.CASUAL)
    ),
    ActivityPreset(
        id = "evening",
        labelRes = R.string.travel_activity_evening,
        styles = listOf(FashionStyle.GLAMOUR, FashionStyle.ROMANTIC)
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TravelPlannerContent(
    closetItems: List<ClosetItem>,
    profile: PersonalStyleProfile?,
    onClose: () -> Unit
) {
    val trips = remember { mutableStateListOf<TravelPlan>() }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.suitcase_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.suitcase_sheet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = stringResource(id = R.string.suitcase_plan_trip_action)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(id = R.string.suitcase_close_sheet))
                }
            }
        }

        if (trips.isEmpty()) {
            TravelEmptyState(onCreate = { showDialog = true })
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(trips, key = { it.id }) { plan ->
                    TravelPlanCard(plan = plan)
                }
            }
        }
    }

    if (showDialog) {
        TravelPlannerDialog(
            closetItems = closetItems,
            profile = profile,
            onDismiss = { showDialog = false },
            onPlanReady = { plan ->
                trips.add(0, plan)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TravelEmptyState(onCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.suitcase_empty_state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onCreate) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(id = R.string.action_new_trip))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TravelPlannerDialog(
    closetItems: List<ClosetItem>,
    profile: PersonalStyleProfile?,
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
    var isLoadingPlan by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var preparedPlan by remember { mutableStateOf<TravelPlan?>(null) }
    val scrollState = rememberScrollState()
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current

    val selectedPresets = remember { mutableStateListOf<String>() }
    val customActivities = remember { mutableStateListOf<String>() }
    var customActivityText by rememberSaveable { mutableStateOf("") }

    val presetLabels = activityPresets.associate { preset ->
        preset.id to stringResource(id = preset.labelRes)
    }

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
                                    context.getString(R.string.suitcase_no_results)
                                } else {
                                    null
                                }
                            } catch (error: Exception) {
                                errorMessage = context.getString(R.string.suitcase_error_search)
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
                        Spacer(modifier = Modifier.size(12.dp))
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
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                Text(
                    text = stringResource(id = R.string.suitcase_activity_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activityPresets.forEach { preset ->
                        val selected = preset.id in selectedPresets
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) {
                                    selectedPresets.remove(preset.id)
                                } else {
                                    selectedPresets.add(preset.id)
                                }
                                preparedPlan = null
                            },
                            label = { Text(text = stringResource(id = preset.labelRes)) }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.suitcase_activity_custom_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customActivityText,
                            onValueChange = { customActivityText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(text = stringResource(id = R.string.suitcase_activity_custom_hint)) }
                        )
                        Button(onClick = {
                            val trimmed = customActivityText.trim()
                            if (trimmed.isNotEmpty()) {
                                customActivities.add(trimmed)
                                customActivityText = ""
                                preparedPlan = null
                            }
                        }) {
                            Text(text = stringResource(id = R.string.suitcase_activity_custom_add))
                        }
                    }
                    if (customActivities.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customActivities.forEach { entry ->
                                AssistChip(
                                    onClick = { customActivities.remove(entry) },
                                    label = { Text(text = entry) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }
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
                        val activities = buildActivities(selectedPresets, customActivities, presetLabels)
                        when {
                            location == null -> errorMessage = context.getString(R.string.suitcase_error_no_location)
                            start == null || end == null -> errorMessage = context.getString(R.string.suitcase_error_no_dates)
                            end.isBefore(start) -> errorMessage = context.getString(R.string.suitcase_error_date_order)
                            closetItems.isEmpty() -> errorMessage = context.getString(R.string.suitcase_error_no_closet)
                            else -> {
                                errorMessage = null
                                isLoadingPlan = true
                                coroutineScope.launch {
                                    try {
                                        val plan = prepareTravelPlan(location, start, end, activities, closetItems, profile)
                                        preparedPlan = plan
                                    } catch (error: Exception) {
                                        errorMessage = context.getString(R.string.suitcase_error_fetching)
                                    } finally {
                                        isLoadingPlan = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoadingPlan && selectedLocation != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoadingPlan) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.size(12.dp))
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
                    Spacer(modifier = Modifier.size(12.dp))
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

private fun buildActivities(
    selectedPresets: List<String>,
    customActivities: List<String>,
    presetLabels: Map<String, String>
): List<TravelActivity> {
    val presetActivities = selectedPresets.mapNotNull { id ->
        val preset = activityPresets.firstOrNull { it.id == id }
        preset?.let {
            TravelActivity(
                name = presetLabels[id] ?: id,
                styleHints = it.styles
            )
        }
    }
    val custom = customActivities.map { entry ->
        TravelActivity(
            name = entry,
            styleHints = inferStylesForCustomActivity(entry)
        )
    }
    return presetActivities + custom
}

private fun inferStylesForCustomActivity(name: String): List<FashionStyle> {
    val lower = name.lowercase(Locale.getDefault())
    return when {
        listOf("plaż", "basen", "swim", "kąpiel").any { lower.contains(it) } -> listOf(FashionStyle.SPORTY, FashionStyle.CASUAL)
        listOf("gór", "szlak", "hike", "trek").any { lower.contains(it) } -> listOf(FashionStyle.SPORTY, FashionStyle.BOHO)
        listOf("wiecz", "kolac", "party", "noc").any { lower.contains(it) } -> listOf(FashionStyle.GLAMOUR, FashionStyle.ROMANTIC)
        listOf("biznes", "konfer", "spotkanie", "praca").any { lower.contains(it) } -> listOf(FashionStyle.CLASSIC, FashionStyle.SMART_CASUAL)
        else -> listOf(FashionStyle.CASUAL)
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
private fun TravelPlanCard(plan: TravelPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = plan.location.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatDateRange(plan.startDate, plan.endDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (plan.activities.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.suitcase_activity_summary, plan.activities.joinToString(separator = " · ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (plan.climateNotes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.suitcase_climate_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    plan.climateNotes.forEach { note ->
                        Text(
                            text = "• $note",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                text = stringResource(id = R.string.suitcase_outfits_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            plan.packingSuggestions.forEach { suggestion ->
                DailySuggestionCard(suggestion = suggestion)
            }
            if (plan.shoppingTips.isNotEmpty()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Text(
                    text = stringResource(id = R.string.suitcase_shopping_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                plan.shoppingTips.forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailySuggestionCard(suggestion: DailyPackingSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.suitcase_day_label, suggestion.displayDate, suggestion.forecastSummary),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.suitcase_activity_badge, suggestion.activityName),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestion.look.highlights.forEach { keyword ->
                    AssistChip(
                        onClick = {},
                        label = { Text(text = keyword) }
                    )
                }
                suggestion.contextHighlights.forEach { keyword ->
                    AssistChip(
                        onClick = {},
                        label = { Text(text = keyword) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
            if (suggestion.look.advantages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestion.look.advantages.forEach { advantage ->
                        Text(
                            text = "• $advantage",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            suggestion.contingency?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestion.look.pieces.forEach { item ->
                    ClosetPieceRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun ClosetPieceRow(item: ClosetItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.notes,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (item.styles.isNotEmpty()) {
                Text(
                    text = item.styles.joinToString { style -> stringResource(id = style.titleRes) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun ClosetItem.displayName(): String {
    return notes?.takeIf { it.isNotBlank() }
        ?: category.name.lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
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
        plan.climateNotes.forEach { note ->
            Text(
                text = "• $note",
                style = MaterialTheme.typography.bodySmall
            )
        }
        plan.packingSuggestions.take(2).forEach { suggestion ->
            Text(
                text = stringResource(
                    id = R.string.suitcase_day_label,
                    suggestion.displayDate,
                    suggestion.forecastSummary
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            suggestion.look.pieces.forEach { item ->
                Text(
                    text = "• ${item.displayName()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
