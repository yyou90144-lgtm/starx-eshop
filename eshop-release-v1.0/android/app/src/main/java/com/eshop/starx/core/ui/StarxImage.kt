package com.eshop.starx.core.ui

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.eshop.starx.core.network.NetworkModule
import com.eshop.starx.core.ui.theme.StarxSurfaceSoft

/**
 * Product-grid thumbnails are decoded off the main thread at a fixed size.
 * The source images are bundled with the app, so disk and network caches add no value here.
 */
@Composable
fun StarxCatalogThumbnail(
    @DrawableRes imageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val request = remember(imageRes) {
        catalogThumbnailRequest(context, imageRes)
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

/**
 * Warm the six bundled thumbnails before the user reaches a new category.
 * This keeps first-use WebP decoding off the scroll path.
 */
@Composable
fun PreloadCatalogThumbnails(@DrawableRes imageResources: List<Int>) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    val resources = remember(imageResources) { imageResources.toList() }

    LaunchedEffect(imageLoader, resources) {
        resources.forEach { imageRes ->
            imageLoader.enqueue(catalogThumbnailRequest(context, imageRes))
        }
    }
}

private fun catalogThumbnailRequest(context: android.content.Context, @DrawableRes imageRes: Int): ImageRequest =
    ImageRequest.Builder(context)
        .data(imageRes)
        .size(512)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .crossfade(false)
        .allowHardware(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .networkCachePolicy(CachePolicy.DISABLED)
        .build()

/**
 * 商品图。
 *
 * listMode：小图 + RGB_565 + 无 crossfade。
 * scrolling=true：列表 fling 时只读内存缓存，禁止磁盘/网络解码（避免 SVG 解码掉帧）。
 */
@Composable
fun StarxProductImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    listMode: Boolean = false,
    scrolling: Boolean = false,
    seedLabel: String? = null,
    @DrawableRes fallbackImageRes: Int? = null,
) {
    val context = LocalContext.current
    val url = remember(path) { NetworkModule.resolveMediaUrl(path) }
    val initial = remember(seedLabel, contentDescription) {
        (seedLabel ?: contentDescription ?: "选")
            .trim()
            .firstOrNull()
            ?.toString()
            ?: "选"
    }

    val request = remember(url, listMode, scrolling) {
        if (url.isNullOrBlank()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(url)
                .apply {
                    if (listMode) {
                        // A two-column card is about 450 physical pixels wide on the target phone.
                        // Keep enough resolution and color depth to avoid visible upscaling artifacts.
                        size(512)
                        bitmapConfig(Bitmap.Config.ARGB_8888)
                        crossfade(false)
                        allowHardware(false)
                        if (scrolling) {
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.DISABLED)
                            networkCachePolicy(CachePolicy.DISABLED)
                        } else {
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.ENABLED)
                            networkCachePolicy(CachePolicy.ENABLED)
                        }
                    } else {
                        size(900)
                        crossfade(100)
                        allowHardware(true)
                        memoryCachePolicy(CachePolicy.ENABLED)
                        diskCachePolicy(CachePolicy.ENABLED)
                        networkCachePolicy(CachePolicy.ENABLED)
                    }
                }
                .build()
        }
    }

    Box(
        modifier = modifier.background(StarxSurfaceSoft),
        contentAlignment = Alignment.Center,
    ) {
        if (fallbackImageRes != null) {
            Image(
                painter = painterResource(fallbackImageRes),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }

        if (listMode && fallbackImageRes == null) {
            Text(
                text = initial,
                color = Color(0xFFE5484D).copy(alpha = 0.50f),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        } else if (!listMode && fallbackImageRes == null) {
            Text("暂无图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
