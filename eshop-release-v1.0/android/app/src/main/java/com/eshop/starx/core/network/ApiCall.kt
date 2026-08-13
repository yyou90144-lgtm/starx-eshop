package com.eshop.starx.core.network

import retrofit2.HttpException
import java.io.IOException

/**
 * 统一执行 Retrofit 调用并解包 ApiResponse（data 必填）。
 */
suspend fun <T> apiCall(block: suspend () -> ApiResponse<T>): T {
    val response = execute(block)
    return response.data ?: throw ApiException("响应数据为空", businessCode = response.code)
}

/**
 * data 可为空的接口（如 register / logout 返回 success 且 data=null）。
 */
suspend fun <T> apiCallAllowNull(block: suspend () -> ApiResponse<T>): T? {
    val response = execute(block)
    return response.data
}

private suspend fun <T> execute(block: suspend () -> ApiResponse<T>): ApiResponse<T> {
    try {
        val response = block()
        if (response.code != 0) {
            throw ApiException(
                message = response.message.ifBlank { "请求失败" },
                businessCode = response.code,
                unauthorized = response.code == 40101 || response.code == 40102,
            )
        }
        return response
    } catch (e: ApiException) {
        throw e
    } catch (e: HttpException) {
        val unauthorized = e.code() == 401
        val message = when (e.code()) {
            401 -> "登录已失效，请重新登录"
            403 -> "没有权限或账号已被禁用"
            404 -> "资源不存在"
            else -> e.message() ?: "服务器错误(${e.code()})"
        }
        throw ApiException(message, httpCode = e.code(), unauthorized = unauthorized)
    } catch (e: IOException) {
        throw ApiException("网络异常，请检查后端地址与网络连接")
    } catch (e: Exception) {
        throw ApiException(e.message ?: "未知错误")
    }
}
