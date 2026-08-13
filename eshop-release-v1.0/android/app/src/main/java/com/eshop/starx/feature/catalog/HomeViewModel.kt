package com.eshop.starx.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshop.starx.core.network.ApiException
import com.eshop.starx.data.model.CategoryDto
import com.eshop.starx.data.model.HotProductDto
import com.eshop.starx.data.model.ProductSummaryDto
import com.eshop.starx.data.repository.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val categories: List<CategoryDto> = emptyList(),
    val selectedCategoryId: Long? = null,
    val appliedKeyword: String = "",
    val products: List<ProductSummaryDto> = emptyList(),
    val hotProducts: List<HotProductDto> = emptyList(),
    val current: Long = 1,
    val total: Long = 0,
    val hasMore: Boolean = false,
)

class HomeViewModel(
    private val repository: CatalogRepository = CatalogRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var productLoadJob: Job? = null

    init {
        refreshAll()
    }

    fun search(keywordRaw: String) {
        val keyword = keywordRaw.trim()
        _uiState.update {
            it.copy(
                appliedKeyword = keyword,
                products = emptyList(),
                current = 1,
                total = 0,
                hasMore = false,
                loading = true,
            )
        }
        loadProducts(reset = true)
    }

    fun selectCategory(categoryId: Long?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                products = emptyList(),
                current = 1,
                total = 0,
                hasMore = false,
                loading = true,
            )
        }
        loadProducts(reset = true)
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedCategoryId = null,
                appliedKeyword = "",
                products = emptyList(),
                current = 1,
                total = 0,
                hasMore = false,
                loading = true,
            )
        }
        loadProducts(reset = true)
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true, error = null) }
            try {
                val categories = repository.categories()
                val hot = runCatching { repository.hotRanking() }.getOrDefault(emptyList())
                _uiState.update {
                    it.copy(
                        categories = categories,
                        hotProducts = hot,
                        refreshing = false,
                    )
                }
                loadProducts(reset = true)
            } catch (e: ApiException) {
                _uiState.update { it.copy(refreshing = false, loading = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(refreshing = false, loading = false, error = e.message ?: "加载失败")
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.loadingMore || !state.hasMore || state.loading) return
        loadProducts(reset = false)
    }

    private fun loadProducts(reset: Boolean) {
        if (reset) {
            productLoadJob?.cancel()
        }
        val requestState = _uiState.value
        val job = viewModelScope.launch {
            val nextPage = if (reset) 1L else requestState.current + 1
            _uiState.update {
                it.copy(
                    loading = reset,
                    loadingMore = !reset,
                    error = null,
                )
            }
            try {
                val page = repository.products(
                    current = nextPage,
                    size = 20,
                    categoryId = requestState.selectedCategoryId,
                    keyword = requestState.appliedKeyword,
                )
                val latest = _uiState.value
                if (latest.selectedCategoryId != requestState.selectedCategoryId ||
                    latest.appliedKeyword != requestState.appliedKeyword
                ) {
                    return@launch
                }
                _uiState.update {
                    val merged = if (reset) page.records else it.products + page.records
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        refreshing = false,
                        products = merged,
                        current = page.current,
                        total = page.total,
                        hasMore = merged.size < page.total,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _uiState.update {
                    it.copy(loading = false, loadingMore = false, refreshing = false, error = e.message)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        refreshing = false,
                        error = e.message ?: "加载商品失败",
                    )
                }
            }
        }
        productLoadJob = job
    }
}
