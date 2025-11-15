package com.example.dressup.ai

import android.content.Context
import android.net.Uri
import com.example.dressup.data.ClosetItem
import java.util.Locale

class FashionAiEngine {
    fun analyze(uri: Uri, context: Context): ClosetItem {
        val name = resolveName(uri, context)
        val category = FashionKnowledgeBase.detectCategory(name)
        val styles = FashionKnowledgeBase.detectStyles(name, category)
        val colors = FashionKnowledgeBase.detectColorTags(name)
        val label = prettify(name)
        return ClosetItem(
            uri = uri,
            category = category,
            styles = styles,
            notes = label.ifBlank { null },
            colorTags = colors
        )
    }

    private fun resolveName(uri: Uri, context: Context): String {
        val last = uri.lastPathSegment
        if (!last.isNullOrBlank()) {
            return last
        }
        return try {
            context.contentResolver.getType(uri) ?: ""
        } catch (error: Exception) {
            ""
        }
    }

    private fun prettify(name: String): String {
        return name
            .substringAfterLast('/')
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
