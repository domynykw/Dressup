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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ui.components.FeatureCard
import com.example.dressup.ui.theme.BabyBlue
import com.example.dressup.ui.theme.SoftCream
import java.io.File

private val closetCategories = listOf(
    R.string.category_tops,
    R.string.category_bottoms,
    R.string.category_dresses,
    R.string.category_outerwear,
    R.string.category_shoes,
    R.string.category_accessories
)

@Composable
fun ClosetScreen() {
    val context = LocalContext.current
    val clothingUris = rememberClosetItemsState()
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { uriString ->
                val uri = Uri.parse(uriString)
                if (!clothingUris.contains(uri)) {
                    clothingUris.add(0, uri)
                }
            }
        }
        pendingCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        val newUris = uris
            .filter { it != Uri.EMPTY }
            .filterNot { selected -> clothingUris.any { it == selected } }
        if (newUris.isNotEmpty()) {
            clothingUris.addAll(0, newUris)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.closet_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.closet_import_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.closet_import_body),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = openCamera) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.action_add_from_camera))
                    }
                    OutlinedButton(onClick = openGallery) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.action_add_from_gallery))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (clothingUris.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.closet_recent_items_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(clothingUris, key = { it.toString() }) { photoUri ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.size(width = 160.dp, height = 200.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = stringResource(id = R.string.closet_item_photo_content_description),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Text(
                text = stringResource(id = R.string.closet_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        Text(
            text = stringResource(id = R.string.closet_categories_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(closetCategories) { categoryRes ->
                FeatureCard(
                    title = stringResource(id = categoryRes),
                    description = stringResource(id = R.string.closet_category_description),
                    accentColor = BabyBlue
                )
            }
        }
    }
}

@Composable
private fun rememberClosetItemsState() = rememberSaveable(
    saver = listSaver(
        save = { stateList -> stateList.map(Uri::toString) },
        restore = { saved ->
            mutableStateListOf<Uri>().apply {
                addAll(saved.map(Uri::parse))
            }
        }
    )
) { mutableStateListOf<Uri>() }

private fun launchCamera(context: Context, onUriReady: (Uri) -> Unit) {
    val uri = createImageUri(context)
    onUriReady(uri)
}

private fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "closet_photos").apply { mkdirs() }
    val imageFile = File.createTempFile("closet_item_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, imageFile)
}
