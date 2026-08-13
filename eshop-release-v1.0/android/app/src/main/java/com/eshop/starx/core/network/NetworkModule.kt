package com.eshop.starx.core.network

import com.eshop.starx.BuildConfig
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.data.api.AddressApi
import com.eshop.starx.data.api.AuthApi
import com.eshop.starx.data.api.CartApi
import com.eshop.starx.data.api.CatalogApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * 简易服务定位：P2 不用 Hilt，集中提供 Retrofit / API。
 */
object NetworkModule {
    private const val PRODUCT_IMAGE_REVISION = "20260813-coverfix"
    private lateinit var tokenStore: TokenStore

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY 会在真机上严重拖慢；仅 debug 打 HEADERS
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        val baseUrl = BuildConfig.API_BASE_URL.let { url ->
            if (url.endsWith("/")) url else "$url/"
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val catalogApi: CatalogApi by lazy { retrofit.create(CatalogApi::class.java) }
    val cartApi: CartApi by lazy { retrofit.create(CartApi::class.java) }
    val addressApi: AddressApi by lazy { retrofit.create(AddressApi::class.java) }

    fun init(store: TokenStore) {
        tokenStore = store
    }

    fun apiBaseUrl(): String = BuildConfig.API_BASE_URL

    /**
     * 将相对资源路径拼成可访问 URL。
     *
     * 兼容：
     * - `/api/demo/phone.svg`（种子图，已含 /api 前缀）
     * - `/uploads/a.png` 或 `uploads/a.png`
     * - 完整 http(s) URL
     */
    fun resolveMediaUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val trimmed = path.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed

        val api = apiBaseUrl().trimEnd('/')
        // api = http://host:8080/api
        val origin = if (api.endsWith("/api")) api.removeSuffix("/api") else api

        val resolved = when {
            trimmed.startsWith("/api/") || trimmed == "/api" -> origin + trimmed
            trimmed.startsWith("/") -> origin + trimmed
            else -> "$origin/$trimmed"
        }

        // Product assets are replaced in place by the shared backend. Version only this static
        // catalog path so a device cannot retain an outdated product photo after an asset refresh.
        return if (trimmed.startsWith("/api/demo/products/")) {
            "$resolved?rev=$PRODUCT_IMAGE_REVISION"
        } else {
            resolved
        }
    }
}
