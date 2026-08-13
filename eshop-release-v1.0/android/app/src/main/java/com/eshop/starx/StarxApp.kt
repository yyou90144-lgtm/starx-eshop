package com.eshop.starx

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.eshop.starx.core.auth.TokenStore
import com.eshop.starx.core.network.AuthInterceptor
import com.eshop.starx.core.network.NetworkModule
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class StarxApp : Application(), ImageLoaderFactory {
    lateinit var tokenStore: TokenStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenStore = TokenStore(this)
        NetworkModule.init(tokenStore)
    }

    // Coil 全局加载器：支持 demo SVG + 内存/磁盘缓存
    override fun newImageLoader(): ImageLoader {
        val imageOkHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            // Coil is a separate HTTP client, so it must opt into the same JWT policy as Retrofit.
            .addInterceptor(AuthInterceptor(tokenStore))
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(imageOkHttp)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

    companion object {
        lateinit var instance: StarxApp
            private set
    }
}
