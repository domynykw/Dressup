package com.example.dressup.ai

import com.example.dressup.data.ClothingCategory
import com.example.dressup.data.ClosetItem
import kotlin.random.Random

data class StyledLook(
    val id: String,
    val style: FashionStyle,
    val pieces: List<ClosetItem>,
    val narrative: String,
    val highlights: List<String>,
    val advantages: List<String>
)

class FashionStylist {
    fun availableStyles(items: List<ClosetItem>): List<FashionStyle> {
        return items.flatMap(ClosetItem::styles).distinct()
    }

    fun generateLooks(
        items: List<ClosetItem>,
        targetStyle: FashionStyle,
        profile: PersonalStyleProfile?,
        limit: Int = 5
    ): List<StyledLook> {
        if (items.isEmpty()) return emptyList()
        val styleItems = items.filter { targetStyle in it.styles }
        if (styleItems.isEmpty()) return emptyList()

        val tops = styleItems.filter { it.category == ClothingCategory.TOPS }
        val bottoms = styleItems.filter { it.category == ClothingCategory.BOTTOMS }
        val dresses = styleItems.filter { it.category == ClothingCategory.DRESSES }
        val outerwear = styleItems.filter { it.category == ClothingCategory.OUTERWEAR }
        val shoes = styleItems.filter { it.category == ClothingCategory.SHOES }
        val accessories = styleItems.filter { it.category == ClothingCategory.ACCESSORIES }

        val combinations = mutableListOf<List<ClosetItem>>()
        val random = Random(System.currentTimeMillis())

        dresses.forEach { dress ->
            shoes.forEach { shoe ->
                val combo = mutableListOf(dress, shoe)
                outerwear.randomOrNull(random)?.let { combo += it }
                accessories.randomOrNull(random)?.let { combo += it }
                combinations += combo
            }
        }

        tops.forEach { top ->
            bottoms.forEach { bottom ->
                shoes.forEach { shoe ->
                    val combo = mutableListOf(top, bottom, shoe)
                    outerwear.randomOrNull(random)?.let { combo += it }
                    accessories.randomOrNull(random)?.let { combo += it }
                    combinations += combo
                }
            }
        }

        if (combinations.isEmpty()) return emptyList()

        return combinations
            .distinctBy { combo -> combo.map(ClosetItem::id).sorted() }
            .shuffled(random)
            .take(limit)
            .map { combo -> buildLook(combo, targetStyle, profile) }
    }

    private fun <T> List<T>.randomOrNull(random: Random): T? = if (isNotEmpty()) random.nextInt(size).let { this[it] } else null

    fun rebuildLook(
        pieces: List<ClosetItem>,
        style: FashionStyle,
        profile: PersonalStyleProfile?
    ): StyledLook = buildLook(pieces, style, profile)

    private fun buildLook(
        pieces: List<ClosetItem>,
        style: FashionStyle,
        profile: PersonalStyleProfile?
    ): StyledLook {
        val narrative = FashionKnowledgeBase.narrative(
            style,
            pieces.mapNotNull { it.notes }.ifEmpty { pieces.map { it.category.name.lowercase() } }
        )
        val (keywords, advantages) = describeHighlights(pieces, profile)
        return StyledLook(
            id = pieces.joinToString { it.id },
            style = style,
            pieces = pieces,
            narrative = narrative,
            highlights = keywords,
            advantages = advantages
        )
    }

    private fun describeHighlights(
        pieces: List<ClosetItem>,
        profile: PersonalStyleProfile?
    ): Pair<List<String>, List<String>> {
        val keywords = mutableListOf<String>()
        val advantages = mutableListOf<String>()

        val allColors = pieces.flatMap { it.colorTags }.ifEmpty { listOf("Neutralny") }
        val distinctColors = allColors.distinct()
        if (distinctColors.isNotEmpty()) {
            keywords += "Kolory: ${distinctColors.take(3).joinToString(separator = " · ")}"
        }

        val top = pieces.firstOrNull { it.category == ClothingCategory.TOPS || it.category == ClothingCategory.DRESSES }
        val shoes = pieces.firstOrNull { it.category == ClothingCategory.SHOES }
        val topColor = top?.colorTags?.firstOrNull()
        val shoeColor = shoes?.colorTags?.firstOrNull()
        fun String.prettyCase(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        if (!topColor.isNullOrBlank() && !shoeColor.isNullOrBlank()) {
            keywords += "Góra ${topColor.prettyCase()} × buty ${shoeColor.prettyCase()}"
        }

        profile?.let {
            keywords += "Oczy ${it.eyeColor}"
            keywords += "Włosy ${it.hairTone}"
            advantages += "Podkreśla kolor oczu ${it.eyeColor}"
            advantages += "Pasuje do włosów w tonacji ${it.hairTone}"
            advantages += "Łagodzi proporcje twarzy ${it.faceShape}"
            advantages += "Spójne z paletą ${it.palette.name}"
            advantages += it.palette.suggestions.firstOrNull().orEmpty()
        } ?: advantages.add("Uniwersalna kompozycja na wiele okazji")

        advantages += "Akcent kolorystyczny: ${distinctColors.first()}"

        val cleanKeywords = keywords.filter { it.isNotBlank() }.distinct().take(4)
        val cleanAdvantages = advantages.filter { it.isNotBlank() }.distinct().take(4)
        return cleanKeywords to cleanAdvantages
    }
}
