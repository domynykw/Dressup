package com.example.dressup.ui.styling

import kotlin.random.Random

private enum class WardrobeCategory {
    SHOES,
    BOTTOM,
    DRESS,
    TOP,
    LAYER
}

enum class ClothingStyle(val label: String) {
    KLASYCZNY("Klasyczny"),
    CASUAL("Casual"),
    SPORTOWY("Sportowy"),
    BOHO("Boho"),
    GLAMOUR("Glamour"),
    VINTAGE("Vintage"),
    MINIMALISTYCZNY("Minimalistyczny")
}

enum class LanguageStyle(val label: String) {
    POTOCZNY("Potoczny"),
    NAUKOWY("Naukowy"),
    URZEDOWY("Urzędowy"),
    ARTYSTYCZNY("Artystyczny")
}

enum class InteriorStyle(val label: String) {
    SKANDYNAWSKI("Skandynawski"),
    INDUSTRIALNY("Industrialny"),
    RUSTYKALNY("Rustykalny"),
    GLAMOUR("Glamour"),
    NOWOCZESNY("Nowoczesny")
}

private enum class ColorFamily(val displayName: String) {
    PASTEL_COOL("Pastelowy chłodny"),
    PASTEL_WARM("Pastelowy ciepły"),
    NEUTRAL("Neutralny"),
    BOLD("Wyrazisty")
}

private data class WardrobePiece(
    val name: String,
    val category: WardrobeCategory,
    val colorFamily: ColorFamily,
    val clothingStyles: Set<ClothingStyle>
)

data class OutfitSuggestion(
    val title: String,
    val pieces: List<String>,
    val clothingStyle: ClothingStyle,
    val languageStyle: LanguageStyle,
    val interiorStyle: InteriorStyle,
    val colorPalette: List<String>
)

data class VirtualTryOnLook(
    val outfit: OutfitSuggestion,
    val paletteLabel: String,
    val story: String,
    val mood: String
)

private val colorHarmony: Map<ColorFamily, Set<ColorFamily>> = mapOf(
    ColorFamily.PASTEL_COOL to setOf(ColorFamily.PASTEL_COOL, ColorFamily.NEUTRAL),
    ColorFamily.PASTEL_WARM to setOf(ColorFamily.PASTEL_WARM, ColorFamily.NEUTRAL),
    ColorFamily.NEUTRAL to setOf(ColorFamily.PASTEL_COOL, ColorFamily.PASTEL_WARM, ColorFamily.NEUTRAL, ColorFamily.BOLD),
    ColorFamily.BOLD to setOf(ColorFamily.NEUTRAL)
)

