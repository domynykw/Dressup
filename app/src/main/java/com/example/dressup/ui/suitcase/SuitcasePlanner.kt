package com.example.dressup.ui.suitcase

import com.example.dressup.ui.styling.ClothingStyle
import com.example.dressup.ui.styling.InteriorStyle
import com.example.dressup.ui.styling.LanguageStyle
import com.example.dressup.ui.styling.OutfitSuggestion
import com.example.dressup.ui.styling.generateOutfitSuggestions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GeoLocation(
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = buildString {
            append(name)
            admin1?.takeIf { it.isNotBlank() }?.let {
                append(", ")
                append(it)
            }
            append(", ")
            append(country)
        }
}

data class DailyForecast(
    val date: LocalDate,
    val minTemperature: Double,
    val maxTemperature: Double,
    val precipitationProbability: Int
) {
    val averageTemperature: Double
        get() = (minTemperature + maxTemperature) / 2.0

    val summary: String
        get() = String.format(
            Locale("pl"),
            "%s: %.1f°C / %.1f°C, deszcz %d%%",
            date.format(DATE_FORMATTER),
            minTemperature,
            maxTemperature,
            precipitationProbability
        )
}

data class DailyPackingSuggestion(
    val date: LocalDate,
    val displayDate: String,
    val forecastSummary: String,
    val outfit: OutfitSuggestion,
    val reason: String
)

data class TravelPlan(
    val id: Long = System.currentTimeMillis(),
    val location: GeoLocation,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val forecasts: List<DailyForecast>,
    val packingSuggestions: List<DailyPackingSuggestion>,
    val shoppingTips: List<String>
)

private enum class TemperatureBand { COLD, MILD, WARM, HOT }

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))
private val HTTP_CLIENT = OkHttpClient()

suspend fun searchDestination(query: String): List<GeoLocation> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    val url = "https://geocoding-api.open-meteo.com/v1/search?name=${query.trim()}&count=5&language=pl&format=json"
    val request = Request.Builder().url(url).build()
    HTTP_CLIENT.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@use emptyList()
        val body = response.body?.string() ?: return@use emptyList()
        val json = JSONObject(body)
        val results = json.optJSONArray("results") ?: return@use emptyList()
        buildList {
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                add(
                    GeoLocation(
                        name = item.optString("name"),
                        country = item.optString("country"),
                        admin1 = item.optString("admin1"),
                        latitude = item.optDouble("latitude"),
                        longitude = item.optDouble("longitude")
                    )
                )
            }
        }
    }
}

suspend fun fetchWeatherForecast(
    location: GeoLocation,
    startDate: LocalDate,
    endDate: LocalDate
): List<DailyForecast> = withContext(Dispatchers.IO) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val url = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&start_date=${startDate.format(formatter)}&end_date=${endDate.format(formatter)}&language=pl"
    val request = Request.Builder().url(url).build()
    HTTP_CLIENT.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Weather request failed: ${response.code}")
        val body = response.body?.string() ?: throw IOException("Empty weather response")
        val json = JSONObject(body)
        val daily = json.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val mins = daily.getJSONArray("temperature_2m_min")
        val maxs = daily.getJSONArray("temperature_2m_max")
        val precipitation = daily.optJSONArray("precipitation_probability_max")
        buildList {
            for (i in 0 until dates.length()) {
                val date = LocalDate.parse(dates.getString(i))
                val min = mins.optDouble(i)
                val max = maxs.optDouble(i)
                val rain = precipitation?.optInt(i) ?: 0
                add(
                    DailyForecast(
                        date = date,
                        minTemperature = min,
                        maxTemperature = max,
                        precipitationProbability = rain
                    )
                )
            }
        }
    }
}

suspend fun prepareTravelPlan(
    location: GeoLocation,
    startDate: LocalDate,
    endDate: LocalDate
): TravelPlan {
    val forecasts = fetchWeatherForecast(location, startDate, endDate)
    val outfits = generateOutfitSuggestions(limit = 30).toMutableList()
    val packing = buildPackingSuggestions(forecasts, outfits)
    return TravelPlan(
        location = location,
        startDate = startDate,
        endDate = endDate,
        forecasts = forecasts,
        packingSuggestions = packing.first,
        shoppingTips = packing.second
    )
}

private fun buildPackingSuggestions(
    forecasts: List<DailyForecast>,
    outfitPool: MutableList<OutfitSuggestion>
): Pair<List<DailyPackingSuggestion>, List<String>> {
    val suggestions = mutableListOf<DailyPackingSuggestion>()
    val shoppingTips = linkedSetOf<String>()

    forecasts.forEach { forecast ->
        val band = forecast.toTemperatureBand()
        val rainy = forecast.precipitationProbability >= 60
        val chosen = chooseOutfitForBand(outfitPool, band, rainy)
        val outfit = chosen.first ?: fallbackOutfit(band, rainy).also {
            shoppingTips += fallbackShoppingHint(band, rainy)
        }
        val reason = chosen.second ?: reasonForBand(band, rainy)
        val displayDate = forecast.date.format(DATE_FORMATTER)
        val forecastSummary = String.format(
            Locale("pl"),
            "%.1f°C / %.1f°C • Deszcz: %d%%",
            forecast.minTemperature,
            forecast.maxTemperature,
            forecast.precipitationProbability
        )
        suggestions += DailyPackingSuggestion(
            date = forecast.date,
            displayDate = displayDate,
            forecastSummary = forecastSummary,
            outfit = outfit,
            reason = reason
        )
    }

    if (outfitPool.size < forecasts.size) {
        shoppingTips += "Dodaj do garderoby kilka uniwersalnych zestawów, aby uniknąć powtórek stylizacji."
    }

    return suggestions to shoppingTips.toList()
}

