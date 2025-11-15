package com.example.dressup.ai

import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class PersonalPalette(
    val name: String,
    val description: String,
    val colors: List<Color>,
    val suggestions: List<String>
)

data class PersonalStyleProfile(
    val selfieUri: Uri,
    val palette: PersonalPalette,
    val eyeColor: String,
    val hairTone: String,
    val skinTone: String,
    val faceShape: String
) {
    val keywordSummary: List<String>
        get() = listOf(
            "Oczy $eyeColor",
            "Cera $skinTone",
            "Paleta ${palette.name}"
        )
}

object PersonalStyleAnalyzer {
    private val palettes = listOf(
        PersonalPalette(
            name = "Chłodne lato",
            description = "Delikatne chłodne odcienie podbijają jasną cerę i pastelowe dodatki.",
            colors = listOf(Color(0xFFF6A7C1), Color(0xFFB79EFF), Color(0xFFA6D1FF), Color(0xFF8DE8D9)),
            suggestions = listOf(
                "Sięgnij po srebrną biżuterię",
                "Łącz błękity z lawendą",
                "Postaw na chłodne róże w makijażu"
            )
        ),
        PersonalPalette(
            name = "Ciepła jesień",
            description = "Soczyste, głębokie barwy podkreślają złote refleksy i ciemniejsze włosy.",
            colors = listOf(Color(0xFFE6B980), Color(0xFFCD6B3A), Color(0xFF9A4D2E), Color(0xFF5E3D31)),
            suggestions = listOf(
                "Noś karmelowe płaszcze",
                "Dodaj oliwkową zieleń",
                "Podbij look złotą biżuterią"
            )
        ),
        PersonalPalette(
            name = "Zgaszona wiosna",
            description = "Pastelowe kolory o ciepłej podstawie nadają świeżości i lekkości.",
            colors = listOf(Color(0xFFF9D5E5), Color(0xFFE2F0CB), Color(0xFFB3D6FF), Color(0xFFFFF5BA)),
            suggestions = listOf(
                "Łącz pastele w total look",
                "Wybieraj jasne dżinsy",
                "Mieszaj beże z pudrowym różem"
            )
        ),
        PersonalPalette(
            name = "Zimowy kontrast",
            description = "Wyraziste kontrasty, chłodne błękity i głęboka czerń tworzą elegancki efekt.",
            colors = listOf(Color(0xFF0E1D4A), Color(0xFF6D83F2), Color(0xFFEAF0FF), Color(0xFF0A0A0A)),
            suggestions = listOf(
                "Zestawiaj czerń z kobaltem",
                "Dodaj srebrne akcenty",
                "Postaw na kontrastowe makijaże"
            )
        )
    )

    private val eyeColors = listOf("piwne", "zielone", "niebieskie", "szare", "bursztynowe")
    private val hairTones = listOf(
        "chłodny blond",
        "złoty blond",
        "jasny brąz",
        "czekoladowy brąz",
        "czarny połysk",
        "miedziany blond"
    )
    private val skinTones = listOf("jasna porcelana", "beż neutralny", "oliwkowa", "ciemny brąz", "chłodny beż")
    private val faceShapes = listOf("owalna", "serce", "okrągła", "diamentowa", "kwadratowa")

    fun analyzeSelfie(selfieUri: Uri): PersonalStyleProfile {
        val hash = abs(selfieUri.hashCode())
        val palette = palettes[hash % palettes.size]
        val eye = eyeColors[hash % eyeColors.size]
        val hair = hairTones[(hash / 3) % hairTones.size]
        val skin = skinTones[(hash / 5) % skinTones.size]
        val face = faceShapes[(hash / 7) % faceShapes.size]
        return PersonalStyleProfile(
            selfieUri = selfieUri,
            palette = palette,
            eyeColor = eye,
            hairTone = hair,
            skinTone = skin,
            faceShape = face
        )
    }
}
