package com.example.dressup.ai

import com.example.dressup.data.ClothingCategory
import java.util.Locale

private typealias KeywordMap<K> = Map<K, List<String>>

object FashionKnowledgeBase {
    private val categoryKeywords: KeywordMap<ClothingCategory> = mapOf(
        ClothingCategory.TOPS to listOf("bluz", "koszul", "t-shirt", "tshirt", "top", "golf", "sweter", "shirt"),
        ClothingCategory.BOTTOMS to listOf("spodni", "jeans", "denim", "spódnic", "leggin", "leggins", "pants"),
        ClothingCategory.DRESSES to listOf("sukien", "dress", "kombinezon"),
        ClothingCategory.OUTERWEAR to listOf("płaszcz", "plaszcz", "marynark", "kurtk", "żakiet", "żakiet", "ramonesk", "kamizelk"),
        ClothingCategory.SHOES to listOf("but", "sneaker", "trampk", "szpil", "loafer", "mokasyn", "boot", "obuw"),
        ClothingCategory.ACCESSORIES to listOf("torb", "toreb", "pasek", "kapelusz", "okular", "chusta", "apaszk", "biż", "biz"),
        ClothingCategory.UNKNOWN to emptyList()
    )

    private val styleKeywords: KeywordMap<FashionStyle> = mapOf(
        FashionStyle.CLASSIC to listOf("marynark", "trencz", "koszul", "garnitur", "plis", "czarn", "biel"),
        FashionStyle.SMART_CASUAL to listOf("cygaret", "chinos", "mokasyn", "blezer", "żakiet", "plisowana", "koszulka polo"),
        FashionStyle.CASUAL to listOf("jeans", "denim", "basic", "t-shirt", "tshirt", "dres", "sweter", "cardigan", "bluza"),
        FashionStyle.MINIMALIST to listOf("beż", "bez", "szary", "golf", "prosty", "monochrom", "basic", "minimal"),
        FashionStyle.SPORTY to listOf("sport", "sneaker", "leggins", "trening", "dres", "technicz", "athleisure"),
        FashionStyle.BOHO to listOf("boho", "frędzl", "haft", "koronk", "maxi", "etno", "luźn"),
        FashionStyle.GLAMOUR to listOf("satyn", "błysk", "cek", "szpil", "wieczor", "koktajl", "futrz"),
        FashionStyle.ROMANTIC to listOf("kwiat", "falban", "plis", "pastel", "delikat", "koronk"),
        FashionStyle.STREETWEAR to listOf("oversize", "hoodie", "street", "cargo", "snapback", "crewneck"),
        FashionStyle.ROCK to listOf("skór", "ramonesk", "stud", "czarn", "metal", "rock")
    )

    private val fallbackStylesByCategory: Map<ClothingCategory, List<FashionStyle>> = mapOf(
        ClothingCategory.TOPS to listOf(FashionStyle.CASUAL, FashionStyle.CLASSIC, FashionStyle.SMART_CASUAL),
        ClothingCategory.BOTTOMS to listOf(FashionStyle.CASUAL, FashionStyle.MINIMALIST, FashionStyle.SMART_CASUAL),
        ClothingCategory.DRESSES to listOf(FashionStyle.GLAMOUR, FashionStyle.ROMANTIC, FashionStyle.BOHO),
        ClothingCategory.OUTERWEAR to listOf(FashionStyle.CLASSIC, FashionStyle.STREETWEAR, FashionStyle.ROCK),
        ClothingCategory.SHOES to listOf(FashionStyle.CLASSIC, FashionStyle.CASUAL, FashionStyle.GLAMOUR),
        ClothingCategory.ACCESSORIES to listOf(FashionStyle.GLAMOUR, FashionStyle.BOHO, FashionStyle.MINIMALIST),
        ClothingCategory.UNKNOWN to FashionStyle.values().toList()
    )

    private val colorKeywords: Map<String, List<String>> = mapOf(
        "Baby blue" to listOf("baby blue", "błękit", "blekit", "blue", "turkus", "denim"),
        "Liliowy" to listOf("lili", "lilac", "fiolet", "lawend"),
        "Pudrowy róż" to listOf("róż", "roz", "pink", "blush"),
        "Beż" to listOf("beż", "bez", "taupe", "camel", "karmel"),
        "Biel" to listOf("biały", "bialy", "white", "krem"),
        "Czarny" to listOf("czarn", "black", "grafit", "antracyt"),
        "Granat" to listOf("granat", "navy", "kobalt"),
        "Zieleń" to listOf("ziel", "green", "oliwk", "emerald"),
        "Czerwień" to listOf("czerwie", "red", "bord", "wine"),
        "Złoto" to listOf("złot", "zlot", "gold", "miod"),
        "Srebro" to listOf("srebr", "silver", "platyn")
    )

    fun detectCategory(name: String): ClothingCategory {
        val lower = name.lowercase(Locale.getDefault())
        return categoryKeywords.entries.firstOrNull { (_, keywords) ->
            keywords.any { lower.contains(it) }
        }?.key ?: ClothingCategory.UNKNOWN
    }

    fun detectStyles(name: String, category: ClothingCategory): List<FashionStyle> {
        val lower = name.lowercase(Locale.getDefault())
        val scored = styleKeywords.mapValues { (_, keywords) ->
            keywords.count { lower.contains(it) }
        }
        val maxScore = scored.values.maxOrNull() ?: 0
        val matched = scored.filterValues { it == maxScore && it > 0 }.keys.toList()
        if (matched.isNotEmpty()) {
            return matched
        }
        return fallbackStylesByCategory[category]?.take(2) ?: FashionStyle.values().take(2)
    }

    fun detectColorTags(name: String): List<String> {
        val lower = name.lowercase(Locale.getDefault())
        val matches = colorKeywords.mapNotNull { (label, keywords) ->
            if (keywords.any { lower.contains(it) }) label else null
        }
        if (matches.isNotEmpty()) {
            return matches.distinct()
        }
        return listOf("Neutralny")
    }

    fun narrative(style: FashionStyle, highlightedPieces: List<String>): String {
        val tone = when (style) {
            FashionStyle.CLASSIC -> "elegancji i ponadczasowych linii"
            FashionStyle.SMART_CASUAL -> "swobodnej elegancji idealnej na spotkanie"
            FashionStyle.CASUAL -> "codziennej wygody"
            FashionStyle.MINIMALIST -> "czystych form i spokojnej palety"
            FashionStyle.SPORTY -> "energii athleisure"
            FashionStyle.BOHO -> "artystycznej swobody"
            FashionStyle.GLAMOUR -> "wieczorowego blasku"
            FashionStyle.ROMANTIC -> "subtelnego romantyzmu"
            FashionStyle.STREETWEAR -> "miejskiego charakteru"
            FashionStyle.ROCK -> "zadziornej pewności siebie"
        }
        val pieces = highlightedPieces.joinToString()
        return "Połączenie $pieces buduje historię $tone."
    }
}
