package com.eshop.starx.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eshop.starx.core.ui.PreloadCatalogThumbnails
import com.eshop.starx.core.ui.formatPrice
import com.eshop.starx.core.ui.theme.StarxPrimary
import com.eshop.starx.core.ui.theme.StarxPrimarySoft
import com.eshop.starx.core.ui.theme.StarxSurface
import com.eshop.starx.data.model.CategoryDto
import com.eshop.starx.data.model.HotProductDto
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val TitleSpectrum = listOf(
    Color(0xFFE5484D),
    Color(0xFFE58C38),
    Color(0xFF70A24B),
    Color(0xFF328BB0),
    Color(0xFF7659A7),
)

private val BrandTitle = buildAnnotatedString {
    "星选商城".forEachIndexed { index, character ->
        withStyle(SpanStyle(color = TitleSpectrum[index])) {
            append(character)
        }
    }
}

@Composable
fun HomeScreen(
    onOpenProduct: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val isScrolling by remember { androidx.compose.runtime.derivedStateOf { listState.isScrollInProgress } }
    val productRows = remember(state.products) { state.products.chunked(2) }
    var keywordDraft by remember { mutableStateOf("") }

    PreloadCatalogThumbnails(catalogThumbnailResources)

    LaunchedEffect(state.selectedCategoryId, state.appliedKeyword) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            last to total
        }
            .distinctUntilChanged()
            .filter { (last, total) -> total > 0 && last >= total - 4 }
            .collect {
                val current = viewModel.uiState.value
                if (current.hasMore && !current.loadingMore && !current.loading) {
                    viewModel.loadMore()
                }
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader(
                keyword = keywordDraft,
                total = state.total,
                onKeywordChange = { keywordDraft = it },
                onClearKeyword = {
                    keywordDraft = ""
                    if (state.appliedKeyword.isNotBlank()) viewModel.search("")
                },
                onSearch = { viewModel.search(keywordDraft) },
            )
            CategoryTabs(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelectCategory = viewModel::selectCategory,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            when {
                state.loading -> ProductGridLoading()

                state.error != null && state.products.isEmpty() -> {
                    HomeMessage(
                        title = "商品加载失败",
                        detail = state.error,
                        actionLabel = "重新加载",
                        onAction = viewModel::refreshAll,
                    )
                }

                state.products.isEmpty() -> {
                    HomeMessage(
                        title = "没有找到商品",
                        detail = "换个分类或关键词试试",
                        actionLabel = "查看全部",
                        onAction = {
                            keywordDraft = ""
                            viewModel.clearFilters()
                        },
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (state.hotProducts.isNotEmpty() &&
                            state.selectedCategoryId == null &&
                            state.appliedKeyword.isBlank()
                        ) {
                            item(key = "hot", contentType = "hot") {
                                HotProducts(
                                    products = state.hotProducts.take(3),
                                    onOpenProduct = onOpenProduct,
                                )
                            }
                        }

                        item(key = "product_heading", contentType = "heading") {
                            ProductSectionHeading(
                                title = sectionTitle(state),
                                total = state.total,
                            )
                        }

                        items(
                            items = productRows,
                            key = { row -> row.first().id },
                            contentType = { "product_row" },
                        ) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                row.forEach { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = { onOpenProduct(product.id) },
                                        modifier = Modifier.weight(1f),
                                        scrolling = isScrolling,
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (state.loadingMore) {
                            item(key = "loading_more", contentType = "footer") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = StarxPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }

                        if (!state.hasMore) {
                            item(key = "end", contentType = "footer") {
                                Text(
                                    text = "已经到底了",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    keyword: String,
    total: Long,
    onKeywordChange: (String) -> Unit,
    onClearKeyword: () -> Unit,
    onSearch: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = BrandTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (total > 0) {
                Text(
                    text = "$total 件好物",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("搜索商品") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = if (keyword.isNotEmpty()) {
                {
                    IconButton(onClick = onClearKeyword) {
                        Icon(Icons.Default.Close, contentDescription = "清空搜索")
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun CategoryTabs(
    categories: List<CategoryDto>,
    selectedCategoryId: Long?,
    onSelectCategory: (Long?) -> Unit,
) {
    val options = remember(categories) { listOf<CategoryDto?>(null) + categories }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { category ->
            val selected = selectedCategoryId == category?.id
            Column(
                modifier = Modifier
                    .widthIn(min = 78.dp)
                    .height(48.dp)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelectCategory(category?.id) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = category?.name ?: "全部",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) StarxPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(2.dp)
                        .background(
                            color = if (selected) StarxPrimary else Color.Transparent,
                            shape = RoundedCornerShape(1.dp),
                        ),
                )
            }
        }
    }
}

@Composable
private fun HotProducts(
    products: List<HotProductDto>,
    onOpenProduct: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "热销精选",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            products.forEachIndexed { index, product ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .background(StarxSurface, RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { onOpenProduct(product.productId) }
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "热销 ${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = StarxPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatPrice(product.minPrice),
                        style = MaterialTheme.typography.labelLarge,
                        color = StarxPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductSectionHeading(title: String, total: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$total 件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProductGridLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(246.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeMessage(
    title: String,
    detail: String?,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (!detail.isNullOrBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

private fun sectionTitle(state: HomeUiState): String = when {
    state.appliedKeyword.isNotBlank() -> "搜索结果"
    state.selectedCategoryId != null -> {
        state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name ?: "分类商品"
    }
    else -> "全部商品"
}
