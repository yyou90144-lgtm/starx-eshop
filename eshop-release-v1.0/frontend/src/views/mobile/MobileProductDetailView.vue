<script setup>
import { computed, defineAsyncComponent, onMounted, onUnmounted, ref, watch } from 'vue'
import { showImagePreview, showSuccessToast, showToast } from 'vant'
import { useRoute, useRouter } from 'vue-router'
import { addCartItem } from '../../api/cart'
import { getProduct } from '../../api/catalog'
import { recordBrowseHistory } from '../../api/browseHistory'
import { addFavorite, getFavoriteStatus, removeFavorite } from '../../api/favorite'
import { useAuthStore } from '../../stores/auth'
import { notifyCartUpdated } from '../../utils/cartBadge'
import { formatMoney } from '../../utils/shop'
import { productImageSrc, restoreProductImage } from '../../utils/productVisual'
import { isPlaceholderImage, resolveProductDetailContent } from '../../utils/productDetailContent'
import { buildSkuSpecIndex, specsEntries, specsLabel } from '../../utils/skuSpecs'

const ProductReviewList = defineAsyncComponent(() => import('../../components/review/ProductReviewList.vue'))

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const product = ref(null)
const selectedSkuId = ref(null)
const selectedAttrs = ref({})
const quantity = ref(1)
const loading = ref(false)
const adding = ref(false)
const imageFailed = ref(false)
const errorMessage = ref('')
const favorited = ref(false)
const favoriteLoading = ref(false)
const swipeKey = ref(0)
const reviewsVisible = ref(false)
const reviewsSentinel = ref(null)
let favoriteRequestSequence = 0
let requestSequence = 0
let reviewsObserver = null

const COLOR_SWATCH = {
  暗影黑: '#2b2b2e',
  天际蓝: '#4d7fb8',
  太空银: '#c9ced6',
  星辰灰: '#6b7078',
  碳晶黑: '#1a1a1a',
  祥云白: '#f3f3f3',
  阳光橙: '#e8883a',
  海风蓝: '#7eb8d8',
  深空黑: '#1f1f21',
  星河银: '#c8cdd3',
  云朵白: '#f7f7f7',
  曜石黑: '#171717',
  晴空蓝: '#5ba3d9',
  白色: '#f5f5f5',
  黑色: '#222',
  灰色: '#8a8a8a',
  米白: '#f2ebe0',
  墨绿色: '#2f5d4a',
  银色: '#d0d4d8',
}

const DIM_LABEL = {
  颜色: '外观',
  外观: '外观',
  存储容量: '存储容量',
  容量: '存储容量',
  处理器: '处理器',
  内存: '内存',
  硬盘: '硬盘',
}

const DIM_ORDER = ['颜色', '外观', '处理器', '存储容量', '容量', '内存', '硬盘']

const selectedSku = computed(() => {
  if (!product.value || selectedSkuId.value === null) return null
  return (product.value.skus || []).find((sku) => sku.id === selectedSkuId.value) || null
})

const maxQuantity = computed(() => Math.min(selectedSku.value?.stock || 0, 99))

const canAddToCart = computed(() => (
  Boolean(selectedSku.value && selectedSku.value.stock > 0)
  && quantity.value >= 1
  && quantity.value <= maxQuantity.value
))

const activeColor = computed(() => selectedAttrs.value?.颜色 || selectedAttrs.value?.外观 || '')

const detailContent = computed(() => resolveProductDetailContent(product.value || {}, { color: activeColor.value }))

const galleryImages = computed(() => {
  if (!product.value) return []
  const list = detailContent.value.gallery || []
  if (list.length) return list
  const src = productImageSrc(product.value)
  return src ? [src] : []
})

watch(activeColor, () => {
  swipeKey.value += 1
  imageFailed.value = false
})

const skuSpecIndex = computed(() => buildSkuSpecIndex(product.value?.skus))
const specsOf = (sku) => (sku ? (skuSpecIndex.value.get(sku.id) || {}) : {})
const skuLabel = (sku) => specsLabel(specsOf(sku))

