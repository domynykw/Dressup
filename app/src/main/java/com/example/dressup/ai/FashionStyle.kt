package com.example.dressup.ai

import androidx.annotation.StringRes
import com.example.dressup.R

enum class FashionStyle(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    CLASSIC(R.string.fashion_style_classic, R.string.fashion_style_classic_desc),
    SMART_CASUAL(R.string.fashion_style_smart_casual, R.string.fashion_style_smart_casual_desc),
    CASUAL(R.string.fashion_style_casual, R.string.fashion_style_casual_desc),
    MINIMALIST(R.string.fashion_style_minimalist, R.string.fashion_style_minimalist_desc),
    SPORTY(R.string.fashion_style_sporty, R.string.fashion_style_sporty_desc),
    BOHO(R.string.fashion_style_boho, R.string.fashion_style_boho_desc),
    GLAMOUR(R.string.fashion_style_glamour, R.string.fashion_style_glamour_desc),
    ROMANTIC(R.string.fashion_style_romantic, R.string.fashion_style_romantic_desc),
    STREETWEAR(R.string.fashion_style_streetwear, R.string.fashion_style_streetwear_desc),
    ROCK(R.string.fashion_style_rock, R.string.fashion_style_rock_desc)
}