private fun DailyForecast.toTemperatureBand(): TemperatureBand {
    val avg = averageTemperature
    return when {
        avg < 8 -> TemperatureBand.COLD
        avg < 16 -> TemperatureBand.MILD
        avg < 23 -> TemperatureBand.WARM
        else -> TemperatureBand.HOT
    }
}

private fun chooseOutfitForBand(
    outfitPool: MutableList<OutfitSuggestion>,
    band: TemperatureBand,
    rainy: Boolean
): Pair<OutfitSuggestion?, String?> {
    val preferred = outfitPool.firstOrNull { it.matchesBand(band, rainy) }
    return if (preferred != null) {
        outfitPool.remove(preferred)
        preferred to reasonForBand(band, rainy)
    } else {
        val alternative = outfitPool.firstOrNull()
        if (alternative != null) {
            outfitPool.remove(alternative)
            alternative to "Zestaw najlepiej odpowiada dostępnej garderobie."
        } else {
            null to null
        }
    }
}

private fun OutfitSuggestion.matchesBand(band: TemperatureBand, rainy: Boolean): Boolean {
    val text = (pieces + title).joinToString(" ").lowercase(Locale.getDefault())
    val warmKeywords = listOf("wełn", "sweter", "golf", "kardigan", "marynark", "płaszcz")
    val breezyKeywords = listOf("sukien", "top", "satyn", "lnian", "jedwab", "koszulk", "bluzk")
    val sportyKeywords = listOf("sneakers", "trampki", "dres")
    val rainKeywords = listOf("kurtk", "płaszcz", "marynark", "bomber")

    val warmScore = warmKeywords.count { text.contains(it) }
    val breezyScore = breezyKeywords.count { text.contains(it) }

    val rainReady = rainKeywords.any { text.contains(it) }

    return when (band) {
        TemperatureBand.COLD -> warmScore >= 2 || (warmScore >= 1 && rainReady)
        TemperatureBand.MILD -> warmScore >= 1 || sportyKeywords.any { text.contains(it) }
        TemperatureBand.WARM -> breezyScore >= 1 && warmScore == 0
        TemperatureBand.HOT -> breezyScore >= 2 || text.contains("sukien")
    } && (!rainy || rainReady)
}

private fun reasonForBand(band: TemperatureBand, rainy: Boolean): String = when (band) {
    TemperatureBand.COLD -> if (rainy) {
        "Zestaw zawiera ciepłe warstwy i ochronę przed deszczem."
    } else {
        "Ciepłe warstwy utrzymają komfort w chłodniejsze dni."
    }

    TemperatureBand.MILD -> if (rainy) {
        "Lekka warstwa zewnętrzna poradzi sobie z przelotnym deszczem."
    } else {
        "Zestaw łączy wygodę i lekkie warstwy na zmienną pogodę."
    }

    TemperatureBand.WARM -> if (rainy) {
        "Przewiewne materiały z dodatkową warstwą na ewentualny deszcz."
    } else {
        "Przewiewne materiały utrzymają komfort w ciepłe dni."
    }

    TemperatureBand.HOT -> if (rainy) {
        "Lekkie elementy i warstwa na nagłe opady."
    } else {
        "Najlżejsze elementy garderoby pozwolą przetrwać wysokie temperatury."
    }
}

private fun fallbackOutfit(band: TemperatureBand, rainy: Boolean): OutfitSuggestion {
    val pieces = when (band) {
        TemperatureBand.COLD -> listOf("Wełniany płaszcz", "Ciepły sweter", "Termiczne spodnie", "Buty za kostkę")
        TemperatureBand.MILD -> listOf("Warstwowa marynarka", "Długie spodnie", "Koszula z długim rękawem")
        TemperatureBand.WARM -> listOf("Lekka sukienka", "Sandały lub loafersy", "Opcjonalnie cienki kardigan")
        TemperatureBand.HOT -> listOf("Lniany top", "Szorty lub spódnica", "Sandały")
    }
    val adjustedPieces = if (rainy) pieces + "Płaszcz przeciwdeszczowy" else pieces
    return OutfitSuggestion(
        title = "Awaryjna stylizacja",
        pieces = adjustedPieces,
        clothingStyle = when (band) {
            TemperatureBand.COLD -> ClothingStyle.KLASYCZNY
            TemperatureBand.MILD -> ClothingStyle.CASUAL
            TemperatureBand.WARM -> ClothingStyle.BOHO
            TemperatureBand.HOT -> ClothingStyle.MINIMALISTYCZNY
        },
        languageStyle = LanguageStyle.POTOCZNY,
        interiorStyle = InteriorStyle.NOWOCZESNY,
        colorPalette = listOf("Neutralny")
    )
}

private fun fallbackShoppingHint(band: TemperatureBand, rainy: Boolean): String {
    val base = when (band) {
        TemperatureBand.COLD -> "Rozważ zakup wełnianego płaszcza lub grubego swetra na chłód."
        TemperatureBand.MILD -> "Przydadzą się lekkie warstwy, np. kardigan lub marynarka."
        TemperatureBand.WARM -> "Dodaj przewiewne sukienki lub lniane koszule na cieplejsze dni."
        TemperatureBand.HOT -> "Zainwestuj w bardzo lekkie, oddychające tkaniny, np. len."
    }
    return if (rainy) "$base Płaszcz przeciwdeszczowy również się przyda." else base
}

fun formatDateRange(start: LocalDate, end: LocalDate): String {
    val startText = start.format(DATE_FORMATTER)
    val endText = end.format(DATE_FORMATTER)
    return "$startText – $endText"
}

fun formatDateForDisplay(dateMillis: Long?): LocalDate? = dateMillis?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
}