const skuDimensions = computed(() => {
  const skus = product.value?.skus || []
  if (!skus.length) return []
  const index = skuSpecIndex.value
  const keys = new Set()
  skus.forEach((sku) => Object.keys(index.get(sku.id) || {}).forEach((k) => keys.add(k)))
  const ordered = [
    ...DIM_ORDER.filter((k) => keys.has(k)),
    ...[...keys].filter((k) => !DIM_ORDER.includes(k)),
  ]
  return ordered.map((key) => {
    const values = []
    skus.forEach((sku) => {
      const v = (index.get(sku.id) || {})[key]
      if (v && !values.includes(v)) values.push(v)
    })
    return {
      key,
      label: DIM_LABEL[key] || key,
      options: values,
      isColor: key === '颜色' || key === '外观',
    }
  }).filter((d) => d.options.length > 0)
})

const useGroupedSpecs = computed(() => {
  const dims = skuDimensions.value
  return dims.length >= 2 || dims.some((d) => d.options.length >= 2)
})

const isOptionAvailable = (key, value) => {
  const skus = product.value?.skus || []
  const index = skuSpecIndex.value
  const attrs = { ...selectedAttrs.value, [key]: value }
  return skus.some((sku) => {
    if (sku.stock <= 0) return false
    const map = index.get(sku.id) || {}
    return Object.entries(attrs).every(([k, v]) => !v || map[k] === v)
  })
}

const findSkuByAttrs = (attrs) => {
  const skus = product.value?.skus || []
  const index = skuSpecIndex.value
  const exact = skus.find((sku) => {
    const map = index.get(sku.id) || {}
    return Object.entries(attrs).every(([k, v]) => map[k] === v)
  })
  if (exact) return exact
  const scored = skus.map((sku) => {
    const map = index.get(sku.id) || {}
    let score = 0
    Object.entries(attrs).forEach(([k, v]) => {
      if (map[k] === v) score += 1
    })
    if (sku.stock > 0) score += 0.1
    return { sku, score }
  }).sort((a, b) => b.score - a.score)
  return scored[0]?.sku || null
}

const colorSwatchStyle = (name) => {
  const c = COLOR_SWATCH[name] || '#ccc'
  return {
    background: c,
    boxShadow: c === '#f3f3f3' || c === '#f7f7f7' || c === '#f5f5f5' || c === '#f2ebe0'
      ? 'inset 0 0 0 1px rgba(0,0,0,.12)'
      : 'none',
  }
}

const selectAttr = (key, value) => {
  const next = { ...selectedAttrs.value, [key]: value }
  let sku = findSkuByAttrs(next)
  if (!sku || sku.stock <= 0) {
    const partial = { [key]: value }
    sku = findSkuByAttrs(partial)
    if (sku) {
      selectedAttrs.value = { ...specsOf(sku) }
      selectedSkuId.value = sku.id
      quantity.value = 1
      return
    }
    showToast('该规格暂时无货')
    return
  }
  selectedAttrs.value = { ...specsOf(sku) }
  selectedSkuId.value = sku.id
  quantity.value = 1
}

const selectSku = (sku) => {
  if (!sku || sku.stock <= 0) {
    showToast('该规格暂时无货')
    return
  }
  selectedSkuId.value = sku.id
  selectedAttrs.value = { ...specsOf(sku) }
  quantity.value = Math.max(1, Math.min(quantity.value, Math.min(sku.stock, 99)))
}

const onGalleryImageError = (event) => {
  const src = event?.currentTarget?.getAttribute('src') || ''
  if (isPlaceholderImage(src)) return
  if (event?.currentTarget?.dataset?.fallbackApplied === 'true') {
    imageFailed.value = true
    return
  }
  restoreProductImage(event, product.value)
}

const loadProduct = async () => {
  const requestId = ++requestSequence
  loading.value = true
  errorMessage.value = ''
  imageFailed.value = false
  product.value = null
  selectedSkuId.value = null
  selectedAttrs.value = {}
  quantity.value = 1
  favorited.value = false
  favoriteLoading.value = false
  favoriteRequestSequence += 1
  swipeKey.value += 1

  try {
    const id = Number(route.params.id)
    if (!Number.isInteger(id) || id <= 0) throw new Error('商品编号不正确')
    const data = await getProduct(id)
    if (requestId !== requestSequence) return
    product.value = data
    const skus = data.skus || []
    const firstAvailable = skus.find((sku) => sku.stock > 0) || skus[0]
    selectedSkuId.value = firstAvailable ? firstAvailable.id : null
    const index = buildSkuSpecIndex(skus)
    selectedAttrs.value = firstAvailable ? { ...(index.get(firstAvailable.id) || {}) } : {}
    if (auth.isLoggedIn) {
      void loadFavoriteStatus(data.id)
      void recordBrowseQuietly(data.id)
    }
  } catch (error) {
    if (requestId === requestSequence) {
      errorMessage.value = error.message || '商品详情加载失败'
    }
  } finally {
    if (requestId === requestSequence) loading.value = false
  }
}

