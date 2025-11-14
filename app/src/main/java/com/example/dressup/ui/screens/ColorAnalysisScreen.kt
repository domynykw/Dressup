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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dressup.R
import com.example.dressup.ui.theme.BabyBlue
import com.example.dressup.ui.theme.SoftCream
import java.io.File
import kotlin.math.abs

private data class ColorProfile(
    val name: String,
    val description: String,
    val palette: List<Color>,
    val suggestions: List<String>
)

private data class ColorAnalysisResult(
    val profile: ColorProfile,
    val selfieUri: Uri
)

private val colorProfiles = listOf(
    ColorProfile(
        name = "Chłodne lato",
        description = "Delikatne chłodne odcienie, które podbijają jasną cerę i pastelowe dodatki.",
        palette = listOf(Color(0xFFF6A7C1), Color(0xFFB79EFF), Color(0xFFA6D1FF), Color(0xFF8DE8D9)),
        suggestions = listOf("Noś srebrną biżuterię", "Łącz błękity z lawendą", "Wybieraj chłodne róże do makijażu")
    ),
    ColorProfile(
        name = "Ciepła jesień",
        description = "Soczyste, głębokie barwy najlepiej podkreślają złote refleksy i ciemniejsze włosy.",
        palette = listOf(Color(0xFFE6B980), Color(0xFFCD6B3A), Color(0xFF9A4D2E), Color(0xFF5E3D31)),
        suggestions = listOf("Sięgaj po złotą biżuterię", "Noś karmelowe płaszcze", "Dodaj oliwkową zieleń do stylizacji")
    ),
    ColorProfile(
        name = "Zgaszona wiosna",
        description = "Pastelowe kolory o ciepłej podstawie dodają świeżości i lekkości każdej stylizacji.",
        palette = listOf(Color(0xFFF9D5E5), Color(0xFFE2F0CB), Color(0xFFB3D6FF), Color(0xFFFFF5BA)),
        suggestions = listOf("Łącz pastele w total look", "Wybieraj jasne dżinsy", "Mieszaj beże z pudrowym różem")
    ),
    ColorProfile(
        name = "Zimowy kontrast",
        description = "Wyraziste kontrasty, chłodne błękity i głęboka czerń tworzą elegancki efekt.",
        palette = listOf(Color(0xFF0E1D4A), Color(0xFF6D83F2), Color(0xFFEAF0FF), Color(0xFF0A0A0A)),
        suggestions = listOf("Stawiaj na czarno-białe zestawy", "Dodawaj kobalt jako akcent", "W makijażu postaw na chłodne odcienie")
    )
)

@Composable
fun ColorAnalysisScreen() {
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selfieUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedProfileName by rememberSaveable { mutableStateOf<String?>(null) }
    val analysisResult = remember(selfieUriString, selectedProfileName) {
        if (selfieUriString != null && selectedProfileName != null) {
            val profile = colorProfiles.firstOrNull { it.name == selectedProfileName } ?: colorProfiles.first()
            ColorAnalysisResult(profile = profile, selfieUri = Uri.parse(selfieUriString!!))
        } else {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { savedUri ->
                val uri = Uri.parse(savedUri)
                val result = analyzeSelfie(uri)
                selfieUriString = result.selfieUri.toString()
                selectedProfileName = result.profile.name
            }
        }
        pendingCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && uri != Uri.EMPTY) {
            val result = analyzeSelfie(uri)
            selfieUriString = result.selfieUri.toString()
            selectedProfileName = result.profile.name
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchSelfieCamera(context) { uri ->
                pendingCameraUri = uri.toString()
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, R.string.closet_camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    val takeSelfie: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchSelfieCamera(context) { uri ->
                pendingCameraUri = uri.toString()
                cameraLauncher.launch(uri)
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val pickFromGallery: () -> Unit = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.color_analysis_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(id = R.string.color_analysis_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.color_analysis_how_it_works),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = takeSelfie) {
                        Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.action_add_selfie))
                    }
                    OutlinedButton(onClick = pickFromGallery) {
                        Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.action_pick_from_gallery))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        analysisResult?.let { result ->
            AnalyzedPaletteSection(result)
        } ?: run {
            Text(
                text = stringResource(id = R.string.color_analysis_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AnalyzedPaletteSection(result: ColorAnalysisResult) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AsyncImage(
            model = result.selfieUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
        )
        Text(
            text = stringResource(id = R.string.color_analysis_result_title, result.profile.name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(id = R.string.color_analysis_result_description, result.profile.description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        )
        Text(
            text = stringResource(id = R.string.color_analysis_palette_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            result.profile.palette.forEach { color ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = color.toHexLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BabyBlue.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.color_analysis_recommendations_title),
                    style = MaterialTheme.typography.titleSmall
                )
                result.profile.suggestions.forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

private fun analyzeSelfie(uri: Uri): ColorAnalysisResult {
    val profileIndex = abs(uri.hashCode()) % colorProfiles.size
    val profile = colorProfiles[profileIndex]
    return ColorAnalysisResult(profile = profile, selfieUri = uri)
}

private fun launchSelfieCamera(context: Context, onUriReady: (Uri) -> Unit) {
    val imagesDir = File(context.cacheDir, "color_analysis").apply { mkdirs() }
    val imageFile = File.createTempFile("selfie_", ".jpg", imagesDir)
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, imageFile)
    onUriReady(uri)
}

private fun Color.toHexLabel(): String = "#" + toArgb().toUInt().toString(16).uppercase().takeLast(6)
