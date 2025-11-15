package com.example.dressup.ui.state

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dressup.ai.PersonalStyleProfile
import com.example.dressup.ai.StyledLook
import com.example.dressup.data.ClosetItem
import com.example.dressup.data.DressUpRepository
import com.example.dressup.data.StoredStyledLook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class DressUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DressUpRepository(application)

    private val _closetItems = MutableStateFlow<List<ClosetItem>>(emptyList())
    val closetItems: StateFlow<List<ClosetItem>> = _closetItems.asStateFlow()

    private val _personalProfile = MutableStateFlow<PersonalStyleProfile?>(null)
    val personalProfile: StateFlow<PersonalStyleProfile?> = _personalProfile.asStateFlow()

    private val _styledLooks = MutableStateFlow<List<StyledLook>>(emptyList())
    val styledLooks: StateFlow<List<StyledLook>> = _styledLooks.asStateFlow()

    private val storedLooks = MutableStateFlow<List<StoredStyledLook>>(emptyList())
    private val calendarAssignments = MutableStateFlow<Map<LocalDate, String>>(emptyMap())
    private val _calendarLooks = MutableStateFlow<Map<LocalDate, StyledLook>>(emptyMap())
    val calendarLooks: StateFlow<Map<LocalDate, StyledLook>> = _calendarLooks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.closetItemsFlow.collect { items ->
                _closetItems.value = items
                rebuildStyledLooks(items, storedLooks.value)
            }
        }
        viewModelScope.launch {
            repository.personalProfileFlow.collect { profile ->
                _personalProfile.value = profile
            }
        }
        viewModelScope.launch {
            repository.storedLooksFlow.collect { looks ->
                storedLooks.value = looks
                rebuildStyledLooks(_closetItems.value, looks)
            }
        }
        viewModelScope.launch {
            repository.calendarAssignmentsFlow.collect { assignments ->
                updateCalendarAssignments(assignments, persist = false)
                rebuildCalendar(assignments, _styledLooks.value)
            }
        }
    }

    fun addClosetItem(item: ClosetItem): Boolean {
        val current = _closetItems.value
        if (current.any { it.uri == item.uri }) {
            return false
        }
        val updated = listOf(item) + current
        _closetItems.value = updated
        persistCloset(updated)
        return true
    }

    fun replaceClosetItem(updated: ClosetItem) {
        val next = _closetItems.value.map { existing ->
            if (existing.id == updated.id) updated else existing
        }
        _closetItems.value = next
        persistCloset(next)
    }

    fun removeByUri(uri: Uri) {
        val next = _closetItems.value.filterNot { it.uri == uri }
        _closetItems.value = next
        persistCloset(next)
    }

    fun updatePersonalProfile(profile: PersonalStyleProfile) {
        _personalProfile.value = profile
        viewModelScope.launch {
            repository.savePersonalProfile(profile)
        }
    }

    fun clearPersonalProfile() {
        _personalProfile.value = null
        viewModelScope.launch {
            repository.savePersonalProfile(null)
        }
    }

    fun saveStyledLooks(looks: List<StyledLook>) {
        _styledLooks.value = looks
        viewModelScope.launch {
            repository.saveStyledLooks(looks)
        }
    }

    fun updateLook(updated: StyledLook) {
        val next = _styledLooks.value.map { existing ->
            if (existing.id == updated.id) updated else existing
        }
        saveStyledLooks(next)
    }

    fun clearStyledLooks() {
        _styledLooks.value = emptyList()
        viewModelScope.launch {
            repository.saveStyledLooks(emptyList())
        }
    }

    fun assignLookToDate(date: LocalDate, look: StyledLook) {
        val updated = calendarAssignments.value.toMutableMap().apply {
            put(date, look.id)
        }
        updateCalendarAssignments(updated, persist = true)
        rebuildCalendar(updated, _styledLooks.value)
    }

    fun removeCalendarEntry(date: LocalDate) {
        val updated = calendarAssignments.value.toMutableMap().apply {
            remove(date)
        }
        updateCalendarAssignments(updated, persist = true)
        rebuildCalendar(updated, _styledLooks.value)
    }

    private fun persistCloset(items: List<ClosetItem>) {
        viewModelScope.launch {
            repository.saveClosetItems(items)
        }
    }

    private fun rebuildStyledLooks(items: List<ClosetItem>, stored: List<StoredStyledLook>) {
        if (stored.isEmpty()) {
            _styledLooks.value = emptyList()
            rebuildCalendar(calendarAssignments.value, emptyList())
            return
        }
        val itemsById = items.associateBy(ClosetItem::id)
        val rebuilt = stored.mapNotNull { storedLook ->
            val pieces = storedLook.pieceIds.mapNotNull(itemsById::get)
            if (pieces.isEmpty()) {
                null
            } else {
                StyledLook(
                    id = storedLook.id,
                    style = storedLook.style,
                    pieces = pieces,
                    narrative = storedLook.narrative,
                    highlights = storedLook.highlights,
                    advantages = storedLook.advantages
                )
            }
        }
        _styledLooks.value = rebuilt
        rebuildCalendar(calendarAssignments.value, rebuilt)
    }

    private fun updateCalendarAssignments(assignments: Map<LocalDate, String>, persist: Boolean) {
        calendarAssignments.value = assignments
        if (persist) {
            viewModelScope.launch {
                repository.saveCalendarAssignments(assignments)
            }
        }
    }

    private fun rebuildCalendar(assignments: Map<LocalDate, String>, looks: List<StyledLook>) {
        if (assignments.isEmpty() || looks.isEmpty()) {
            if (_calendarLooks.value.isNotEmpty()) {
                _calendarLooks.value = emptyMap()
            }
            if (assignments.isEmpty()) {
                updateCalendarAssignments(emptyMap(), persist = false)
            }
            return
        }
        val byId = looks.associateBy(StyledLook::id)
        val validAssignments = mutableMapOf<LocalDate, String>()
        val mapped = mutableMapOf<LocalDate, StyledLook>()
        assignments.forEach { (date, lookId) ->
            val look = byId[lookId]
            if (look != null) {
                validAssignments[date] = lookId
                mapped[date] = look
            }
        }
        _calendarLooks.value = mapped
        if (validAssignments.size != assignments.size) {
            updateCalendarAssignments(validAssignments, persist = true)
        }
    }
}