const recordBrowseQuietly = async (productId) => {
  try {
    await recordBrowseHistory(productId)
  } catch {
    // 浏览历史失败不打断商品浏览。
  }
}

const loadFavoriteStatus = async (productId) => {
  const requestId = ++favoriteRequestSequence
  try {
    const data = await getFavoriteStatus(productId)
    if (requestId === favoriteRequestSequence && product.value?.id === productId) {
      favorited.value = Boolean(data?.favorited)
    }
  } catch {
    // 收藏状态失败不影响商品浏览
  }
}

const toggleFavorite = async () => {
  if (!auth.isLoggedIn) {
    showToast('请先登录后再收藏')
    await router.push({
      name: 'mobile-login',
      query: { redirect: route.fullPath },
    })
    return
  }
  const productId = product.value?.id
  if (!productId || favoriteLoading.value) return

  const previous = favorited.value
  favoriteLoading.value = true
  favorited.value = !previous
  try {
    if (favorited.value) {
      await addFavorite(productId)
      showSuccessToast('已收藏')
    } else {
      await removeFavorite(productId)
      showSuccessToast('已取消收藏')
    }
  } catch (error) {
    if (product.value?.id === productId) favorited.value = previous
    showToast({ type: 'fail', message: error.message || '收藏操作失败' })
  } finally {
    if (product.value?.id === productId) favoriteLoading.value = false
  }
}

const onQuantityChange = (value) => {
  if (value < 1) {
    quantity.value = 1
    return
  }
  if (value > maxQuantity.value) {
    quantity.value = maxQuantity.value
    showToast(`最多只能购买 ${maxQuantity.value} 件`)
  }
}

const previewImage = (index) => {
  if (!galleryImages.value.length) return
  showImagePreview({
    images: galleryImages.value,
    startPosition: index,
    closeable: true,
  })
}

const addToCart = async () => {
  if (!auth.isLoggedIn) {
    showToast('请先登录后再加入购物车')
    await router.push({
      name: 'mobile-login',
      query: { redirect: route.fullPath },
    })
    return
  }
  if (!selectedSku.value) {
    showToast('请先选择商品规格')
    return
  }
  if (selectedSku.value.stock <= 0) {
    showToast('当前规格暂时无货')
    return
  }
  if (quantity.value < 1 || quantity.value > selectedSku.value.stock) {
    showToast(`购买数量必须在 1 至 ${selectedSku.value.stock} 之间`)
    return
  }

  adding.value = true
  try {
    const item = await addCartItem({
      skuId: selectedSku.value.id,
      quantity: quantity.value,
    })
    notifyCartUpdated()
    showSuccessToast({
      message: `已加入购物车，当前数量 ${item.quantity}`,
      duration: 2200,
      position: 'top',
      className: 'eshop-mobile-toast eshop-mobile-toast--success',
      wordBreak: 'break-word',
      closeOnClick: true,
    })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '加入购物车失败' })
  } finally {
    adding.value = false
  }
}

const goCart = () => {
  router.push({ name: 'mobile-cart' })
}

const setupReviewsObserver = () => {
  reviewsObserver?.disconnect()
  reviewsObserver = null
  if (reviewsVisible.value) return
  const el = reviewsSentinel.value
  if (!el || typeof IntersectionObserver === 'undefined') {
    reviewsVisible.value = true
    return
  }
  reviewsObserver = new IntersectionObserver((entries) => {
    if (entries.some((entry) => entry.isIntersecting)) {
      reviewsVisible.value = true
      reviewsObserver?.disconnect()
      reviewsObserver = null
    }
  }, { rootMargin: '240px 0px' })
  reviewsObserver.observe(el)
}

watch(() => route.params.id, () => {
  reviewsVisible.value = false
  void loadProduct()
}, { immediate: true })

watch(reviewsSentinel, () => {
  setupReviewsObserver()
})

onMounted(() => {
  setupReviewsObserver()
})

onUnmounted(() => {
  reviewsObserver?.disconnect()
  reviewsObserver = null
})
</script>

