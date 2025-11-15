package com.example.dressup.ui.suitcase

import com.example.dressup.ai.FashionStylist
import com.example.dressup.ai.FashionStyle
import com.example.dressup.ai.PersonalStyleProfile
import com.example.dressup.ai.StyledLook
import com.example.dressup.data.ClothingCategory
import com.example.dressup.data.ClosetItem
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

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))
private val HTTP_CLIENT = OkHttpClient()

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

data class TravelActivity(
    val name: String,
    val styleHints: List<FashionStyle>
)

data class DailyPackingSuggestion(
    val date: LocalDate,
    val displayDate: String,
    val forecastSummary: String,
    val activityName: String,
    val look: StyledLook,
    val contextHighlights: List<String>,
    val contingency: String?
)

data class TravelPlan(
    val id: Long = System.currentTimeMillis(),
    val location: GeoLocation,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val forecasts: List<DailyForecast>,
    val activities: List<String>,
    val packingSuggestions: List<DailyPackingSuggestion>,
    val climateNotes: List<String>,
    val shoppingTips: List<String>
)

private enum class TemperatureBand { COLD, MILD, WARM, HOT }

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
    endDate: LocalDate,
    activities: List<TravelActivity>,
    closetItems: List<ClosetItem>,
    profile: PersonalStyleProfile?
): TravelPlan {
    val forecasts = fetchWeatherForecast(location, startDate, endDate)
    val packing = buildPackingSuggestions(forecasts, activities, closetItems, profile)
    val climateNotes = buildClimateNotes(forecasts)
    return TravelPlan(
        location = location,
        startDate = startDate,
        endDate = endDate,
        forecasts = forecasts,
        activities = activities.map(TravelActivity::name),
        packingSuggestions = packing.first,
        climateNotes = climateNotes,
        shoppingTips = packing.second
    )
}

private fun buildPackingSuggestions(
    forecasts: List<DailyForecast>,
    activities: List<TravelActivity>,
    closetItems: List<ClosetItem>,
    profile: PersonalStyleProfile?
): Pair<List<DailyPackingSuggestion>, List<String>> {
    val stylist = FashionStylist()
    val normalizedActivities = if (activities.isEmpty()) {
        listOf(TravelActivity("Dzienna aktywność", listOf(FashionStyle.CASUAL, FashionStyle.SMART_CASUAL)))
    } else {
        activities
    }
    val styleOrder = normalizedActivities.flatMap { it.styleHints }.ifEmpty { listOf(FashionStyle.CASUAL) }
    val lookPools = styleOrder.distinct().associateWith { style ->
        stylist.generateLooks(closetItems, style, profile, limit = forecasts.size * 2).toMutableList()
    }.filterValues { it.isNotEmpty() }.toMutableMap()

    val suggestions = mutableListOf<DailyPackingSuggestion>()
    val shoppingTips = linkedSetOf<String>()

    forecasts.forEachIndexed { index, forecast ->
        val band = forecast.toTemperatureBand()
        val rainy = forecast.precipitationProbability >= 60
        val activity = normalizedActivities[index % normalizedActivities.size]
        val preferredStyle = selectStyleForActivity(activity, lookPools, band)
        var look = takeLookForStyle(preferredStyle, lookPools)
        if (look == null) {
            val fallbackStyle = lookPools.keys.firstOrNull()
            look = fallbackStyle?.let { takeLookForStyle(it, lookPools) }
        }
        if (look == null) {
            look = createFallbackLook(closetItems, preferredStyle, stylist, profile)
            if (look == null) {
                shoppingTips += "Dodaj więcej elementów w stylu ${activity.name}, aby AI mogła przygotować pełne zestawy."
                look = createFallbackLook(closetItems, FashionStyle.CASUAL, stylist, profile)
            }
        }
        if (look == null) {
            return@forEachIndexed
        }

        if (rainy) {
            shoppingTips += "Na ${forecast.date.format(DATE_FORMATTER)} prognozowane są opady – spakuj warstwę przeciwdeszczową."
        }

        val contextHighlights = buildContextHighlights(activity, band, rainy)
        val contingency = buildContingency(band, rainy)
        val displayDate = forecast.date.format(DATE_FORMATTER)
        val summary = String.format(
            Locale("pl"),
            "%.1f°C / %.1f°C • Deszcz: %d%%",
            forecast.minTemperature,
            forecast.maxTemperature,
            forecast.precipitationProbability
        )

        suggestions += DailyPackingSuggestion(
            date = forecast.date,
            displayDate = displayDate,
            forecastSummary = summary,
            activityName = activity.name,
            look = look,
            contextHighlights = contextHighlights,
            contingency = contingency
        )
    }

    if (lookPools.values.sumOf { it.size } < forecasts.size) {
        shoppingTips += "Rozważ spakowanie dodatkowych bazowych elementów, aby mieć zapasowe stylizacje."
    }

    return suggestions to shoppingTips.toList()
}