private val wardrobePieces = listOf(
    WardrobePiece(
        name = "Liliowa bluza z kapturem",
        category = WardrobeCategory.TOP,
        colorFamily = ColorFamily.PASTEL_COOL,
        clothingStyles = setOf(ClothingStyle.CASUAL, ClothingStyle.SPORTOWY)
    ),
    WardrobePiece(
        name = "Błękitna koszula oversize",
        category = WardrobeCategory.TOP,
        colorFamily = ColorFamily.PASTEL_COOL,
        clothingStyles = setOf(ClothingStyle.MINIMALISTYCZNY, ClothingStyle.KLASYCZNY)
    ),
    WardrobePiece(
        name = "Jedwabna bluzka écru",
        category = WardrobeCategory.TOP,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.GLAMOUR)
    ),
    WardrobePiece(
        name = "Pastelowe spodnie garniturowe",
        category = WardrobeCategory.BOTTOM,
        colorFamily = ColorFamily.PASTEL_COOL,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.MINIMALISTYCZNY)
    ),
    WardrobePiece(
        name = "Denimowe mom jeans",
        category = WardrobeCategory.BOTTOM,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.CASUAL, ClothingStyle.VINTAGE)
    ),
    WardrobePiece(
        name = "Plisowana spódnica midi",
        category = WardrobeCategory.BOTTOM,
        colorFamily = ColorFamily.PASTEL_WARM,
        clothingStyles = setOf(ClothingStyle.BOHO, ClothingStyle.GLAMOUR)
    ),
    WardrobePiece(
        name = "Sukienka slip dress w kolorze pudrowego różu",
        category = WardrobeCategory.DRESS,
        colorFamily = ColorFamily.PASTEL_WARM,
        clothingStyles = setOf(ClothingStyle.GLAMOUR, ClothingStyle.MINIMALISTYCZNY)
    ),
    WardrobePiece(
        name = "Wełniany kardigan w kolorze kości słoniowej",
        category = WardrobeCategory.LAYER,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.BOHO, ClothingStyle.CASUAL)
    ),
    WardrobePiece(
        name = "Krótki żakiet z fakturą tweed",
        category = WardrobeCategory.LAYER,
        colorFamily = ColorFamily.PASTEL_COOL,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.VINTAGE)
    ),
    WardrobePiece(
        name = "Pastelowe sneakersy",
        category = WardrobeCategory.SHOES,
        colorFamily = ColorFamily.PASTEL_COOL,
        clothingStyles = setOf(ClothingStyle.CASUAL, ClothingStyle.SPORTOWY)
    ),
    WardrobePiece(
        name = "Satynowe szpilki w kolorze nude",
        category = WardrobeCategory.SHOES,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.GLAMOUR, ClothingStyle.KLASYCZNY)
    ),
    WardrobePiece(
        name = "Skórzane loafersy w kolorze koniaku",
        category = WardrobeCategory.SHOES,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.MINIMALISTYCZNY)
    ),
    WardrobePiece(
        name = "Ażurowy sweter z bawełny organicznej",
        category = WardrobeCategory.LAYER,
        colorFamily = ColorFamily.PASTEL_WARM,
        clothingStyles = setOf(ClothingStyle.BOHO, ClothingStyle.CASUAL)
    ),
    WardrobePiece(
        name = "Jedwabny top na ramiączkach",
        category = WardrobeCategory.TOP,
        colorFamily = ColorFamily.PASTEL_WARM,
        clothingStyles = setOf(ClothingStyle.GLAMOUR, ClothingStyle.BOHO)
    ),
    WardrobePiece(
        name = "Czarne cygaretki",
        category = WardrobeCategory.BOTTOM,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.MINIMALISTYCZNY)
    ),
    WardrobePiece(
        name = "Futrzana kamizelka w odcieniu karmelu",
        category = WardrobeCategory.LAYER,
        colorFamily = ColorFamily.PASTEL_WARM,
        clothingStyles = setOf(ClothingStyle.BOHO, ClothingStyle.GLAMOUR)
    ),
    WardrobePiece(
        name = "Białe trampki z grubą podeszwą",
        category = WardrobeCategory.SHOES,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.CASUAL, ClothingStyle.MINIMALISTYCZNY)
    ),
    WardrobePiece(
        name = "Satynowa sukienka kopertowa w kolorze szampana",
        category = WardrobeCategory.DRESS,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.GLAMOUR, ClothingStyle.KLASYCZNY)
    ),
    WardrobePiece(
        name = "Wełniany golf w kolorze grafitowym",
        category = WardrobeCategory.TOP,
        colorFamily = ColorFamily.NEUTRAL,
        clothingStyles = setOf(ClothingStyle.MINIMALISTYCZNY, ClothingStyle.KLASYCZNY)
    ),
    WardrobePiece(
        name = "Kobaltowa marynarka o dopasowanym kroju",
        category = WardrobeCategory.LAYER,
        colorFamily = ColorFamily.BOLD,
        clothingStyles = setOf(ClothingStyle.KLASYCZNY, ClothingStyle.GLAMOUR)
    )
)