<template>
  <section class="mobile-detail">
    <div v-if="errorMessage" class="state-block">
      <van-empty :description="errorMessage" image="error">
        <van-button round type="primary" size="small" @click="loadProduct">
          重新加载
        </van-button>
      </van-empty>
    </div>

    <template v-else-if="product">
      <div v-if="galleryImages.length" class="gallery">
        <van-swipe
          :key="swipeKey"
          :autoplay="0"
          indicator-color="#e5484d"
          lazy-render
        >
          <van-swipe-item
            v-for="(image, index) in galleryImages"
            :key="`${image}-${index}`"
            @click="previewImage(index)"
          >
            <img
              :src="image"
              :alt="`${product.name} 图 ${index + 1}`"
              @error="onGalleryImageError"
            />
          </van-swipe-item>
        </van-swipe>
        <span v-if="galleryImages.length > 1" class="image-counter">
          {{ galleryImages.length }} 张
        </span>
        <button
          type="button"
          class="favorite-button"
          :class="{ active: favorited }"
          :disabled="favoriteLoading"
          :aria-label="favorited ? '取消收藏' : '收藏商品'"
          @click.stop="toggleFavorite"
        >
          <van-loading v-if="favoriteLoading" size="18" color="#ee0a24" />
          <van-icon v-else :name="favorited ? 'like' : 'like-o'" size="22" />
          <span>{{ favorited ? '已收藏' : '收藏' }}</span>
        </button>
      </div>
      <div v-else class="gallery placeholder">
        <button
          type="button"
          class="favorite-button favorite-button--placeholder"
          :class="{ active: favorited }"
          :disabled="favoriteLoading"
          :aria-label="favorited ? '取消收藏' : '收藏商品'"
          @click.stop="toggleFavorite"
        >
          <van-loading v-if="favoriteLoading" size="18" color="#ee0a24" />
          <van-icon v-else :name="favorited ? 'like' : 'like-o'" size="22" />
          <span>{{ favorited ? '已收藏' : '收藏' }}</span>
        </button>
        暂无商品图片
      </div>

      <div class="card card--hero">
        <div class="price-panel">
          <strong>{{ formatMoney(selectedSku?.price) }}</strong>
          <small v-if="selectedSku">库存 {{ selectedSku.stock }} 件</small>
        </div>
        <h1>{{ product.name }}</h1>
        <p>{{ product.subtitle || '品质商品，放心选购' }}</p>
        <div v-if="detailContent.learningNote || detailContent.sourceNote" class="learning-note">
          <span v-if="detailContent.learningNote">{{ detailContent.learningNote }}</span>
          <small v-if="detailContent.sourceNote">{{ detailContent.sourceNote }}</small>
        </div>
        <van-tag v-if="product.categoryName" type="primary" plain>
          {{ product.categoryName }}
        </van-tag>
      </div>

      <div class="card">
        <template v-if="useGroupedSpecs && skuDimensions.length">
          <div
            v-for="dim in skuDimensions"
            :key="dim.key"
            class="spec-row-select"
          >
            <div class="spec-row-label">{{ dim.label }}</div>
            <div class="spec-row-options">
              <button
                v-for="opt in dim.options"
                :key="`${dim.key}-${opt}`"
                type="button"
                class="spec-chip"
                :class="{
                  active: selectedAttrs[dim.key] === opt,
                  disabled: !isOptionAvailable(dim.key, opt),
                  'is-color': dim.isColor,
                }"
                :disabled="!isOptionAvailable(dim.key, opt) && selectedAttrs[dim.key] !== opt"
                @click="selectAttr(dim.key, opt)"
              >
                <span
                  v-if="dim.isColor"
                  class="color-dot"
                  :style="colorSwatchStyle(opt)"
                  aria-hidden="true"
                />
                <span>{{ opt }}</span>
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="card-heading">
            <span>选择规格</span>
            <small v-if="!product.skus?.length">暂无规格</small>
          </div>
          <div v-if="product.skus?.length" class="sku-list">
            <button
              v-for="sku in product.skus"
              :key="sku.id"
              type="button"
              class="sku-option"
              :class="{ active: selectedSkuId === sku.id, disabled: sku.stock <= 0 }"
              :disabled="sku.stock <= 0"
              @click="selectSku(sku)"
            >
              <span>{{ skuLabel(sku) || '默认规格' }}</span>
              <small>
                {{ sku.stock > 0
                  ? `${formatMoney(sku.price)} · 库存 ${sku.stock}`
                  : '暂时无货' }}
              </small>
            </button>
          </div>
        </template>
        <div v-if="selectedSku" class="selected-specs">
          <span v-for="item in specsEntries(specsOf(selectedSku))" :key="item.key">
            <b>{{ DIM_LABEL[item.key] || item.key }}</b>{{ item.value }}
          </span>
        </div>
      </div>

      <div class="card">
        <div class="card-heading">
          <span>购买数量</span>
          <small v-if="selectedSku">最多 {{ maxQuantity }} 件</small>
        </div>
        <div class="quantity-row">
          <van-stepper
            v-model="quantity"
            :min="1"
            :max="Math.max(maxQuantity, 1)"
            :disabled="!selectedSku || selectedSku.stock <= 0"
            integer
            @change="onQuantityChange"
          />
          <span v-if="!selectedSku">请先选择规格</span>
          <span v-else-if="selectedSku.stock <= 0">暂时无货</span>
        </div>
      </div>

      <div class="card">
        <div class="card-heading"><span>商品详情</span></div>
        <ul v-if="detailContent.highlights.length" class="highlight-list">
          <li v-for="item in detailContent.highlights" :key="item">{{ item }}</li>
        </ul>
        <p v-for="(paragraph, index) in detailContent.paragraphs" :key="`p-${index}`" class="detail-text">
          {{ paragraph }}
        </p>
        <div v-if="detailContent.specs.length" class="spec-table">
          <div v-for="item in detailContent.specs" :key="item.label" class="spec-row">
            <span>{{ item.label }}</span>
            <b>{{ item.value }}</b>
          </div>
        </div>
      </div>

      <section
        v-if="detailContent.detailImages.length"
        class="detail-longform"
        aria-label="图文详情"
      >
        <div class="detail-longform-head">
          <span>图文详情</span>
          <small>向下滑动查看完整介绍</small>
        </div>
        <div class="detail-longform-strip">
          <img
            v-for="(image, index) in detailContent.detailImages"
            :key="`${image}-${index}`"
            :src="image"
            :alt="`${product.name} 详情 ${index + 1}`"
            loading="lazy"
            decoding="async"
          />
        </div>
      </section>

      <div ref="reviewsSentinel">
        <ProductReviewList v-if="reviewsVisible" :product-id="product.id" :page-size="5" />
      </div>

      <div class="service-row">
        <span>模拟支付</span>
        <span>库存实时校验</span>
        <span>订单可追踪</span>
      </div>

      <div class="action-bar">
        <van-button
          type="warning"
          plain
          hairline
          icon="cart-o"
          class="cart-button"
          @click="goCart"
        >
          购物车
        </van-button>
        <van-button
          type="danger"
          class="add-button"
          :loading="adding"
          :disabled="!canAddToCart"
          @click="addToCart"
        >
          {{ canAddToCart ? '加入购物车' : (selectedSku ? '暂时无货' : '请选择规格') }}
        </van-button>
      </div>
    </template>

    <div v-else class="state-block">
      <van-loading color="#e5484d" size="32" />
      <p>商品加载中…</p>
    </div>
  </section>
