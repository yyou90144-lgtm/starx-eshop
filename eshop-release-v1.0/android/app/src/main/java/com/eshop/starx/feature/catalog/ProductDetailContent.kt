package com.eshop.starx.feature.catalog

import com.eshop.starx.core.network.NetworkModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class StructuredProductDetail(
    val version: Int = 0,
    val highlights: List<String> = emptyList(),
    val paragraphs: List<String> = emptyList(),
    val specs: List<ProductDetailSpec> = emptyList(),
)

@Serializable
data class ProductDetailSpec(
    val label: String = "",
    val value: String = "",
)

/** Parses the versioned product-detail payload used by the shared PC and Android backend. */
fun parseProductDetail(raw: String?): StructuredProductDetail? {
    val text = raw?.trim().orEmpty()
    if (!text.startsWith("{")) return null

    return runCatching {
        NetworkModule.json.decodeFromString<StructuredProductDetail>(text)
    }.getOrNull()?.takeIf { it.version == 1 }
}
