package com.eshop.starx.core.network

import kotlinx.serialization.Serializable

/**
 * 与后端统一响应体对齐：
 * { "code": 0, "message": "success", "data": ... }
 */
@Serializable
data class ApiResponse<T>(
    val code: Int = -1,
    val message: String = "",
    val data: T? = null,
)

@Serializable
data class PageResult<T>(
    val current: Long = 1,
    val size: Long = 20,
    val total: Long = 0,
    val records: List<T> = emptyList(),
)