</template>

<style scoped>
.mobile-detail {
  min-height: 100%;
  padding: 0 0 96px;
  background: #f5f5f2;
}

.state-block {
  display: grid;
  min-height: 60vh;
  place-items: center;
  color: #64748b;
  padding: 12px;
}

.state-block p {
  margin-top: 12px;
}

.gallery {
  position: relative;
  overflow: hidden;
  background: #fff;
  border-bottom: 1px solid #e8e6e1;
}

.gallery :deep(.van-swipe) {
  aspect-ratio: 1;
  background: #fff;
}

.gallery :deep(.van-swipe-item) {
  display: grid;
  place-items: center;
  background: #fff;
}

.gallery img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center center;
  background: #fff;
  display: block;
}

.image-counter {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 2;
  padding: 3px 10px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  background: rgba(0, 0, 0, 0.42);
  border-radius: 999px;
  pointer-events: none;
}

.gallery.placeholder {
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  color: #746f68;
  font-size: 13px;
  background: #fafaf8;
}

.favorite-button {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 12px;
  color: #45433f;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid #e2e1dd;
  border-radius: 999px;
  box-shadow: 0 4px 14px rgba(40, 36, 30, 0.08);
}

.favorite-button.active {
  color: #e5484d;
}

.favorite-button:disabled {
  opacity: 0.72;
}

.favorite-button span {
  font-size: 12px;
  font-weight: 600;
}

.favorite-button--placeholder {
  position: static;
  margin: 0 auto 12px;
}

.card {
  margin: 10px 12px 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #e2e1dd;
  border-radius: 12px;
}

.card--hero {
  margin-top: 0;
  border-radius: 0 0 12px 12px;
  border-top: 0;
}

