package com.example.dressup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dressup.R
import com.example.dressup.ui.theme.SoftCream

private data class FeedPost(
    val author: String,
    val title: String,
    val description: String,
    val likes: Int
)

private val demoFeed = listOf(
    FeedPost("@lena.style", "Lilac layering", "Sweter lilac + denim + płaszcz baby blue", 128),
    FeedPost("@fashionmate", "Suitcase essentials", "Jak spakować walizkę na city break", 96),
    FeedPost("@minimalmood", "Soft office look", "Jedwabna koszula + spodnie palazzo + loafersy", 154)
)

@Composable
fun FeedScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.feed_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(id = R.string.feed_subtitle),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(demoFeed) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.author,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.feed_likes, post.likes),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row {
                                IconButton(onClick = { /* TODO: like post */ }) {
                                    Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
                                }
                                IconButton(onClick = { /* TODO: share post */ }) {
                                    Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
