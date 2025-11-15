package com.example.dressup.data

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dressup.ai.FashionStyle
import com.example.dressup.ai.PersonalPalette
import com.example.dressup.ai.PersonalStyleProfile
import com.example.dressup.ai.StyledLook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "dressup_state")

class DressUpRepository(private val context: Context) {
    private val closetKey = stringPreferencesKey("closet_items")
    private val looksKey = stringPreferencesKey("styled_looks")
    private val profileKey = stringPreferencesKey("personal_profile")
    private val calendarKey = stringPreferencesKey("look_calendar")

    val closetItemsFlow: Flow<List<ClosetItem>> = context.dataStore.data.map { preferences ->
        decodeClosetItems(preferences[closetKey])
    }

    val storedLooksFlow: Flow<List<StoredStyledLook>> = context.dataStore.data.map { preferences ->
        decodeStyledLooks(preferences[looksKey])
    }

    val personalProfileFlow: Flow<PersonalStyleProfile?> = context.dataStore.data.map { preferences ->
        decodeProfile(preferences[profileKey])
    }

    val calendarAssignmentsFlow: Flow<Map<LocalDate, String>> = context.dataStore.data.map { preferences ->
        decodeCalendar(preferences[calendarKey])
    }

    suspend fun saveClosetItems(items: List<ClosetItem>) {
        context.dataStore.edit { preferences ->
            if (items.isEmpty()) {
                preferences.remove(closetKey)
            } else {
                preferences[closetKey] = encodeClosetItems(items)
            }
        }
    }

    suspend fun saveStyledLooks(looks: List<StyledLook>) {
        context.dataStore.edit { preferences ->
            if (looks.isEmpty()) {
                preferences.remove(looksKey)
            } else {
                preferences[looksKey] = encodeStyledLooks(looks)
            }
        }
    }

    suspend fun savePersonalProfile(profile: PersonalStyleProfile?) {
        context.dataStore.edit { preferences ->
            if (profile == null) {
                preferences.remove(profileKey)
            } else {
                preferences[profileKey] = encodeProfile(profile)
            }
        }
    }

    suspend fun saveCalendarAssignments(assignments: Map<LocalDate, String>) {
        context.dataStore.edit { preferences ->
            if (assignments.isEmpty()) {
                preferences.remove(calendarKey)
            } else {
                preferences[calendarKey] = encodeCalendar(assignments)
            }
        }
    }

    private fun encodeClosetItems(items: List<ClosetItem>): String {
        val array = JSONArray()
        items.forEach { item ->
            val json = JSONObject()
            json.put("id", item.id)
            json.put("uri", item.uri.toString())
            json.put("category", item.category.name)
            json.put("styles", JSONArray(item.styles.map(FashionStyle::name)))
            item.notes?.let { json.put("notes", it) }
            json.put("colorTags", JSONArray(item.colorTags))
            array.put(json)
        }
        return array.toString()
    }