.learning-note {
  display: grid;
  gap: 4px;
  margin: 8px 0 10px;
  padding: 8px 10px;
  border: 1px dashed #f0c2c2;
  border-radius: 10px;
  background: #fff7f7;
  color: #9a3b3b;
  font-size: 12px;
  line-height: 1.5;
}

.learning-note small {
  color: #a86464;
  font-size: 11px;
  word-break: break-all;
}

.price-panel {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #fff5f4;
  border-radius: 8px;
}

.price-panel strong {
  color: #c83b40;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.price-panel small,
.card-heading small {
  color: #746f68;
  font-size: 12px;
}

.card h1 {
  margin: 0 0 6px;
  color: #252422;
  font-size: 18px;
  line-height: 1.4;
  font-weight: 800;
}

.card > p {
  margin: 0 0 8px;
  color: #746f68;
  font-size: 13px;
  line-height: 1.55;
}

.card-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #252422;
  font-size: 14px;
  font-weight: 700;
}

/* 京东式：外观 / 存储容量 分行 */
.spec-row-select {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}

.spec-row-select:last-of-type {
  margin-bottom: 0;
}

.spec-row-label {
  color: #8a8780;
  font-size: 13px;
  font-weight: 700;
}

.spec-row-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 6px 12px;
  color: #2b2a27;
  font-size: 13px;
  font-weight: 600;
  background: #fff;
  border: 1px solid #d6d4cf;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}

.spec-chip.active {
  color: #c83b40;
  border-color: #e5484d;
  background: #fff1f0;
  box-shadow: 0 0 0 1px rgba(229, 72, 77, 0.18);
}

.spec-chip.disabled {
  color: #b0aea8;
  opacity: 0.55;
  text-decoration: line-through;
  cursor: not-allowed;
}

.spec-chip.is-color {
  padding-left: 10px;
}

.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  flex: 0 0 auto;
}

.sku-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.sku-option {
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  color: #45433f;
  text-align: left;
  background: #fff;
  border: 1px solid #d6d4cf;
  border-radius: 8px;
}

.sku-option.active {
  color: #c83b40;
  background: #fff1f0;
  border-color: #e5484d;
  box-shadow: 0 0 0 1px rgba(229, 72, 77, 0.16);
}

.sku-option.disabled {
  color: #928d85;
  background: #fafaf8;
}

.sku-option small {
  color: #746f68;
  font-size: 11px;
}

.selected-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0eeea;
}

.selected-specs span {
  display: flex;
  gap: 4px;
  padding: 4px 8px;
  color: #45433f;
  background: #fafaf8;
  border-radius: 6px;
  font-size: 11px;
}

.selected-specs b {
  color: #c83b40;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #746f68;
  font-size: 12px;
}

.card .detail-text {
  margin: 0 0 10px;
  color: #45433f;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.highlight-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 12px;
  padding: 0;
  list-style: none;
}

.highlight-list li {
  padding: 5px 10px;
  color: #c62828;
  background: #fff1f0;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.spec-table {
  display: grid;
  margin: 4px 0 12px;
  border: 1px solid #e2e1dd;
  border-radius: 8px;
  overflow: hidden;
}

.spec-row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 10px;
  padding: 10px 12px;
  border-top: 1px solid #e2e1dd;
  font-size: 13px;
}

.spec-row:first-child {
  border-top: 0;
}

.spec-row span {
  color: #8a8780;
}

.spec-row b {
  color: #2b2a27;
  font-weight: 700;
}

.detail-longform {
  margin: 12px 0 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e2e1dd;
  border-radius: 14px;
}

.detail-longform-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #eee;
}

.detail-longform-head span {
  color: #2b2a27;
  font-size: 14px;
  font-weight: 800;
}

.detail-longform-head small {
  color: #8a8780;
  font-size: 12px;
}

.detail-longform-strip {
  margin: 0;
  padding: 0;
  line-height: 0;
  font-size: 0;
  background: #fff;
}

.detail-longform-strip img {
  display: block;
  width: 100%;
  height: auto;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  vertical-align: top;
  object-fit: fill;
  background: #fff;
}

.service-row {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 14px;
  margin: 16px 12px 8px;
  color: #8a8780;
  font-size: 11px;
}

.action-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  gap: 8px;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid #e2e1dd;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.cart-button {
  flex: 0 0 96px;
}

.add-button {
  flex: 1;
  font-weight: 700;
}
</style>
