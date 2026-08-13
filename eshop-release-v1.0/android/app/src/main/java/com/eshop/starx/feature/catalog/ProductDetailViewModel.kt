package com.eshop.starx.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eshop.starx.core.network.ApiException
import com.eshop.starx.data.model.ProductDetailDto
import com.eshop.starx.data.model.SkuDto
import com.eshop.starx.data.repository.CartRepository
import com.eshop.starx.data.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val loading: Boolean = true,
    val adding: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val product: ProductDetailDto? = null,
    val selectedSkuId: Long? = null,
    val quantity: Int = 1,
    val needLogin: Boolean = false,
)

class ProductDetailViewModel(
    private val productId: Long,
    private val catalogRepository: CatalogRepository = CatalogRepository(),
    private val cartRepository: CartRepository = CartRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, needLogin = false) }
            try {
                val product = catalogRepository.product(productId)
                val firstAvailable = product.skus.firstOrNull { it.stock > 0 && it.status != "DISABLED" }
                    ?: product.skus.firstOrNull()
                _uiState.update {
                    it.copy(
                        loading = false,
                        product = product,
                        selectedSkuId = firstAvailable?.id,
                        quantity = 1,
                    )
                }
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun selectSku(sku: SkuDto) {
        _uiState.update { it.copy(selectedSkuId = sku.id, quantity = 1, message = null) }
    }

    fun changeQuantity(delta: Int) {
        _uiState.update { state ->
            val sku = state.product?.skus?.find { it.id == state.selectedSkuId }
            val max = (sku?.stock ?: 1).coerceAtLeast(1).coerceAtMost(99)
            val next = (state.quantity + delta).coerceIn(1, max)
            state.copy(quantity = next)
        }
    }

    fun addToCart(hasToken: Boolean) {
        if (!hasToken) {
            _uiState.update { it.copy(needLogin = true, message = "请先登录后再加购") }
            return
        }
        val state = _uiState.value
        val skuId = state.selectedSkuId
        if (skuId == null) {
            _uiState.update { it.copy(message = "请选择规格") }
            return
        }
        val sku = state.product?.skus?.find { it.id == skuId }
        if (sku == null || sku.stock <= 0) {
            _uiState.update { it.copy(message = "该规格暂无库存") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(adding = true, message = null, error = null, needLogin = false) }
            try {
                cartRepository.addItem(skuId, state.quantity)
                _uiState.update { it.copy(adding = false, message = "已加入购物车") }
            } catch (e: ApiException) {
                val needLogin = e.unauthorized || e.httpCode == 401
                _uiState.update {
                    it.copy(
                        adding = false,
                        error = e.message,
                        needLogin = needLogin,
                        message = if (needLogin) "请先登录后再加购" else null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(adding = false, error = e.message ?: "加购失败")
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    companion object {
        fun factory(productId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductDetailViewModel(productId) as T
                }
            }
    }
}