    private fun decodeClosetItems(raw: String?): List<ClosetItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    val category = runCatching {
                        ClothingCategory.valueOf(json.optString("category"))
                    }.getOrDefault(ClothingCategory.UNKNOWN)
                    val stylesJson = json.optJSONArray("styles") ?: JSONArray()
                    val styles = buildList {
                        for (i in 0 until stylesJson.length()) {
                            val name = stylesJson.optString(i)
                            runCatching { FashionStyle.valueOf(name) }.getOrNull()?.let { add(it) }
                        }
                    }
                    val colorsJson = json.optJSONArray("colorTags") ?: JSONArray()
                    val colors = buildList {
                        for (i in 0 until colorsJson.length()) {
                            add(colorsJson.optString(i))
                        }
                    }
                    add(
                        ClosetItem(
                            id = json.optString("id"),
                            uri = Uri.parse(json.optString("uri")),
                            category = category,
                            styles = styles,
                            notes = json.optString("notes").takeIf { it.isNotBlank() },
                            colorTags = colors
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeStyledLooks(looks: List<StyledLook>): String {
        val array = JSONArray()
        looks.forEach { look ->
            val json = JSONObject()
            json.put("id", look.id)
            json.put("style", look.style.name)
            json.put("pieceIds", JSONArray(look.pieces.map(ClosetItem::id)))
            json.put("narrative", look.narrative)
            json.put("highlights", JSONArray(look.highlights))
            json.put("advantages", JSONArray(look.advantages))
            array.put(json)
        }
        return array.toString()
    }

    private fun decodeStyledLooks(raw: String?): List<StoredStyledLook> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.getJSONObject(index)
                    val pieceIdsArray = json.optJSONArray("pieceIds") ?: JSONArray()
                    val pieceIds = buildList {
                        for (i in 0 until pieceIdsArray.length()) {
                            add(pieceIdsArray.optString(i))
                        }
                    }
                    val highlightsJson = json.optJSONArray("highlights") ?: JSONArray()
                    val highlights = buildList {
                        for (i in 0 until highlightsJson.length()) {
                            add(highlightsJson.optString(i))
                        }
                    }
                    val advantagesJson = json.optJSONArray("advantages") ?: JSONArray()
                    val advantages = buildList {
                        for (i in 0 until advantagesJson.length()) {
                            add(advantagesJson.optString(i))
                        }
                    }
                    val style = runCatching {
                        FashionStyle.valueOf(json.optString("style"))
                    }.getOrNull()
                    if (style != null) {
                        add(
                            StoredStyledLook(
                                id = json.optString("id"),
                                style = style,
                                pieceIds = pieceIds,
                                narrative = json.optString("narrative"),
                                highlights = highlights,
                                advantages = advantages
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeProfile(profile: PersonalStyleProfile): String {
        val json = JSONObject()
        json.put("selfieUri", profile.selfieUri.toString())
        json.put("eyeColor", profile.eyeColor)
        json.put("hairTone", profile.hairTone)
        json.put("skinTone", profile.skinTone)
        json.put("faceShape", profile.faceShape)

        val palette = JSONObject()
        palette.put("name", profile.palette.name)
        palette.put("description", profile.palette.description)
        palette.put("colors", JSONArray(profile.palette.colors.map { it.value.toLong() }))
        palette.put("suggestions", JSONArray(profile.palette.suggestions))
        json.put("palette", palette)
        return json.toString()
    }

    private fun decodeProfile(raw: String?): PersonalStyleProfile? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JSONObject(raw)
            val paletteJson = json.getJSONObject("palette")
            val colorsJson = paletteJson.getJSONArray("colors")
            val colors = buildList {
                for (i in 0 until colorsJson.length()) {
                    add(Color(colorsJson.getLong(i)))
                }
            }
            val suggestionsJson = paletteJson.optJSONArray("suggestions") ?: JSONArray()
            val suggestions = buildList {
                for (i in 0 until suggestionsJson.length()) {
                    add(suggestionsJson.optString(i))
                }
            }
            PersonalStyleProfile(
                selfieUri = Uri.parse(json.optString("selfieUri")),
                palette = PersonalPalette(
                    name = paletteJson.optString("name"),
                    description = paletteJson.optString("description"),
                    colors = colors,
                    suggestions = suggestions
                ),
                eyeColor = json.optString("eyeColor"),
                hairTone = json.optString("hairTone"),
                skinTone = json.optString("skinTone"),
                faceShape = json.optString("faceShape")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun encodeCalendar(assignments: Map<LocalDate, String>): String {
        val json = JSONObject()
        assignments.forEach { (date, lookId) ->
            json.put(date.toString(), lookId)
        }
        return json.toString()
    }

    private fun decodeCalendar(raw: String?): Map<LocalDate, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val json = JSONObject(raw)
            buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val lookId = json.optString(key)
                    if (lookId.isNotBlank()) {
                        runCatching { LocalDate.parse(key) }.getOrNull()?.let { date ->
                            put(date, lookId)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}

data class StoredStyledLook(
    val id: String,
    val style: FashionStyle,
    val pieceIds: List<String>,
    val narrative: String,
    val highlights: List<String>,
    val advantages: List<String>
)
