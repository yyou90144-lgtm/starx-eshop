package com.eshop.starx.feature.catalog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eshop.starx.R
import com.eshop.starx.core.ui.StarxProductImage
import com.eshop.starx.core.ui.formatPrice
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxSurface
import com.eshop.starx.data.model.ProductSummaryDto

val catalogThumbnailResources = listOf(
    R.drawable.catalog_phone,
    R.drawable.catalog_headphones,
    R.drawable.catalog_laptop,
    R.drawable.catalog_keyboard,
    R.drawable.catalog_lamp,
    R.drawable.catalog_home,
)

private val ProductCardShape = RoundedCornerShape(10.dp)
private val ProductCardImageShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)

/**
 * 商品主图以后端 mainImage 为准；本地资源只用于加载失败或滚动缓存未命中时兜底。
 */
@Composable
fun ProductCard(
    product: ProductSummaryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scrolling: Boolean = false,
) {
    val priceText = remember(product.minPrice) { formatPrice(product.minPrice) }
    val imageRes = remember(product.name, product.categoryId, product.id) {
        catalogImageResource(product.name, product.categoryId, product.id)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StarxSurface, ProductCardShape)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = ProductCardShape,
            )
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(ProductCardImageShape),
        ) {
            StarxProductImage(
                path = product.mainImage,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                listMode = true,
                scrolling = scrolling,
                fallbackImageRes = imageRes,
            )
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.subtitle.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                minLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = priceText,
                color = StarxPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun ProductImage(
    contentDescription: String?,
    mainImage: String?,
    productName: String,
    categoryId: Long?,
    productId: Long,
    modifier: Modifier = Modifier,
) {
    val imageRes = remember(productName, categoryId, productId) {
        catalogImageResource(productName, categoryId, productId)
    }
    StarxProductImage(
        path = mainImage,
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxWidth()
            .height(272.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        fallbackImageRes = imageRes,
    )
}

@DrawableRes
fun catalogImageResource(productName: String, categoryId: Long?, productId: Long): Int {
    val normalizedName = productName.trim()
    return when {
        "手机" in normalizedName -> R.drawable.catalog_phone
        "音箱" in normalizedName || "耳机" in normalizedName -> R.drawable.catalog_headphones
        "键盘" in normalizedName || "鼠标" in normalizedName -> R.drawable.catalog_keyboard
        "笔记本" in normalizedName || "平板" in normalizedName || "显示器" in normalizedName -> {
            R.drawable.catalog_laptop
        }
        "台灯" in normalizedName || "灯" in normalizedName -> R.drawable.catalog_lamp
        categoryId == 1L -> R.drawable.catalog_phone
        categoryId == 2L -> R.drawable.catalog_laptop
        categoryId == 3L -> R.drawable.catalog_home
        else -> when ((productId % 6L).toInt()) {
        0 -> R.drawable.catalog_phone
        1 -> R.drawable.catalog_headphones
        2 -> R.drawable.catalog_laptop
        3 -> R.drawable.catalog_keyboard
        4 -> R.drawable.catalog_lamp
        else -> R.drawable.catalog_home
        }
    }
}