fun generateOutfitSuggestions(limit: Int = 5): List<OutfitSuggestion> {
    val byCategory = wardrobePieces.groupBy { it.category }

    val tops = byCategory[WardrobeCategory.TOP].orEmpty()
    val bottoms = byCategory[WardrobeCategory.BOTTOM].orEmpty()
    val dresses = byCategory[WardrobeCategory.DRESS].orEmpty()
    val layers = byCategory[WardrobeCategory.LAYER].orEmpty()
    val shoes = byCategory[WardrobeCategory.SHOES].orEmpty()

    val outfits = mutableListOf<OutfitSuggestion>()
    val random = Random(System.currentTimeMillis())

    fun paletteIsHarmonious(pieces: List<WardrobePiece>): Boolean {
        val colors = pieces.map { it.colorFamily }
        return colors.all { base ->
            colors.all { partner ->
                base == partner || colorHarmony[base]?.contains(partner) == true
            }
        }
    }

    fun selectClothingStyle(pieces: List<WardrobePiece>): ClothingStyle {
        val votes = pieces.flatMap { it.clothingStyles }.groupingBy { it }.eachCount()
        if (votes.isEmpty()) return ClothingStyle.CASUAL
        val maxVote = votes.maxByOrNull { it.value }?.value ?: 1
        val strongest = votes.filterValues { it == maxVote }.keys
        return strongest.random(random)
    }

    fun mapLanguageStyle(clothingStyle: ClothingStyle): LanguageStyle = when (clothingStyle) {
        ClothingStyle.KLASYCZNY -> LanguageStyle.URZEDOWY
        ClothingStyle.CASUAL -> LanguageStyle.POTOCZNY
        ClothingStyle.SPORTOWY -> LanguageStyle.POTOCZNY
        ClothingStyle.BOHO -> LanguageStyle.ARTYSTYCZNY
        ClothingStyle.GLAMOUR -> LanguageStyle.ARTYSTYCZNY
        ClothingStyle.VINTAGE -> LanguageStyle.ARTYSTYCZNY
        ClothingStyle.MINIMALISTYCZNY -> LanguageStyle.NAUKOWY
    }

    fun mapInteriorStyle(clothingStyle: ClothingStyle): InteriorStyle = when (clothingStyle) {
        ClothingStyle.KLASYCZNY -> InteriorStyle.NOWOCZESNY
        ClothingStyle.CASUAL -> InteriorStyle.SKANDYNAWSKI
        ClothingStyle.SPORTOWY -> InteriorStyle.INDUSTRIALNY
        ClothingStyle.BOHO -> InteriorStyle.RUSTYKALNY
        ClothingStyle.GLAMOUR -> InteriorStyle.GLAMOUR
        ClothingStyle.VINTAGE -> InteriorStyle.RUSTYKALNY
        ClothingStyle.MINIMALISTYCZNY -> InteriorStyle.SKANDYNAWSKI
    }

    fun buildTitle(clothingStyle: ClothingStyle, palette: List<ColorFamily>): String {
        val mainColor = palette.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        val colorLabel = mainColor?.displayName ?: "Kolorystyczna harmonia"
        return "$colorLabel x ${clothingStyle.label}"
    }

    fun describePieces(pieces: List<WardrobePiece>): List<String> = pieces.map { it.name }

    val combinations = mutableListOf<List<WardrobePiece>>()

    tops.forEach { top ->
        bottoms.forEach { bottom ->
            shoes.forEach { shoe ->
                val basePieces = listOf(top, bottom, shoe)
                if (paletteIsHarmonious(basePieces)) {
                    val layersForCombo = layers.filter { layer ->
                        paletteIsHarmonious(basePieces + layer)
                    }
                    if (layersForCombo.isNotEmpty()) {
                        combinations.add(basePieces + layersForCombo.random(random))
                    } else {
                        combinations.add(basePieces)
                    }
                }
            }
        }
    }

    dresses.forEach { dress ->
        shoes.forEach { shoe ->
            val basePieces = listOf(dress, shoe)
            if (paletteIsHarmonious(basePieces)) {
                val layersForCombo = layers.filter { layer ->
                    paletteIsHarmonious(basePieces + layer)
                }
                if (layersForCombo.isNotEmpty()) {
                    combinations.add(basePieces + layersForCombo.random(random))
                } else {
                    combinations.add(basePieces)
                }
            }
        }
    }

    val shuffledCombinations = combinations.shuffled(random)

    for (combo in shuffledCombinations) {
        if (outfits.size >= limit) break
        val clothingStyle = selectClothingStyle(combo)
        val languageStyle = mapLanguageStyle(clothingStyle)
        val interiorStyle = mapInteriorStyle(clothingStyle)
        val palette = combo.map { it.colorFamily }
        val title = buildTitle(clothingStyle, palette)
        val suggestion = OutfitSuggestion(
            title = title,
            pieces = describePieces(combo),
            clothingStyle = clothingStyle,
            languageStyle = languageStyle,
            interiorStyle = interiorStyle,
            colorPalette = palette.map(ColorFamily::displayName).distinct()
        )
        outfits.add(suggestion)
    }

    if (outfits.isEmpty()) {
        outfits.add(
            OutfitSuggestion(
                title = "Pastelowy start",
                pieces = listOf("Dodaj więcej elementów do garderoby, aby wygenerować stylizacje"),
                clothingStyle = ClothingStyle.CASUAL,
                languageStyle = LanguageStyle.POTOCZNY,
                interiorStyle = InteriorStyle.SKANDYNAWSKI,
                colorPalette = listOf(ColorFamily.PASTEL_COOL.displayName)
            )
        )
    }

    return outfits
}

private val paletteLabels = mapOf(
    ClothingStyle.KLASYCZNY to "Pastelowa elegancja",
    ClothingStyle.CASUAL to "Weekendowa lekkość",
    ClothingStyle.SPORTOWY to "Athleisure",
    ClothingStyle.BOHO to "Boho dreams",
    ClothingStyle.GLAMOUR to "Wieczorowy blask",
    ClothingStyle.VINTAGE to "Retro vibe",
    ClothingStyle.MINIMALISTYCZNY to "Skandynawska prostota"
)

private val avatarMoods = listOf(
    "Delikatna elegancja",
    "Energia miasta",
    "Creative flow",
    "Chill vibes",
    "Glow up"
)

fun generateVirtualTryOnLook(): VirtualTryOnLook {
    val outfit = generateOutfitSuggestions(limit = 1).first()
    val paletteLabel = paletteLabels[outfit.clothingStyle] ?: "Mix pasteli"
    val mood = avatarMoods.random()
    val story = buildString {
        append("Awatar łączy styl ")
        append(outfit.clothingStyle.label.lowercase())
        append(" z wnętrzarskim klimatem ")
        append(outfit.interiorStyle.label.lowercase())
        append(", tworząc spójną stylizację z elementów: ")
        append(outfit.pieces.joinToString())
        append('.')
    }
    return VirtualTryOnLook(
        outfit = outfit,
        paletteLabel = paletteLabel,
        story = story,
        mood = mood
    )
}
