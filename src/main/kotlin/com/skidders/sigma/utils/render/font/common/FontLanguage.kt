package com.skidders.sigma.utils.render.font.common

enum class FontLanguage(val charCodes: IntArray) {
    ENGLISH(intArrayOf(31, 127, 0, 0)),
    ENGLISH_RUSSIAN(intArrayOf(31, 127, 1024, 1106))
}