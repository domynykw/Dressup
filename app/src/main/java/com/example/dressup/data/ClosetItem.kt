package com.example.dressup.data

import android.net.Uri
import androidx.annotation.StringRes
import com.example.dressup.R
import com.example.dressup.ai.FashionStyle
import java.util.UUID

enum class ClothingCategory(@StringRes val labelRes: Int) {
    TOPS(R.string.category_tops),
    BOTTOMS(R.string.category_bottoms),
    DRESSES(R.string.category_dresses),
    OUTERWEAR(R.string.category_outerwear),
    SHOES(R.string.category_shoes),
    ACCESSORIES(R.string.category_accessories),
    UNKNOWN(R.string.category_unknown)
}

data class ClosetItem(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val category: ClothingCategory,
    val styles: List<FashionStyle>,
    val notes: String? = null,
    val colorTags: List<String> = emptyList()
)
