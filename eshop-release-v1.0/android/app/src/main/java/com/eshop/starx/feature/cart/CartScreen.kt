package com.eshop.starx.feature.cart

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eshop.starx.core.network.NetworkModule
import com.eshop.starx.core.ui.theme.StarxBorder
import com.eshop.starx.core.ui.theme.StarxInk
import com.eshop.starx.core.ui.theme.StarxMuted
import com.eshop.starx.core.ui.theme.StarxPage
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxSurface
import com.eshop.starx.data.model.CartItemDto
import com.eshop.starx.data.repository.CartRepository
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CartScreen(
    hasToken: Boolean,
    onNeedLogin: () -> Unit,
    refreshKey: Int = 0,
) {
    val repo = remember { CartRepository() }
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<CartItemDto>>(emptyList()) }

    fun reload() {
        if (!hasToken) return
        scope.launch {
            loading = true
            error = null
            try {
                items = repo.list()
            } catch (e: Exception) {
                error = e.message ?: "加载失败"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(hasToken, refreshKey) {
        if (hasToken) reload() else {
            items = emptyList()
            error = null
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text("购物车", style = MaterialTheme.typography.headlineMedium, color = StarxInk)
        Spacer(Modifier.height(12.dp))

        when {
            !hasToken -> {
                Text("登录后查看购物车", color = StarxMuted)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onNeedLogin) { Text("去登录") }
            }
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StarxPrimary)
            }
            error != null -> Column {
                Text(error ?: "", color = StarxPrimary)
                TextButton(onClick = { reload() }) { Text("重试") }
            }
            items.isEmpty() -> Text("购物车还是空的，去首页加购吧", color = StarxMuted)
            else -> {
                val total = items.sumOf { it.subtotal ?: ((it.price ?: 0.0) * it.quantity) }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        CartRow(
                            item = item,
                            onMinus = {
                                scope.launch {
                                    try {
                                        if (item.quantity <= 1) {
                                            repo.remove(item.id)
                                        } else {
                                            repo.updateQuantity(item.id, item.quantity - 1)
                                        }
                                        reload()
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            },
                            onPlus = {
                                scope.launch {
                                    try {
                                        repo.updateQuantity(item.id, (item.quantity + 1).coerceAtMost(99))
                                        reload()
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            },
                            onRemove = {
                                scope.launch {
                                    try {
                                        repo.remove(item.id)
                                        reload()
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            },
                        )
                    }
                }
                Surface(
                    color = StarxSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarxBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("合计", color = StarxMuted, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "¥${String.format(Locale.CHINA, "%.2f", total)}",
                                color = StarxPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        Button(onClick = { /* 结算 S5 */ }, enabled = false) {
                            Text("去结算（S5）")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    item: CartItemDto,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StarxSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StarxBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val url = NetworkModule.resolveMediaUrl(item.productImage)
            AsyncImage(
                model = url,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.productName,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = StarxInk,
                )
                if (!item.specsJson.isNullOrBlank()) {
                    Text(item.specsJson!!, style = MaterialTheme.typography.bodySmall, color = StarxMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "¥${String.format(Locale.CHINA, "%.2f", item.price ?: 0.0)}",
                    color = StarxPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onMinus) { Text("−") }
                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 4.dp))
                    TextButton(onClick = onPlus) { Text("+") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onRemove) { Text("删除", color = StarxPrimary) }
                }
            }
        }
    }
}

/** 兼容旧调用名 */
@Composable
fun CartPlaceholderScreen(
    hasToken: Boolean = false,
    onNeedLogin: () -> Unit = {},
    refreshKey: Int = 0,
) {
    CartScreen(hasToken = hasToken, onNeedLogin = onNeedLogin, refreshKey = refreshKey)
}
