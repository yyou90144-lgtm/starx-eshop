package com.eshop.starx.core.ui

import java.util.Locale

fun formatPrice(value: Double?): String {
    if (value == null) return "—"
    return if (value % 1.0 == 0.0) {
        String.format(Locale.CHINA, "¥%.0f", value)
    } else {
        String.format(Locale.CHINA, "¥%.2f", value)
    }
}

fun formatSpecs(specsJson: String?): String {
    if (specsJson.isNullOrBlank()) return "默认规格"
    // 后端多为 JSON 对象字符串，简单去括号展示；失败则原文
    val trimmed = specsJson.trim()
    return trimmed
        .removePrefix("{")
        .removeSuffix("}")
        .replace("\"", "")
        .replace(",", " · ")
        .replace(":", ": ")
        .ifBlank { "默认规格" }
}
