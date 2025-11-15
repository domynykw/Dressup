package com.example.dressup.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ai.FashionAiEngine
import com.example.dressup.ai.StyledLook
import com.example.dressup.data.ClothingCategory
import com.example.dressup.data.ClosetItem
import com.example.dressup.ui.components.ChipGroup
import com.example.dressup.ui.components.PrimaryButton
import com.example.dressup.ui.components.SectionCard
import com.example.dressup.ui.state.DressUpViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

private const val FILTER_ALL = "all"
private const val FILTER_ADD = "add"

private data class ClosetFilter(
    val id: String,
    val labelRes: Int,
    val category: ClothingCategory? = null,
    val isAdd: Boolean = false
)

private val closetFilters = buildList {
    add(ClosetFilter(id = FILTER_ALL, labelRes = R.string.closet_filter_all))
    ClothingCategory.values()
        .filter { it != ClothingCategory.UNKNOWN }
        .forEach { category ->
            add(ClosetFilter(id = category.name, labelRes = category.labelRes, category = category))
        }
    add(ClosetFilter(id = FILTER_ADD, labelRes = R.string.category_add, isAdd = true))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClosetScreen(
    viewModel: DressUpViewModel,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val closetItems by viewModel.closetItems.collectAsState()
    val styledLooks by viewModel.styledLooks.collectAsState()
    val calendarLooks by viewModel.calendarLooks.collectAsState()
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var selectedFilterId by rememberSaveable { mutableStateOf(FILTER_ALL) }
    val aiEngine = remember { FashionAiEngine() }
    val locale = remember { Locale.getDefault() }
    var currentMonthIso by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val currentMonth = remember(currentMonthIso) { YearMonth.parse(currentMonthIso) }
    var assigningLook by remember { mutableStateOf<StyledLook?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCalendarEntry by remember { mutableStateOf<Pair<LocalDate, StyledLook>?>(null) }
    val monthLabel = remember(currentMonth, locale) {
        currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
    val weekdayLabels = stringArrayResource(id = R.array.calendar_weekdays)
    val calendarDays = remember(currentMonth, calendarLooks) {
        buildCalendarDays(currentMonth, calendarLooks)
    }
    val detailedFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { uriString ->
                val uri = Uri.parse(uriString)
                handleIncomingUri(
                    context = context,
                    uri = uri,
                    aiEngine = aiEngine,
                    viewModel = viewModel
                )?.let { item ->
                    val target = closetFilters.firstOrNull { it.category == item.category }?.id ?: FILTER_ALL
                    selectedFilterId = target
                }
            }
        }
        pendingCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        uris
            .filter { it != Uri.EMPTY }
            .forEach { uri ->
                handleIncomingUri(
                    context = context,
                    uri = uri,
                    aiEngine = aiEngine,
                    viewModel = viewModel
                )?.let { item ->
                    val target = closetFilters.firstOrNull { it.category == item.category }?.id ?: FILTER_ALL
                    selectedFilterId = target
                }
            }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera(context) { createdUri ->
                pendingCameraUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.closet_camera_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val openCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera(context) { createdUri ->
                pendingCameraUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val openGallery: () -> Unit = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val closeSheet: () -> Unit = {
        coroutineScope.launch {
            try {
                sheetState.hide()
            } finally {
                showSourceSheet = false
            }
        }
    }

    val handleCameraSelection = {
        openCamera()
        closeSheet()
    }

    val handleGallerySelection = {
        openGallery()
        closeSheet()
    }

    val addCardTitle = if (closetItems.isEmpty()) {
        R.string.closet_add_title
    } else {
        R.string.closet_add_title_existing
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.closet_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(id = R.string.closet_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_settings),
                    contentDescription = stringResource(id = R.string.accessibility_settings)
                )
            }
        }

        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.ic_outline_closet,
            title = stringResource(id = addCardTitle),
            subtitle = stringResource(id = R.string.closet_add_subtitle)
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.closet_add_action),
                modifier = Modifier.fillMaxWidth(),
                onClick = { showSourceSheet = true }
            )
        }

        val selectedFilter = closetFilters.firstOrNull { it.id == selectedFilterId } ?: closetFilters.first()
        val filteredItems = when {
            selectedFilter.id == FILTER_ALL -> closetItems
            selectedFilter.category != null -> closetItems.filter { it.category == selectedFilter.category }
            else -> closetItems
        }
        val chipLabels = closetFilters.map { stringResource(id = it.labelRes) }
        val selectedIndex = closetFilters.indexOfFirst { it.id == selectedFilterId }.takeIf { it >= 0 }

        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.ic_outline_profile,
            title = stringResource(id = R.string.closet_section_wardrobe_title)
        ) {
            ChipGroup(
                modifier = Modifier.fillMaxWidth(),
                chips = chipLabels,
                selectedIndex = selectedIndex,
                onSelectedChanged = { index ->
                    val filter = closetFilters[index]
                    if (filter.isAdd) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.closet_add_category_hint),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedFilterId = filter.id
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                closetItems.isEmpty() -> {
                    ClosetEmptyState()
                }

                filteredItems.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.closet_filter_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(filteredItems, key = ClosetItem::id) { item ->
                            ClosetItemCard(item = item)
                        }
                    }
                }
            }
        }

        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.ic_outline_style,
            title = stringResource(id = R.string.closet_section_stylings_title),
            subtitle = stringResource(id = R.string.closet_stylings_subtitle)
        ) {
            if (styledLooks.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.closet_stylings_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(styledLooks, key = StyledLook::id) { look ->
                        StyledLookCard(
                            look = look,
                            onAddToCalendar = {
                                assigningLook = look
                                showDatePicker = true
                            }
                        )
                    }
                }
            }
        }

        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            iconRes = R.drawable.ic_outline_calendar,
            title = stringResource(id = R.string.closet_calendar_title),
            subtitle = stringResource(id = R.string.closet_calendar_subtitle)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonthIso = currentMonth.minusMonths(1).toString() }) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = stringResource(id = R.string.closet_calendar_previous)
                    )
                }
                Text(
                    text = stringResource(id = R.string.calendar_month_label, monthLabel, currentMonth.year),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { currentMonthIso = currentMonth.plusMonths(1).toString() }) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = stringResource(id = R.string.closet_calendar_next)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (calendarLooks.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.closet_calendar_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(weekdayLabels.size) { index ->
                    Text(
                        text = weekdayLabels[index],
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                itemsIndexed(
                    items = calendarDays,
                    key = { index, day -> day.date?.toString() ?: "empty_$index" }
                ) { _, day ->
                    ClosetCalendarCell(
                        day = day,
                        onEntryClick = { date, look ->
                            selectedCalendarEntry = date to look
                        }
                    )
                }
            }
        }
    }

    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.closet_add_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ClosetSheetOption(
                    iconRes = R.drawable.ic_outline_camera,
                    label = stringResource(id = R.string.action_add_from_camera),
                    onClick = handleCameraSelection
                )
                ClosetSheetOption(
                    iconRes = R.drawable.ic_outline_gallery,
                    label = stringResource(id = R.string.action_add_from_gallery),
                    onClick = handleGallerySelection
                )
            }
        }
    }

    if (showDatePicker && assigningLook != null) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                assigningLook = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = dateState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Instant.ofEpochMilli(selectedMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        assigningLook?.let { look ->
                            viewModel.assignLookToDate(selectedDate, look)
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.closet_calendar_saved_toast,
                                    selectedDate.format(detailedFormatter)
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showDatePicker = false
                        assigningLook = null
                    },
                    enabled = dateState.selectedDateMillis != null
                ) {
                    Text(text = stringResource(id = R.string.closet_calendar_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        assigningLook = null
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    selectedCalendarEntry?.let { (date, look) ->
        AlertDialog(
            onDismissRequest = { selectedCalendarEntry = null },
            title = {
                Text(
                    text = stringResource(
                        id = R.string.closet_calendar_dialog_title,
                        date.format(detailedFormatter)
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            id = R.string.closet_calendar_entry_style,
                            stringResource(id = look.style.titleRes)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (look.highlights.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.styling_profile_highlights_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        look.highlights.forEach { highlight ->
                            Text(
                                text = "• $highlight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                    if (look.advantages.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.styling_profile_advantages_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        look.advantages.forEach { advantage ->
                            Text(
                                text = "• $advantage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeCalendarEntry(date)
                        selectedCalendarEntry = null
                    }
                ) {
                    Text(text = stringResource(id = R.string.closet_calendar_dialog_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCalendarEntry = null }) {
                    Text(text = stringResource(id = R.string.closet_calendar_dialog_close))
                }
            }
        )
    }
}

private data class ClosetCalendarDay(
    val date: LocalDate?,
    val look: StyledLook?
)

private fun buildCalendarDays(
    month: YearMonth,
    assignments: Map<LocalDate, StyledLook>
): List<ClosetCalendarDay> {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value + 6) % 7
    val totalDays = month.lengthOfMonth()
    val items = mutableListOf<ClosetCalendarDay>()
    repeat(firstDayOffset) { items.add(ClosetCalendarDay(null, null)) }
    for (day in 1..totalDays) {
        val date = month.atDay(day)
        items.add(ClosetCalendarDay(date, assignments[date]))
    }
    while (items.size % 7 != 0) {
        items.add(ClosetCalendarDay(null, null))
    }
    return items
}

@Composable
private fun ClosetEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.closet_empty_state),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
        Text(
            text = stringResource(id = R.string.closet_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(id = R.string.closet_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StyledLookCard(
    look: StyledLook,
    onAddToCalendar: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .heightIn(min = 280.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            look.pieces.firstOrNull()?.let { piece ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = piece.uri,
                        contentDescription = piece.notes,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(
                text = stringResource(
                    id = R.string.closet_styling_style_label,
                    stringResource(id = look.style.titleRes)
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            if (look.highlights.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.styling_profile_highlights_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                look.highlights.take(2).forEach { highlight ->
                    Text(
                        text = "• $highlight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            look.advantages.firstOrNull()?.let { advantage ->
                Text(
                    text = advantage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
            Button(
                onClick = onAddToCalendar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.closet_stylings_add_to_calendar))
            }
        }
    }
}

@Composable
private fun ClosetCalendarCell(
    day: ClosetCalendarDay,
    onEntryClick: (LocalDate, StyledLook) -> Unit
) {
    if (day.date == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {}
        return
    }

    val hasLook = day.look != null
    val containerColor = if (hasLook) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (hasLook) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = {
            val look = day.look ?: return@Card
            onEntryClick(day.date, look)
        },
        enabled = hasLook,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (hasLook) 140.dp else 110.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.calendar_day_label, day.date.dayOfMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            day.look?.let { look ->
                Text(
                    text = stringResource(
                        id = R.string.closet_calendar_entry_style,
                        stringResource(id = look.style.titleRes)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                look.highlights.firstOrNull()?.let { highlight ->
                    Text(
                        text = highlight,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private fun launchCamera(context: Context, onUriReady: (Uri) -> Unit) {
    val uri = createImageUri(context)
    onUriReady(uri)
}

private fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "closet_photos").apply { mkdirs() }
    val imageFile = File.createTempFile("closet_item_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, imageFile)
}

private fun persistClosetUri(context: Context, source: Uri): Uri {
    val authority = "${context.packageName}.fileprovider"
    val currentPath = source.path.orEmpty()
    if (source.scheme == "content" && source.authority == authority && currentPath.contains("closet_photos")) {
        return source
    }

    val imagesDir = File(context.filesDir, "closet_photos").apply { mkdirs() }
    val targetFile = File(imagesDir, "closet_item_${System.currentTimeMillis()}.jpg")

    val copied = runCatching {
        context.contentResolver.openInputStream(source)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        true
    }.getOrElse {
        if (targetFile.exists()) {
            targetFile.delete()
        }
        false
    }

    return if (copied && targetFile.exists()) {
        FileProvider.getUriForFile(context, authority, targetFile)
    } else {
        source
    }
}

private fun handleIncomingUri(
    context: Context,
    uri: Uri,
    aiEngine: FashionAiEngine,
    viewModel: DressUpViewModel
): ClosetItem? {
    val analyzed = aiEngine.analyze(uri, context)
    val persistedUri = persistClosetUri(context, uri)
    val item = analyzed.copy(uri = persistedUri)
    val added = viewModel.addClosetItem(item)
    if (added) {
        val stylesLabel = if (item.styles.isNotEmpty()) {
            item.styles.joinToString { style ->
                context.getString(style.titleRes)
            }
        } else {
            context.getString(R.string.closet_ai_styles_unknown)
        }
        Toast.makeText(
            context,
            context.getString(
                R.string.closet_ai_added_toast,
                context.getString(item.category.labelRes),
                stylesLabel
            ),
            Toast.LENGTH_SHORT
        ).show()
        return item
    }
    return null
}

@Composable
private fun ClosetItemCard(item: ClosetItem) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(width = 180.dp, height = 220.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.uri,
                contentDescription = stringResource(id = R.string.closet_item_photo_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(text = stringResource(id = item.category.labelRes)) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(id = R.string.closet_ai_styles_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.styles.take(3).forEach { style ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(text = stringResource(id = style.titleRes)) }
                        )
                    }
                }
                if (item.colorTags.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.closet_ai_colors_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = item.colorTags.joinToString(separator = " • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ClosetSheetOption(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