private fun buildClimateNotes(forecasts: List<DailyForecast>): List<String> {
    if (forecasts.isEmpty()) return emptyList()
    val notes = mutableListOf<String>()
    val rainyDays = forecasts.count { it.precipitationProbability >= 60 }
    if (rainyDays > 0) {
        notes += "W trakcie wyjazdu możliwe są częste opady – miej przy sobie płaszcz przeciwdeszczowy i wodoodporne obuwie."
    }
    val hotDays = forecasts.count { it.averageTemperature >= 26 }
    if (hotDays > 0) {
        notes += "Spodziewane są bardzo ciepłe dni – postaw na przewiewne materiały i jasne kolory."
    }
    val coolNights = forecasts.count { it.minTemperature <= 10 }
    if (coolNights > 0) {
        notes += "Noce mogą być chłodne – dodaj lekką warstwę ocieplającą do walizki."
    }
    return notes
}

private fun selectStyleForActivity(
    activity: TravelActivity,
    lookPools: Map<FashionStyle, MutableList<StyledLook>>,
    band: TemperatureBand
): FashionStyle {
    val ordered = activity.styleHints.ifEmpty { listOf(FashionStyle.CASUAL) }
    val candidate = ordered.firstOrNull { lookPools[it]?.isNotEmpty() == true }
    if (candidate != null) {
        return candidate
    }
    return when (band) {
        TemperatureBand.HOT -> FashionStyle.BOHO
        TemperatureBand.WARM -> FashionStyle.CASUAL
        TemperatureBand.MILD -> FashionStyle.SMART_CASUAL
        TemperatureBand.COLD -> FashionStyle.CLASSIC
    }
}

private fun takeLookForStyle(
    style: FashionStyle,
    lookPools: MutableMap<FashionStyle, MutableList<StyledLook>>
): StyledLook? {
    val pool = lookPools[style] ?: return null
    if (pool.isEmpty()) {
        lookPools.remove(style)
        return null
    }
    return pool.removeAt(0)
}

private fun createFallbackLook(
    closetItems: List<ClosetItem>,
    targetStyle: FashionStyle,
    stylist: FashionStylist,
    profile: PersonalStyleProfile?
): StyledLook? {
    if (closetItems.isEmpty()) return null
    val dresses = closetItems.filter { it.category == ClothingCategory.DRESSES }
    val tops = closetItems.filter { it.category == ClothingCategory.TOPS }
    val bottoms = closetItems.filter { it.category == ClothingCategory.BOTTOMS }
    val shoes = closetItems.filter { it.category == ClothingCategory.SHOES }
    val outerwear = closetItems.filter { it.category == ClothingCategory.OUTERWEAR }
    val accessories = closetItems.filter { it.category == ClothingCategory.ACCESSORIES }

    val pieces = when {
        dresses.isNotEmpty() && shoes.isNotEmpty() -> buildList {
            add(dresses.first())
            add(shoes.first())
            outerwear.firstOrNull()?.let(::add)
            accessories.firstOrNull()?.let(::add)
        }
        tops.isNotEmpty() && bottoms.isNotEmpty() && shoes.isNotEmpty() -> buildList {
            add(tops.first())
            add(bottoms.first())
            add(shoes.first())
            outerwear.firstOrNull()?.let(::add)
            accessories.firstOrNull()?.let(::add)
        }
        else -> closetItems.take(3)
    }

    return if (pieces.size >= 2) {
        stylist.rebuildLook(pieces, targetStyle, profile)
    } else {
        null
    }
}

private fun buildContextHighlights(
    activity: TravelActivity,
    band: TemperatureBand,
    rainy: Boolean
): List<String> {
    val highlights = mutableListOf("Aktywność: ${activity.name}")
    when (band) {
        TemperatureBand.HOT -> highlights += "Lekkość i przewiewność na wysokie temperatury"
        TemperatureBand.WARM -> highlights += "Komfort na ciepłe dni"
        TemperatureBand.MILD -> highlights += "Warstwowe elementy na zmienną pogodę"
        TemperatureBand.COLD -> highlights += "Ciepłe warstwy na chłodniejsze chwile"
    }
    if (rainy) {
        highlights += "Ochrona przed możliwymi opadami"
    }
    return highlights.distinct()
}

private fun buildContingency(
    band: TemperatureBand,
    rainy: Boolean
): String? {
    return when {
        rainy && (band == TemperatureBand.COLD || band == TemperatureBand.MILD) -> "Awaryjnie zabierz lekką kurtkę przeciwdeszczową i buty z dobrą przyczepnością."
        rainy -> "Dodaj do walizki szybkoschnącą warstwę przeciwdeszczową."
        band == TemperatureBand.HOT -> "Na chłodniejsze wieczory warto mieć cienką narzutkę lub szal."
        band == TemperatureBand.COLD -> "Zapasowa ciepła bluza lub golf przyda się na nocne spadki temperatur."
        else -> null
    }
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

fun formatDateRange(start: LocalDate, end: LocalDate): String {
    val startText = start.format(DATE_FORMATTER)
    val endText = end.format(DATE_FORMATTER)
    return "$startText – $endText"
}

fun formatDateForDisplay(dateMillis: Long?): LocalDate? = dateMillis?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
}
