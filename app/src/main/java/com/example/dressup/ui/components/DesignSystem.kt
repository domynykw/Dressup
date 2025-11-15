package com.example.dressup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dressup.R
import com.example.dressup.ui.theme.AquaPrimary
import com.example.dressup.ui.theme.AquaSecondary
import com.example.dressup.ui.theme.AzureDeep
import com.example.dressup.ui.theme.MistBlue
import com.example.dressup.ui.theme.SnowWhite

private val ScreenGradient = Brush.radialGradient(
    colors = listOf(AquaSecondary.copy(alpha = 0.28f), SnowWhite),
    center = Alignment.TopCenter.align(1f, 0.05f),
    radius = 1600f
)

@Composable
fun DressUpGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(ScreenGradient)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        content()
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    iconTint: Color = Color.White,
    title: String,
    subtitle: String? = null,
    content: @Composable Column.() -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SnowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                iconRes?.let {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AquaPrimary, AzureDeep)))
                            .padding(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    subtitle?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            content()
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(AquaPrimary, AzureDeep)), RoundedCornerShape(24.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.let { Icon(imageVector = it, contentDescription = null, tint = Color.White) }
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzureDeep),
        border = BorderStroke(1.4.dp, Brush.horizontalGradient(listOf(AquaPrimary, AzureDeep)))
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = AzureDeep)
    }
}

@Composable
fun ChipGroup(
    modifier: Modifier = Modifier,
    chips: List<String>,
    selectedIndex: Int?,
    onSelectedChanged: (Int) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chips.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val background = if (isSelected) Brush.horizontalGradient(listOf(AquaPrimary, AzureDeep)) else Brush.horizontalGradient(listOf(MistBlue, SnowWhite))
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable { onSelectedChanged(index) }
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = textColor)
            }
        }
    }
}

@Composable
fun GradientIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Brush.linearGradient(listOf(AquaPrimary, AzureDeep))
) {
    GradientIconButton(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        brush = brush
    )
}

@Composable
fun GradientIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Brush.linearGradient(listOf(AquaPrimary, AzureDeep))
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(brush)
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
