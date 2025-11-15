package com.example.dressup.ai

import android.content.Context
import android.net.Uri
import com.example.dressup.R
import com.example.dressup.data.ClothingCategory
import com.example.dressup.data.ClosetItem
import java.util.Locale

enum class ShoppingScope {
    ITEM,
    OUTFIT
}

data class ShoppingReview(
    val title: String,
    val positives: List<String>,
    val negatives: List<String>,
    val pairings: List<String>,
    val previewUris: List<Uri>
)

class ShoppingAdvisor(
    private val aiEngine: FashionAiEngine = FashionAiEngine()
) {
    fun evaluate(
        context: Context,
        uris: List<Uri>,
        closet: List<ClosetItem>,
        scope: ShoppingScope
    ): ShoppingReview? {
        if (uris.isEmpty()) return null
        val analyzed = uris.map { uri -> aiEngine.analyze(uri, context) }
        if (analyzed.isEmpty()) return null

        val styles = analyzed.flatMap { it.styles }.distinct()
        val colors = analyzed.flatMap { it.colorTags }.distinct()
        val categories = analyzed.map { it.category }
        val styleLabels = styles.map { style -> context.getString(style.titleRes) }
        val positives = mutableListOf<String>()
        val negatives = mutableListOf<String>()
        val pairings = mutableListOf<String>()

        if (styleLabels.isNotEmpty()) {
            positives += context.getString(R.string.shop_positive_style, styleLabels.joinToString(" · "))
        }
        if (colors.isNotEmpty()) {
            positives += context.getString(R.string.shop_positive_colors, colors.joinToString(" · "))
        }
        if (scope == ShoppingScope.OUTFIT) {
            positives += context.getString(R.string.shop_positive_outfit_ready, categories.size)
        }

        if (colors.size <= 1) {
            negatives += context.getString(R.string.shop_negative_monotone)
        }
        if (scope == ShoppingScope.OUTFIT) {
            val hasTop = categories.any { it == ClothingCategory.TOPS || it == ClothingCategory.DRESSES }
            val hasBottom = categories.any { it == ClothingCategory.BOTTOMS || it == ClothingCategory.DRESSES }
            val hasShoes = categories.any { it == ClothingCategory.SHOES }
            if (!hasTop) {
                negatives += context.getString(
                    R.string.shop_negative_missing_piece,
                    missingLabel(context, ClothingCategory.TOPS)
                )
            }
            if (!hasBottom) {
                negatives += context.getString(
                    R.string.shop_negative_missing_piece,
                    missingLabel(context, ClothingCategory.BOTTOMS)
                )
            }
            if (!hasShoes) {
                negatives += context.getString(
                    R.string.shop_negative_missing_piece,
                    missingLabel(context, ClothingCategory.SHOES)
                )
            }
        }

        val styleSet = styles.toSet()
        val colorSet = colors.toSet()
        val matchingCloset = closet.filter { closetItem ->
            closetItem.styles.any(styleSet::contains) || closetItem.colorTags.any(colorSet::contains)
        }
        if (matchingCloset.isEmpty()) {
            negatives += context.getString(R.string.shop_negative_no_matches)
            pairings += context.getString(R.string.shop_pairing_none)
        } else {
            matchingCloset
                .distinctBy { it.id }
                .take(5)
                .forEach { closetItem ->
                    val name = closetItem.notes?.takeIf { it.isNotBlank() }
                        ?: context.getString(closetItem.category.labelRes)
                    pairings += context.getString(
                        R.string.shop_pairing_with,
                        name,
                        context.getString(closetItem.category.labelRes)
                    )
                }
        }

        val primary = analyzed.first()
        val categoryLabel = context.getString(primary.category.labelRes)
        val primaryLabel = primary.notes?.takeIf { it.isNotBlank() } ?: categoryLabel
        val title = when (scope) {
            ShoppingScope.ITEM -> context.getString(R.string.shop_review_title_item, primaryLabel)
            ShoppingScope.OUTFIT -> {
                val styleName = styleLabels.firstOrNull() ?: context.getString(R.string.shop_generic_outfit)
                context.getString(R.string.shop_review_title_outfit, styleName)
            }
        }

        val cleanNegatives = negatives.distinct()
        val cleanPositives = positives.distinct()
        val cleanPairings = pairings.distinct()

        return ShoppingReview(
            title = title,
            positives = cleanPositives,
            negatives = cleanNegatives,
            pairings = cleanPairings,
            previewUris = uris
        )
    }
}

private fun missingLabel(context: Context, category: ClothingCategory): String {
    val raw = context.getString(category.labelRes)
    return raw.replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.getDefault()) else it.toString() }
}
