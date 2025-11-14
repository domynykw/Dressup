package com.example.dressup.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dressup.R
import com.example.dressup.ui.styling.OutfitSuggestion
import com.example.dressup.ui.styling.VirtualTryOnLook
import com.example.dressup.ui.styling.generateOutfitSuggestions
import com.example.dressup.ui.styling.generateVirtualTryOnLook
import com.example.dressup.ui.theme.SoftCream

private enum class StylingTab(val labelRes: Int) {
    AI(R.string.styling_tab_ai),
    VIRTUAL(R.string.styling_tab_virtual)
}

@Composable
fun StylingScreen() {
    var suggestions by remember { mutableStateOf(generateOutfitSuggestions()) }
    var selectedTab by remember { mutableStateOf(StylingTab.AI) }
    var virtualLook by remember { mutableStateOf(generateVirtualTryOnLook()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.styling_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = {
                if (selectedTab == StylingTab.AI) {
                    suggestions = generateOutfitSuggestions()
                } else {
                    virtualLook = generateVirtualTryOnLook()
                }
            }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
            }
        }
        Text(
            text = stringResource(id = R.string.styling_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            StylingTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(text = stringResource(id = tab.labelRes)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTab) {
            StylingTab.AI -> SuggestionList(
                suggestions = suggestions,
                onRefresh = { suggestions = generateOutfitSuggestions() }
            )
            StylingTab.VIRTUAL -> VirtualTryOnSection(
                look = virtualLook,
                onRefresh = {
                    virtualLook = generateVirtualTryOnLook()
                }
            )
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
private fun SuggestionList(
    suggestions: List<OutfitSuggestion>,
    onRefresh: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(suggestions) { suggestion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    suggestion.pieces.forEach { element ->
                        Text(
                            text = "• $element",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Divider()
                    Text(
                        text = stringResource(
                            id = R.string.styling_clothing_style,
                            suggestion.clothingStyle.label
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(
                            id = R.string.styling_language_style,
                            suggestion.languageStyle.label
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(
                            id = R.string.styling_interior_style,
                            suggestion.interiorStyle.label
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestion.colorPalette.forEach { colorLabel ->
                            TagChip(label = colorLabel)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* TODO: Open outfit editor */ }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.action_edit_outfit))
                        }
                        OutlinedButton(onClick = onRefresh) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    look.outfit.colorPalette.forEach { colorLabel ->
                        TagChip(label = colorLabel)
                    }
                }
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
