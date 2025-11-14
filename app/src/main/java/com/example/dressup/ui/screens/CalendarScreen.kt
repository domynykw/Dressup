package com.example.dressup.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dressup.R
import com.example.dressup.ui.theme.SoftCream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private data class CalendarDay(
    val date: LocalDate?,
    val outfit: String?,
    val mood: String?
)

private val sampleMonth: YearMonth = YearMonth.now()

private val outfitEntries: Map<LocalDate, Pair<String, String>> = mapOf(
    LocalDate.now().withDayOfMonth(1) to ("Marynarka + jeansy + loafers" to "Konferencja – pełna pewność"),
    LocalDate.now().withDayOfMonth(2) to ("Pastelowy dres + sneakersy" to "Home office, wygoda"),
    LocalDate.now().withDayOfMonth(3) to ("Sukienka midi + baleriny" to "Spotkanie z klientem"),
    LocalDate.now().withDayOfMonth(5) to ("Total look lilac" to "Randka w centrum"),
    LocalDate.now().withDayOfMonth(9) to ("Denim on denim" to "Weekendowy spacer"),
    LocalDate.now().withDayOfMonth(12) to ("Oversize sweter + legginsy" to "Chill w domu"),
    LocalDate.now().withDayOfMonth(15) to ("Slip dress + marynarka" to "Teatr"),
    LocalDate.now().withDayOfMonth(18) to ("Lniana koszula + szorty" to "Wypad za miasto"),
    LocalDate.now().withDayOfMonth(22) to ("Golf + cygaretki" to "Prezentacja w pracy"),
    LocalDate.now().withDayOfMonth(27) to ("Pastelowy garnitur" to "Kolacja z przyjaciółmi")
)

private fun buildCalendarDays(month: YearMonth): List<CalendarDay> {
    val firstDayOffset = (month.atDay(1).dayOfWeek.value + 6) % 7 // Monday as first column
    val totalDays = month.lengthOfMonth()
    val items = mutableListOf<CalendarDay>()
    repeat(firstDayOffset) { items.add(CalendarDay(null, null, null)) }
    for (day in 1..totalDays) {
        val date = month.atDay(day)
        val entry = outfitEntries[date]
        items.add(
            CalendarDay(
                date = date,
                outfit = entry?.first,
                mood = entry?.second
            )
        )
    }
    while (items.size % 7 != 0) {
        items.add(CalendarDay(null, null, null))
    }
    return items
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val month = sampleMonth
    val monthLabel = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val days = buildCalendarDays(month)
    val weekdays = stringArrayResource(id = R.array.calendar_weekdays)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.calendar_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(id = R.string.calendar_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.calendar_month_label, monthLabel, month.year),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(weekdays.size) { index ->
                Text(
                    text = weekdays[index],
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            itemsIndexed(
                items = days,
                key = { index, day -> day.date?.toString() ?: "empty_$index" }
            ) { _, day ->
                CalendarCell(day)
            }
        }
    }
}

@Composable
private fun CalendarCell(day: CalendarDay) {
    if (day.date == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
        ) {}
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.calendar_day_label, day.date.dayOfMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            day.outfit?.let {
                Text(
                    text = stringResource(id = R.string.calendar_day_outfit, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
            day.mood?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
