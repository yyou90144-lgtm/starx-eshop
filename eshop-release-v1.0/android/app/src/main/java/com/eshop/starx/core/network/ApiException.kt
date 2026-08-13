package com.eshop.starx.core.network

/**
 * 业务或网络层可展示给用户的错误。
 */
class ApiException(
    message: String,
    val httpCode: Int? = null,
    val businessCode: Int? = null,
    val unauthorized: Boolean = false,
) : Exception(message)
