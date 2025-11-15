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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ai.ShoppingAdvisor
import com.example.dressup.ai.ShoppingReview
import com.example.dressup.ai.ShoppingScope
import com.example.dressup.ui.state.DressUpViewModel
import com.example.dressup.ui.theme.SoftCream
import java.io.File

@Composable
fun ShopScreen(viewModel: DressUpViewModel) {
    val closetItems by viewModel.closetItems.collectAsState()
    val context = LocalContext.current
    val shoppingAdvisor = remember { ShoppingAdvisor() }
    val reviews = remember { mutableStateListOf<ShoppingReview>() }

    var showChannelDialog by rememberSaveable { mutableStateOf(false) }
    var showScopeDialog by rememberSaveable { mutableStateOf(false) }
    var pendingChannel by rememberSaveable { mutableStateOf<ShoppingChannel?>(null) }
    var pendingScope by rememberSaveable { mutableStateOf<ShoppingScope?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { savedUri ->
                val uri = Uri.parse(savedUri)
                val scope = pendingScope ?: ShoppingScope.ITEM
                shoppingAdvisor.evaluate(context, listOf(uri), closetItems, scope)?.let { review ->
                    reviews.add(0, review)
                    while (reviews.size > 6) {
                        reviews.removeAt(reviews.lastIndex)
                    }
                }
            } ?: Toast.makeText(context, R.string.shop_toast_no_media, Toast.LENGTH_SHORT).show()
        }
        pendingCameraUri = null
        pendingScope = null
        pendingChannel = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        val valid = uris.filter { it != Uri.EMPTY }
        if (valid.isEmpty()) {
            Toast.makeText(context, R.string.shop_toast_no_media, Toast.LENGTH_SHORT).show()
        } else {
            val scope = pendingScope ?: ShoppingScope.ITEM
            val selected = if (scope == ShoppingScope.ITEM) listOf(valid.first()) else valid
            shoppingAdvisor.evaluate(context, selected, closetItems, scope)?.let { review ->
                reviews.add(0, review)
                while (reviews.size > 6) {
                    reviews.removeAt(reviews.lastIndex)
                }
            }
        }
        pendingScope = null
        pendingChannel = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera(context) { createdUri ->
                pendingCameraUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            Toast.makeText(context, R.string.shop_toast_camera_denied, Toast.LENGTH_SHORT).show()
            pendingScope = null
            pendingChannel = null
        }
    }

    fun openOffline(scope: ShoppingScope) {
        pendingScope = scope
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera(context) { createdUri ->
                pendingCameraUri = createdUri.toString()
                cameraLauncher.launch(createdUri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openOnline(scope: ShoppingScope) {
        pendingScope = scope
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        galleryLauncher.launch(request)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.shop_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = R.string.shop_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showChannelDialog = true }) {
                        Text(text = stringResource(id = R.string.shop_button_start))
                    }
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.shop_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            } else {
                item {
                    Text(
                        text = stringResource(id = R.string.shop_section_results),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(reviews, key = { it.title + it.previewUris.firstOrNull().toString() }) { review ->
                    ShoppingReviewCard(review = review)
                }
            }
        }
    }

    if (showChannelDialog) {
        AlertDialog(
            onDismissRequest = { showChannelDialog = false },
            title = { Text(text = stringResource(id = R.string.shop_dialog_channel_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(id = R.string.shop_dialog_channel_body))
                    Button(
                        onClick = {
                            pendingChannel = ShoppingChannel.OFFLINE
                            showChannelDialog = false
                            showScopeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.shop_channel_offline))
                    }
                    OutlinedButton(
                        onClick = {
                            pendingChannel = ShoppingChannel.ONLINE
                            showChannelDialog = false
                            showScopeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.shop_channel_online))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChannelDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }

    if (showScopeDialog) {
        AlertDialog(
            onDismissRequest = { showScopeDialog = false },
            title = { Text(text = stringResource(id = R.string.shop_dialog_scope_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(id = R.string.shop_scope_hint))
                    Button(
                        onClick = {
                            val channel = pendingChannel
                            showScopeDialog = false
                            when (channel) {
                                ShoppingChannel.OFFLINE -> openOffline(ShoppingScope.ITEM)
                                ShoppingChannel.ONLINE -> openOnline(ShoppingScope.ITEM)
                                null -> Unit
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.shop_scope_item))
                    }
                    OutlinedButton(
                        onClick = {
                            val channel = pendingChannel
                            showScopeDialog = false
                            when (channel) {
                                ShoppingChannel.OFFLINE -> openOffline(ShoppingScope.OUTFIT)
                                ShoppingChannel.ONLINE -> openOnline(ShoppingScope.OUTFIT)
                                null -> Unit
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.shop_scope_outfit))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showScopeDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ShoppingReviewCard(review: ShoppingReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = review.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (review.previewUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(review.previewUris) { uri ->
                        Card(
                            modifier = Modifier
                                .height(140.dp)
                                .width(160.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            ReviewSection(
                title = stringResource(id = R.string.shop_review_positives),
                items = review.positives,
                emptyMessage = stringResource(id = R.string.shop_no_positives)
            )
            ReviewSection(
                title = stringResource(id = R.string.shop_review_negatives),
                items = review.negatives,
                emptyMessage = stringResource(id = R.string.shop_no_negatives)
            )
            ReviewSection(
                title = stringResource(id = R.string.shop_review_pairings),
                items = review.pairings,
                emptyMessage = stringResource(id = R.string.shop_pairing_none)
            )
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    items: List<String>,
    emptyMessage: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            items.forEach { entry ->
                Text(
                    text = "• $entry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private enum class ShoppingChannel {
    OFFLINE,
    ONLINE
}

private fun launchCamera(context: Context, onReady: (Uri) -> Unit) {
    val uri = createShoppingImageUri(context)
    onReady(uri)
}

private fun createShoppingImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "shop_photos").apply { mkdirs() }
    val file = File.createTempFile("shop_capture_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}
