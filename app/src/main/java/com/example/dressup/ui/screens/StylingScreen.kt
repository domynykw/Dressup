package com.example.dressup.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ai.FashionStyle
import com.example.dressup.ai.FashionStylist
import com.example.dressup.ai.PersonalStyleAnalyzer
import com.example.dressup.ai.PersonalStyleProfile
import com.example.dressup.ai.StyledLook
import com.example.dressup.data.ClosetItem
import com.example.dressup.ui.components.GradientIconButton
import com.example.dressup.ui.components.PrimaryButton
import com.example.dressup.ui.components.SecondaryButton
import com.example.dressup.ui.components.SectionCard
import com.example.dressup.ui.state.DressUpViewModel
import com.example.dressup.ui.styling.VirtualTryOnLook
import com.example.dressup.ui.styling.generateVirtualTryOnLook
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

private enum class StylingTab(val labelRes: Int) {
    WARDROBE(R.string.styling_tab_ai),
    VIRTUAL(R.string.styling_tab_virtual)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylingScreen(viewModel: DressUpViewModel) {
    val closetItems by viewModel.closetItems.collectAsState()
    val personalProfile by viewModel.personalProfile.collectAsState()
    val savedLooks by viewModel.styledLooks.collectAsState()
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(StylingTab.WARDROBE) }
    val stylist = remember { FashionStylist() }
    var selectedStyle by rememberSaveable { mutableStateOf<FashionStyle?>(null) }
    var editingLook by remember { mutableStateOf<StyledLook?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val travelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var virtualLook by remember { mutableStateOf(generateVirtualTryOnLook()) }
    var pendingSelfieUri by rememberSaveable { mutableStateOf<String?>(null) }
    var showTravelPlanner by rememberSaveable { mutableStateOf(false) }

    val availableStyles = remember(closetItems) { stylist.availableStyles(closetItems) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingSelfieUri?.let { uriString ->
                val uri = Uri.parse(uriString)
                val persisted = persistSelfieUri(context, uri)
                viewModel.updatePersonalProfile(PersonalStyleAnalyzer.analyzeSelfie(persisted))
            }
        }
        pendingSelfieUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            val persisted = persistSelfieUri(context, uri)
            viewModel.updatePersonalProfile(PersonalStyleAnalyzer.analyzeSelfie(persisted))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchSelfieCamera(context) { createdUri ->
                pendingSelfieUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            Toast.makeText(context, R.string.closet_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    val takeSelfie: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchSelfieCamera(context) { createdUri ->
                pendingSelfieUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val pickFromGallery: () -> Unit = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(savedLooks) {
        if (savedLooks.isNotEmpty()) {
            val savedStyle = savedLooks.first().style
            if (selectedStyle != savedStyle) {
                selectedStyle = savedStyle
            }
        }
    }

    LaunchedEffect(closetItems, personalProfile, savedLooks, availableStyles) {
        if (closetItems.isEmpty()) {
            return@LaunchedEffect
        }
        val preferredStyle = when {
            selectedStyle != null && selectedStyle in availableStyles -> selectedStyle
            savedLooks.isNotEmpty() && savedLooks.first().style in availableStyles -> savedLooks.first().style
            availableStyles.isNotEmpty() -> availableStyles.first()
            else -> null
        }
        if (selectedStyle != preferredStyle) {
            selectedStyle = preferredStyle
        }
        if (preferredStyle != null && savedLooks.isEmpty()) {
            val generated = stylist.generateLooks(closetItems, preferredStyle, personalProfile)
            viewModel.saveStyledLooks(generated)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.styling_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(id = R.string.styling_ai_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GradientIconButton(
                    imageVector = Icons.Filled.FlightTakeoff,
                    contentDescription = stringResource(id = R.string.styling_open_travel_planner),
                    onClick = { showTravelPlanner = true }
                )
                GradientIconButton(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(id = R.string.styling_action_new_mix),
                    onClick = {
                        when (selectedTab) {
                            StylingTab.WARDROBE -> {
                                selectedStyle?.let {
                                    val refreshed = stylist.generateLooks(closetItems, it, personalProfile)
                                    viewModel.saveStyledLooks(refreshed)
                                }
                            }

                            StylingTab.VIRTUAL -> {
                                virtualLook = generateVirtualTryOnLook()
                            }
                        }
                    }
                )
            }
        }

        PersonalPaletteCard(
            profile = personalProfile,
            onTakeSelfie = takeSelfie,
            onPickFromGallery = pickFromGallery
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            StylingTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(text = stringResource(id = tab.labelRes)) }
                )
            }
        }

        when (selectedTab) {
            StylingTab.WARDROBE -> WardrobeStylingSection(
                closetItems = closetItems,
                availableStyles = availableStyles,
                selectedStyle = selectedStyle,
                looks = savedLooks,
                onStyleSelected = { style ->
                    selectedStyle = style
                    val generated = stylist.generateLooks(closetItems, style, personalProfile)
                    viewModel.saveStyledLooks(generated)
                },
                onEdit = { look -> editingLook = look },
                onRegenerate = {
                    selectedStyle?.let { style ->
                        val regenerated = stylist.generateLooks(closetItems, style, personalProfile)
                        viewModel.saveStyledLooks(regenerated)
                    }
                }
            )
            StylingTab.VIRTUAL -> VirtualTryOnSection(
                look = virtualLook,
                onRefresh = { virtualLook = generateVirtualTryOnLook() }
            )
        }
    }

    if (showTravelPlanner) {
        ModalBottomSheet(
            onDismissRequest = { showTravelPlanner = false },
            sheetState = travelSheetState
        ) {
            TravelPlannerContent(
                closetItems = closetItems,
                profile = personalProfile,
                onClose = {
                    coroutineScope.launch {
                        travelSheetState.hide()
                        showTravelPlanner = false
                    }
                }
            )
        }
    }

    if (editingLook != null) {
        ModalBottomSheet(
            onDismissRequest = { editingLook = null },
            sheetState = sheetState
        ) {
            OutfitEditorSheet(
                look = editingLook!!,
                closetItems = closetItems,
                stylist = stylist,
                profile = personalProfile,
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        editingLook = null
                    }
                },
                onSave = { updated ->
                    viewModel.updateLook(updated)
                    coroutineScope.launch {
                        sheetState.hide()
                        editingLook = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WardrobeStylingSection(
    closetItems: List<ClosetItem>,
    availableStyles: List<FashionStyle>,
    selectedStyle: FashionStyle?,
    looks: List<StyledLook>,
    onStyleSelected: (FashionStyle) -> Unit,
    onEdit: (StyledLook) -> Unit,
    onRegenerate: () -> Unit
) {
    if (closetItems.isEmpty()) {
        Text(
            text = stringResource(id = R.string.styling_ai_empty_state),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    if (availableStyles.isEmpty()) {
        Text(
            text = stringResource(id = R.string.styling_ai_not_enough),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        availableStyles.forEach { style ->
            val selected = style == selectedStyle
            AssistChip(
                onClick = { onStyleSelected(style) },
                label = { Text(text = stringResource(id = style.titleRes)) },
                colors = if (selected) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    labelColor = MaterialTheme.colorScheme.primary
                ) else AssistChipDefaults.assistChipColors()
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    if (looks.isEmpty()) {
        Text(
            text = stringResource(id = R.string.styling_ai_need_more_items),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(looks, key = StyledLook::id) { look ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.styling_ai_generated_title, stringResource(id = look.style.titleRes)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = look.style.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        look.pieces.forEach { item ->
                            ClosetPreview(item = item)
                        }
                    }
                    Text(
                        text = look.narrative,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    if (look.highlights.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.styling_profile_highlights_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            look.highlights.forEach { highlight ->
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(text = highlight) }
                                )
                            }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onEdit(look) }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.action_edit_outfit))
                        }
                        OutlinedButton(onClick = onRegenerate) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.styling_action_new_mix))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClosetPreview(item: ClosetItem) {
    Card(
        modifier = Modifier.size(96.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.uri,
                contentDescription = item.notes,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(text = stringResource(id = item.category.labelRes)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalPaletteCard(
    profile: PersonalStyleProfile?,
    onTakeSelfie: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        iconRes = R.drawable.ic_outline_profile,
        title = stringResource(id = R.string.styling_profile_section_title),
        subtitle = stringResource(id = R.string.styling_profile_section_subtitle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.action_add_selfie),
                modifier = Modifier.weight(1f),
                onClick = onTakeSelfie
            )
            SecondaryButton(
                text = stringResource(id = R.string.action_pick_from_gallery),
                modifier = Modifier.weight(1f),
                onClick = onPickFromGallery
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (profile == null) {
            Text(
                text = stringResource(id = R.string.styling_profile_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = stringResource(id = R.string.styling_profile_saved_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.styling_profile_palette_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                profile.palette.colors.forEach { color ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(color)
                        )
                        Text(
                            text = color.toHexLabel(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Text(
                text = profile.palette.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.styling_profile_features_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(id = R.string.styling_profile_eye_color, profile.eyeColor))
            Text(text = stringResource(id = R.string.styling_profile_hair_tone, profile.hairTone))
            Text(text = stringResource(id = R.string.styling_profile_skin_tone, profile.skinTone))
            Text(text = stringResource(id = R.string.styling_profile_face_shape, profile.faceShape))
            if (profile.keywordSummary.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.styling_profile_highlights_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.keywordSummary.forEach { keyword ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(text = keyword) },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.styling_profile_suggestions_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            profile.palette.suggestions.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun launchSelfieCamera(context: Context, onUriReady: (Uri) -> Unit) {
    val uri = createSelfieUri(context)
    onUriReady(uri)
}

private fun createSelfieUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "style_selfies").apply { mkdirs() }
    val imageFile = File.createTempFile("selfie_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, imageFile)
}

private fun persistSelfieUri(context: Context, source: Uri): Uri {
    val authority = "${context.packageName}.fileprovider"
    val currentPath = source.path.orEmpty()
    if (source.scheme == "content" && source.authority == authority && currentPath.contains("style_selfies")) {
        return source
    }

    val imagesDir = File(context.filesDir, "style_selfies").apply { mkdirs() }
    val targetFile = File(imagesDir, "selfie_${System.currentTimeMillis()}.jpg")

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

private fun Color.toHexLabel(): String =
    "#" + toArgb().toUInt().toString(16).uppercase().takeLast(6)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OutfitEditorSheet(
    look: StyledLook,
    closetItems: List<ClosetItem>,
    stylist: FashionStylist,
    profile: PersonalStyleProfile?,
    onDismiss: () -> Unit,
    onSave: (StyledLook) -> Unit
) {
    var workingPieces by remember(look.id) { mutableStateOf(look.pieces) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.styling_editor_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        workingPieces.forEachIndexed { index, piece ->
            val alternatives = closetItems.filter { it.category == piece.category }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.styling_editor_replace_prompt, piece.notes ?: stringResource(id = piece.category.labelRes)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    alternatives.forEach { option ->
                        val selected = option.id == piece.id
                        AssistChip(
                            onClick = {
                                workingPieces = workingPieces.toMutableList().also { updated ->
                                    updated[index] = option
                                }
                            },
                            label = { Text(text = option.notes ?: stringResource(id = option.category.labelRes)) },
                            colors = if (selected) AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ) else AssistChipDefaults.assistChipColors()
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.styling_editor_cancel))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = {
                val rebuilt = stylist.rebuildLook(workingPieces, look.style, profile).copy(id = look.id)
                onSave(rebuilt)
            }) {
                Text(text = stringResource(id = R.string.styling_editor_save))
            }
        }
    }
}

@Composable
private fun VirtualTryOnSection(
    look: VirtualTryOnLook,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.virtual_try_on_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.virtual_try_on_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                AvatarPreview(look)
                Text(
                    text = stringResource(id = R.string.virtual_try_on_palette, look.paletteLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(id = R.string.virtual_try_on_description, look.story),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
                Button(onClick = onRefresh) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.virtual_try_on_refresh))
                }
            }
        }
    }
}

@Composable
private fun AvatarPreview(look: VirtualTryOnLook) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = look.mood,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    look.outfit.pieces.take(3).forEach { piece ->
                        Text(
                            text = piece,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = look.outfit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
