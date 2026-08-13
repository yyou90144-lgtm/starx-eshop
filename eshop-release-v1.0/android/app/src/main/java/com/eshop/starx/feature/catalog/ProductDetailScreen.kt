package com.eshop.starx.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eshop.starx.core.ui.formatPrice
import com.eshop.starx.core.ui.formatSpecs
import com.eshop.starx.core.ui.theme.StarxPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    hasToken: Boolean,
    onBack: () -> Unit,
    onNeedLogin: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.factory(productId)),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error
        if (!text.isNullOrBlank()) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(state.needLogin) {
        if (state.needLogin) {
            onNeedLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商品详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.product != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { viewModel.changeQuantity(-1) }) { Text("−") }
                        Text(
                            text = "${state.quantity}",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold,
                        )
                        TextButton(onClick = { viewModel.changeQuantity(1) }) { Text("+") }
                    }
                    Button(
                        onClick = { viewModel.addToCart(hasToken) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.adding,
                    ) {
                        if (state.adding) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("加入购物车")
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = StarxPrimary)
                }
            }

            state.product == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error ?: "商品不存在")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = viewModel::load) { Text("重试") }
                    }
                }
            }

            else -> {
                val product = state.product!!
                val selected = product.skus.find { it.id == state.selectedSkuId }
                val structuredDetail = remember(product.detail) { parseProductDetail(product.detail) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProductImage(
                        contentDescription = product.name,
                        mainImage = product.mainImage,
                        productName = product.name,
                        categoryId = product.categoryId,
                        productId = product.id,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!product.subtitle.isNullOrBlank()) {
                        Text(
                            text = product.subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = formatPrice(selected?.price),
                        color = StarxPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!product.categoryName.isNullOrBlank()) {
                        Text("分类：${product.categoryName}")
                    }
                    Text(
                        text = "选择规格",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    product.skus.forEach { sku ->
                        val enabled = sku.stock > 0 && sku.status != "DISABLED"
                        FilterChip(
                            selected = state.selectedSkuId == sku.id,
                            onClick = { if (enabled) viewModel.selectSku(sku) },
                            enabled = enabled,
                            label = {
                                Text(
                                    "${formatSpecs(sku.specsJson)} · ${formatPrice(sku.price)} · 库存${sku.stock}",
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StarxPrimary.copy(alpha = 0.12f),
                                selectedLabelColor = StarxPrimary,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                    if (structuredDetail != null) {
                        Text(
                            text = "商品详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        structuredDetail.highlights
                            .filter { it.isNotBlank() }
                            .forEach { highlight ->
                                Text(
                                    text = highlight,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        structuredDetail.paragraphs
                            .filter { it.isNotBlank() }
                            .forEach { paragraph ->
                                Text(
                                    text = paragraph,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        structuredDetail.specs
                            .filter { it.label.isNotBlank() && it.value.isNotBlank() }
                            .forEach { spec ->
                                Text(
                                    text = "${spec.label}: ${spec.value}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                    } else if (!product.detail.isNullOrBlank()) {
                        Text(
                            text = "商品详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = product.detail,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}
