<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { addCartItem } from '../../api/cart'
import { getProduct } from '../../api/catalog'
import { recordBrowseHistory } from '../../api/browseHistory'
import { addFavorite, getFavoriteStatus, removeFavorite } from '../../api/favorite'
import ProductReviewList from '../../components/review/ProductReviewList.vue'
import { useAuthStore } from '../../stores/auth'
import { notifyCartUpdated } from '../../utils/cartBadge'
import { productImageSrc, restoreProductImage } from '../../utils/productVisual'
import { isPlaceholderImage, resolveProductDetailContent } from '../../utils/productDetailContent'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref(null)
const selectedSkuId = ref()
const selectedAttrs = ref({})
const quantity = ref(1)
const loading = ref(false)
const adding = ref(false)
const imageFailed = ref(false)
const activeImageIndex = ref(0)
const errorMessage = ref('')
const favorited = ref(false)
const favoriteLoading = ref(false)
let requestSequence = 0
let favoriteRequestSequence = 0

const COLOR_SWATCH = {
  暗影黑: '#2b2b2e',
  祥云白: '#f3f3f3',
  阳光橙: '#e8883a',
  海风蓝: '#7eb8d8',
  天际蓝: '#4d7fb8',
  太空银: '#c9ced6',
  星辰灰: '#6b7078',
  碳晶黑: '#1a1a1a',
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

const selectedSku = computed(() => product.value?.skus.find((sku) => sku.id === selectedSkuId.value) || null)
const maxQuantity = computed(() => Math.min(selectedSku.value?.stock || 0, 99))
const canAddToCart = computed(() => Boolean(selectedSku.value && selectedSku.value.stock > 0))
const displayName = (name = '') => name.replace(/^演示分类-/, '').replace(/^演示\s*/, '').replace(/^演示/, '')
const formatMoney = (value) => `¥${Number(value || 0).toFixed(2)}`
const activeColor = computed(() => selectedAttrs.value?.颜色 || selectedAttrs.value?.外观 || '')
const detailContent = computed(() => resolveProductDetailContent(product.value || {}, { color: activeColor.value }))
const galleryImages = computed(() => detailContent.value.gallery || [])
const activeImage = computed(() => galleryImages.value[activeImageIndex.value] || productImageSrc(product.value || {}))

watch(activeColor, () => {
  activeImageIndex.value = 0
  imageFailed.value = false
})

const selectGalleryImage = (index) => {
  activeImageIndex.value = index
  imageFailed.value = false
}

const onMainImageError = (event) => {
  if (isPlaceholderImage(activeImage.value)) {
    imageFailed.value = true
    return
  }
  if (event?.currentTarget?.dataset?.fallbackApplied === 'true') {
    imageFailed.value = true
    return
  }
  restoreProductImage(event, product.value)
}

const parseSpecs = (specsJson) => {
  try {
    return Object.entries(JSON.parse(specsJson)).map(([key, value]) => ({ key, value }))
  } catch {
    return [{ key: '规格', value: specsJson }]
  }
}
const parseSpecsMap = (specsJson) => {
  try {
    return JSON.parse(specsJson) || {}
  } catch {
    return {}
  }
}
const skuLabel = (sku) => parseSpecs(sku.specsJson).map((item) => item.value).join(' / ')

const skuDimensions = computed(() => {
  const skus = product.value?.skus || []
  if (!skus.length) return []
  const keys = new Set()
  skus.forEach((sku) => Object.keys(parseSpecsMap(sku.specsJson)).forEach((k) => keys.add(k)))
  const ordered = [
    ...DIM_ORDER.filter((k) => keys.has(k)),
    ...[...keys].filter((k) => !DIM_ORDER.includes(k)),
  ]
  return ordered.map((key) => {
    const values = []
    skus.forEach((sku) => {
      const v = parseSpecsMap(sku.specsJson)[key]
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
  // 多维度或同维度多选项时用京东式分行选择
  return dims.length >= 2 || dims.some((d) => d.options.length >= 2)
})

const isOptionAvailable = (key, value) => {
  const skus = product.value?.skus || []
  const attrs = { ...selectedAttrs.value, [key]: value }
  return skus.some((sku) => {
    if (sku.stock <= 0) return false
    const map = parseSpecsMap(sku.specsJson)
    return Object.entries(attrs).every(([k, v]) => !v || map[k] === v)
  })
}

const findSkuByAttrs = (attrs) => {
  const skus = product.value?.skus || []
  const exact = skus.find((sku) => {
    const map = parseSpecsMap(sku.specsJson)
    return Object.entries(attrs).every(([k, v]) => map[k] === v)
  })
  if (exact) return exact
  // fallback: match as many attrs as possible, prefer in-stock
  const scored = skus.map((sku) => {
    const map = parseSpecsMap(sku.specsJson)
    let score = 0
    Object.entries(attrs).forEach(([k, v]) => {
      if (map[k] === v) score += 1
    })
    if (sku.stock > 0) score += 0.1
    return { sku, score }
  }).sort((a, b) => b.score - a.score)
  return scored[0]?.sku || null
}

const selectAttr = (key, value) => {
  const next = { ...selectedAttrs.value, [key]: value }
  // if combination unavailable, keep this dim and find best sku that has this value
  let sku = findSkuByAttrs(next)
  if (!sku || sku.stock <= 0) {
    const partial = { [key]: value }
    sku = findSkuByAttrs(partial)
    if (sku) {
      selectedAttrs.value = { ...parseSpecsMap(sku.specsJson) }
      selectedSkuId.value = sku.id
      quantity.value = 1
      if (key === '颜色' || key === '外观') {
        activeImageIndex.value = 0
        imageFailed.value = false
      }
      return
    }
  }
  if (sku) {
    selectedAttrs.value = { ...parseSpecsMap(sku.specsJson) }
    selectedSkuId.value = sku.id
    quantity.value = 1
    if (key === '颜色' || key === '外观') {
      activeImageIndex.value = 0
      imageFailed.value = false
    }
  }
}

const colorSwatchStyle = (name) => {
  const c = COLOR_SWATCH[name] || '#ccc'
  return {
    background: c,
    borderColor: c === '#f3f3f3' || c === '#f7f7f7' || c === '#f5f5f5' || c === '#f2ebe0' ? '#d0d0d0' : c,
  }
}

const initSkuSelection = (data) => {
  const first = data.skus?.find((sku) => sku.stock > 0) || data.skus?.[0]
  selectedSkuId.value = first?.id
  selectedAttrs.value = first ? { ...parseSpecsMap(first.specsJson) } : {}
}

const recordBrowseQuietly = async (productId) => {
  try {
    await recordBrowseHistory(productId)
  } catch {
    // 浏览记录不能阻断商品浏览。
  }
}

const loadFavoriteStatus = async (productId) => {
  const requestId = ++favoriteRequestSequence
  try {
    const data = await getFavoriteStatus(productId)
    if (requestId === favoriteRequestSequence && product.value?.id === productId) favorited.value = Boolean(data?.favorited)
  } catch {
    // 收藏状态失败时保持默认未收藏状态。
  }
}

const loadProduct = async () => {
  const requestId = ++requestSequence
  loading.value = true
  errorMessage.value = ''
  imageFailed.value = false
  activeImageIndex.value = 0
  product.value = null
  favorited.value = false
  favoriteLoading.value = false
  favoriteRequestSequence += 1
  try {
    const id = Number(route.params.id)
    if (!Number.isInteger(id) || id <= 0) throw new Error('商品编号不正确')
    const data = await getProduct(id)
    if (requestId !== requestSequence) return
    product.value = data
    initSkuSelection(data)
    quantity.value = 1
    activeImageIndex.value = 0
    if (auth.isLoggedIn) {
      void loadFavoriteStatus(data.id)
      void recordBrowseQuietly(data.id)
    }
  } catch (error) {
    if (requestId === requestSequence) errorMessage.value = error.message || '商品详情加载失败'
  } finally {
    if (requestId === requestSequence) loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!auth.isLoggedIn) {
    ElMessage.info('请先登录后再收藏')
    await router.push({ name: 'pc-login', query: { redirect: route.fullPath } })
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
      ElMessage.success('已收藏')
    } else {
      await removeFavorite(productId)
      ElMessage.success('已取消收藏')
    }
  } catch (error) {
    if (product.value?.id === productId) favorited.value = previous
    ElMessage.error(error.message || '收藏操作失败')
  } finally {
    if (product.value?.id === productId) favoriteLoading.value = false
  }
}

const selectSku = (sku) => {
  if (sku.stock <= 0) return
  selectedSkuId.value = sku.id
  selectedAttrs.value = { ...parseSpecsMap(sku.specsJson) }
  quantity.value = 1
}

const addToCart = async () => {
  if (!auth.isLoggedIn) {
    ElMessage.info('请先登录后再加入购物车')
    await router.push({ name: 'pc-login', query: { redirect: route.fullPath } })
    return
  }
  if (!canAddToCart.value) {
    ElMessage.warning('当前规格暂时无货')
    return
  }
  adding.value = true
  try {
    const item = await addCartItem({ skuId: selectedSku.value.id, quantity: quantity.value })
    notifyCartUpdated()
    ElMessage.success(`已加入购物车，当前数量 ${item.quantity}`)
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  } finally {
    adding.value = false
  }
}

const goCart = () => router.push({ name: 'pc-cart' })
watch(() => route.params.id, loadProduct, { immediate: true })
</script>

<template>
  <div class="detail-page">
    <el-breadcrumb separator="/" class="detail-breadcrumb">
      <el-breadcrumb-item :to="{ name: 'pc-home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ name: 'pc-products' }">全部商品</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product">{{ displayName(product.categoryName) }}</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product">{{ displayName(product.name) }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false">
      <template #default><el-button link type="primary" @click="router.push({ name: 'pc-products' })">返回商品列表</el-button></template>
    </el-alert>

    <section v-if="loading" class="detail-skeleton" aria-label="正在加载商品详情">
      <div></div><article><span></span><span></span><span></span><span></span></article>
    </section>

    <!-- 京东式：左侧图库+图文可滚动，右侧购买信息吸顶固定 -->
    <section v-else-if="product" class="product-layout">
      <div class="product-main">
        <div class="gallery-panel">
          <div class="gallery-stage">
            <div
              v-if="galleryImages.length > 1"
              class="thumb-rail"
              role="listbox"
              aria-label="商品图片列表"
            >
              <button
                v-for="(image, index) in galleryImages"
                :key="`${image}-${index}`"
                type="button"
                class="thumb-item"
                :class="{ active: activeImageIndex === index, placeholder: isPlaceholderImage(image) }"
                :aria-label="`查看第 ${index + 1} 张图片`"
                :aria-selected="activeImageIndex === index"
                @click="selectGalleryImage(index)"
                @mouseenter="selectGalleryImage(index)"
              >
                <img :src="image" :alt="`${product.name} 图 ${index + 1}`" loading="lazy" />
              </button>
            </div>
            <div class="main-image">
              <img
                v-if="!imageFailed"
                :src="activeImage"
                :alt="product.name"
                @error="onMainImageError"
              />
              <div v-else class="image-placeholder">STARX</div>
              <span v-if="galleryImages.length" class="image-counter">
                {{ activeImageIndex + 1 }} / {{ galleryImages.length }}
              </span>
            </div>
          </div>
          <p v-if="galleryImages.length > 1">悬停或点击左侧缩略图切换 · 共 {{ galleryImages.length }} 张</p>
        </div>

        <section class="detail-content">
          <article>
            <h2>商品介绍</h2>
            <ul v-if="detailContent.highlights.length" class="highlight-list">
              <li v-for="item in detailContent.highlights" :key="item">{{ item }}</li>
            </ul>
            <p v-for="(paragraph, index) in detailContent.paragraphs" :key="`p-${index}`">
              {{ paragraph }}
            </p>
            <div v-if="detailContent.specs.length" class="spec-table">
              <div v-for="item in detailContent.specs" :key="item.label" class="spec-row">
                <span>{{ item.label }}</span>
                <b>{{ item.value }}</b>
              </div>
            </div>
          </article>
          <aside class="buy-notes">
            <h2>购买说明</h2>
            <p><b>库存校验</b>下单时会再次确认 SKU 库存。</p>
            <p><b>订单追踪</b>支付、发货、收货状态可在订单中心查看。</p>
            <p><b>评价参考</b>完成收货后可查看和提交商品评价。</p>
          </aside>
        </section>

        <!-- 小米式无缝图文长图：独立全宽条，避免卡片边框造成拼缝 -->
        <section
          v-if="detailContent.detailImages.length"
          class="detail-longform"
          aria-label="图文详情"
        >
          <header class="detail-longform-head">
            <h2>图文详情</h2>
            <p>向下滑动查看完整介绍</p>
          </header>
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

        <ProductReviewList :product-id="product.id" />
      </div>

      <aside class="product-info product-info--sticky" aria-label="购买信息">
        <div class="product-heading">
          <div class="heading-row">
            <span class="category-label">{{ displayName(product.categoryName) || '精选商品' }}</span>
            <el-button :type="favorited ? 'danger' : 'default'" :plain="!favorited" :loading="favoriteLoading" @click="toggleFavorite">
              {{ favorited ? '已收藏' : '收藏商品' }}
            </el-button>
          </div>
          <h1>{{ displayName(product.name) }}</h1>
          <p>{{ product.subtitle || '查看商品规格、价格和库存信息' }}</p>
          <div v-if="detailContent.learningNote || detailContent.sourceNote" class="learning-note">
            <span v-if="detailContent.learningNote">{{ detailContent.learningNote }}</span>
            <small v-if="detailContent.sourceNote">{{ detailContent.sourceNote }}</small>
          </div>
        </div>

        <div class="price-panel">
          <div><span>当前价格</span><strong>{{ formatMoney(selectedSku?.price) }}</strong></div>
          <small v-if="selectedSku">已选规格库存 {{ selectedSku.stock }} 件</small>
        </div>

        <div class="option-block">
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
            <div class="option-heading"><span>选择规格</span><small>{{ product.skus.length }} 个可选规格</small></div>
            <div v-if="product.skus.length" class="sku-list">
              <button v-for="sku in product.skus" :key="sku.id" type="button" class="sku-option" :class="{ active: selectedSkuId === sku.id, disabled: sku.stock <= 0 }" @click="selectSku(sku)">
                <b>{{ skuLabel(sku) }}</b>
                <small>{{ sku.stock > 0 ? `${formatMoney(sku.price)}，库存 ${sku.stock} 件` : '暂时无货' }}</small>
              </button>
            </div>
            <el-empty v-else description="该商品暂无可用规格" :image-size="70" />
          </template>
        </div>

        <dl v-if="selectedSku" class="selected-specs">
          <div v-for="item in parseSpecs(selectedSku.specsJson)" :key="item.key">
            <dt>{{ DIM_LABEL[item.key] || item.key }}</dt>
            <dd>{{ item.value }}</dd>
          </div>
        </dl>

        <div class="purchase-row">
          <label for="purchase-quantity">购买数量</label>
          <el-input-number id="purchase-quantity" v-model="quantity" :min="1" :max="Math.max(maxQuantity, 1)" :disabled="!canAddToCart" />
        </div>
        <div class="purchase-actions">
          <el-button type="primary" size="large" :loading="adding" :disabled="!canAddToCart" @click="addToCart">
            {{ canAddToCart ? '加入购物车' : '暂时无货' }}
          </el-button>
          <el-button size="large" :disabled="!canAddToCart" @click="goCart">去购物车结算</el-button>
        </div>
        <p class="purchase-hint">加购后可在购物车确认数量，并在结算页选择地址和完成支付。</p>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.detail-page { width: min(1240px, calc(100% - 40px)); margin: 0 auto; padding: 28px 0 80px; }
.detail-breadcrumb { margin-bottom: 26px; }
.detail-skeleton { display: grid; grid-template-columns: minmax(320px, 1.2fr) minmax(300px, .8fr); gap: 24px; min-height: 500px; padding: 28px; background: #fff; border: 1px solid var(--eshop-border); border-radius: var(--eshop-radius); }
.detail-skeleton > div, .detail-skeleton span { display: block; background: linear-gradient(100deg, #f0efec 25%, #fafaf8 37%, #f0efec 63%); background-size: 400% 100%; animation: skeleton-shimmer 1.3s ease infinite; }
.detail-skeleton > div { aspect-ratio: 1; border-radius: 12px; }
.detail-skeleton article { display: grid; align-content: start; gap: 20px; padding-top: 20px; }
.detail-skeleton span { height: 20px; border-radius: 5px; }
.detail-skeleton span:nth-child(2) { width: 65%; height: 52px; }
.detail-skeleton span:nth-child(3) { width: 92%; height: 110px; }
.detail-skeleton span:nth-child(4) { width: 44%; height: 44px; }
@keyframes skeleton-shimmer { to { background-position: -135% 0; } }
/* 左：图库+详情；右：购买信息固定吸顶（对齐京东详情） */
.product-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 400px);
  gap: 24px;
  align-items: start;
}
.product-main { min-width: 0; display: grid; gap: 22px; }
.gallery-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: var(--eshop-radius);
}
/* 京东式：左侧竖向缩略图 + 右侧大图 */
.gallery-stage {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
}
.thumb-rail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: min(520px, 100%);
  overflow-y: auto;
  padding: 1px;
  scrollbar-width: thin;
  scrollbar-color: #d8d5cf transparent;
}
.thumb-item {
  flex: 0 0 auto;
  width: 60px;
  height: 60px;
  padding: 3px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: 8px;
  cursor: pointer;
  transition: border-color .15s ease, box-shadow .15s ease;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #fff;
  border-radius: 4px;
}
.thumb-item:hover { border-color: #f3a4a4; }
.thumb-item.active {
  border-color: var(--eshop-primary);
  box-shadow: 0 0 0 1px var(--eshop-primary);
}
.thumb-item.placeholder { opacity: 0.9; }
.main-image {
  position: relative;
  display: grid;
  width: 100%;
  aspect-ratio: 1;
  place-items: center;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: 10px;
}
.main-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center center;
  background: #fff;
}
.image-placeholder { color: var(--eshop-muted); font-size: 22px; font-weight: 800; }
.image-counter {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 4px 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.42);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  pointer-events: none;
}
.gallery-panel > p { margin: 10px 0 0; color: var(--eshop-muted); font-size: 12px; line-height: 1.6; }
@media (max-width: 800px) {
  .gallery-stage { grid-template-columns: 56px minmax(0, 1fr); gap: 8px; }
  .thumb-item { width: 52px; height: 52px; }
  .thumb-rail { max-height: 360px; }
}
/* 右侧购买栏：滚动时吸顶固定（对齐京东） */
.product-info {
  min-width: 0;
  padding: 20px 18px 18px;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: var(--eshop-radius);
}
.product-info--sticky {
  position: sticky;
  /* 避开顶栏：logo 区 + 导航（约 120~140px） */
  top: calc(var(--eshop-header-height, 108px) + 28px);
  z-index: 20;
  /* 禁止 overflow 裁切，否则 background-clip:text 标题会被挡住 */
  overflow: visible;
  max-height: none;
  box-shadow: 0 8px 28px rgba(40, 36, 30, 0.06);
  isolation: isolate;
}
.heading-row { display: flex; align-items: center; justify-content: space-between; gap: 14px; }
.heading-row :deep(.el-button) { border-radius: 8px; font-weight: 700; }
.category-label { color: var(--eshop-primary); font-size: 12px; font-weight: 800; }
/* 覆盖全局 spectrum 透明字：购买栏标题用实色，避免被裁切/看不清 */
.product-info .product-heading h1 {
  margin: 13px 0 8px;
  color: var(--eshop-text) !important;
  font-size: 26px;
  line-height: 1.35;
  background: none !important;
  background-clip: border-box !important;
  -webkit-background-clip: border-box !important;
  -webkit-text-fill-color: var(--eshop-text) !important;
  overflow: visible;
  word-break: break-word;
}
.product-heading > p { margin: 0; color: var(--eshop-muted); font-size: 13px; line-height: 1.65; }
.price-panel { display: flex; align-items: flex-end; justify-content: space-between; gap: 14px; margin: 18px 0; padding: 14px 16px; background: var(--eshop-primary-soft); border-radius: 8px; }
.price-panel div { display: grid; gap: 5px; }
.price-panel span { color: var(--eshop-muted); font-size: 12px; }
.price-panel strong { color: var(--eshop-primary); font-size: 28px; }
.price-panel small { color: var(--eshop-text); font-size: 12px; font-weight: 700; }
.option-block { padding: 16px 0; border-top: 1px solid var(--eshop-border); }
.option-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.option-heading span, .purchase-row label { color: var(--eshop-text); font-size: 14px; font-weight: 800; }
.option-heading small { color: var(--eshop-muted); font-size: 12px; }
/* 京东式：外观 / 存储容量 分行选择 */
.spec-row-select {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}
.spec-row-select:last-child { margin-bottom: 0; }
.spec-row-label {
  color: var(--eshop-muted);
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
  color: var(--eshop-text);
  font-size: 13px;
  font-weight: 600;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: 6px;
  cursor: pointer;
  transition: border-color .15s ease, background .15s ease, color .15s ease;
}
.spec-chip:hover:not(.disabled) {
  border-color: #f3a4a4;
}
.spec-chip.active {
  color: var(--eshop-primary);
  border-color: var(--eshop-primary);
  background: var(--eshop-primary-soft);
  box-shadow: 0 0 0 1px var(--eshop-primary);
}
.spec-chip.disabled {
  color: #b0aea8;
  cursor: not-allowed;
  opacity: 0.55;
  text-decoration: line-through;
}
.spec-chip.is-color {
  padding-left: 10px;
}
.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  flex: 0 0 auto;
}
.sku-list { display: flex; flex-wrap: wrap; gap: 9px; }
.sku-option { display: grid; min-width: 120px; gap: 4px; padding: 10px 12px; text-align: left; color: var(--eshop-text); background: #fff; border: 1px solid var(--eshop-border); border-radius: 9px; cursor: pointer; }
.sku-option:hover, .sku-option.active { border-color: var(--eshop-primary); background: var(--eshop-primary-soft); }
.sku-option.disabled { cursor: not-allowed; opacity: .45; }
.sku-option b { font-size: 13px; }
.sku-option small { color: var(--eshop-muted); font-size: 11px; }
.selected-specs { display: flex; flex-wrap: wrap; gap: 0; margin: 0; padding: 0 0 16px; border-bottom: 1px solid var(--eshop-border); }
.selected-specs div { display: flex; gap: 7px; padding: 0 16px 0 0; }
.selected-specs dt, .selected-specs dd { margin: 0; font-size: 12px; }
.selected-specs dt { color: var(--eshop-muted); }
.selected-specs dd { color: var(--eshop-text); font-weight: 750; }
.purchase-row { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; padding-top: 16px; }
.purchase-row label { margin-right: 4px; }
.purchase-row :deep(.el-input-number) { width: 112px; }
.purchase-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 14px; }
.purchase-actions :deep(.el-button) { min-height: 44px; width: 100%; border-radius: 8px; font-weight: 800; }
.purchase-hint { margin: 12px 0 0; color: var(--eshop-muted); font-size: 12px; line-height: 1.6; }
.learning-note {
  display: grid;
  gap: 4px;
  margin-top: 10px;
  padding: 8px 10px;
  border: 1px dashed #f0c2c2;
  border-radius: 8px;
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
.detail-content {
  display: grid;
  gap: 18px;
  padding: 22px 20px;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: var(--eshop-radius);
}
.detail-content h2 { margin: 0 0 12px; color: var(--eshop-text); font-size: 20px; }
.detail-content h3 { margin: 22px 0 12px; color: var(--eshop-text); font-size: 16px; }
.detail-content article > p { max-width: 64ch; margin: 0 0 12px; color: var(--eshop-text-soft); font-size: 14px; line-height: 1.85; }
.highlight-list { display: flex; flex-wrap: wrap; gap: 8px; margin: 0 0 16px; padding: 0; list-style: none; }
.highlight-list li { padding: 6px 10px; color: var(--eshop-primary); background: var(--eshop-primary-soft); border-radius: 999px; font-size: 12px; font-weight: 700; }
.spec-table { display: grid; gap: 0; margin: 8px 0 18px; border: 1px solid var(--eshop-border); border-radius: 8px; overflow: hidden; }
.spec-row { display: grid; grid-template-columns: 110px 1fr; gap: 12px; padding: 10px 14px; border-top: 1px solid var(--eshop-border); font-size: 13px; }
.spec-row:first-child { border-top: 0; }
.spec-row span { color: var(--eshop-muted); }
.spec-row b { color: var(--eshop-text); font-weight: 700; }
.detail-longform {
  margin-top: 8px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: var(--eshop-radius);
}
.detail-longform-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid var(--eshop-border);
}
.detail-longform-head h2 {
  margin: 0;
  color: var(--eshop-text);
  font-size: 18px;
}
.detail-longform-head p {
  margin: 0;
  color: var(--eshop-muted);
  font-size: 12px;
}
.detail-longform-strip {
  display: block;
  margin: 0;
  padding: 0;
  line-height: 0;
  background: #fff;
  font-size: 0;
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
.buy-notes {
  padding: 16px;
  background: var(--eshop-surface-soft);
  border-radius: 8px;
}
.buy-notes h2 { margin: 0 0 10px; font-size: 15px; }
.buy-notes p { margin: 0 0 10px; color: var(--eshop-muted); font-size: 13px; line-height: 1.65; }
.buy-notes p:last-child { margin-bottom: 0; }
.buy-notes b { display: block; color: var(--eshop-text); }
@media (max-width: 980px) {
  .product-layout { grid-template-columns: 1fr; }
  .product-info--sticky {
    position: static;
    max-height: none;
    order: -1;
  }
}
@media (max-width: 800px) {
  .detail-page { width: min(100% - 28px, 600px); padding-top: 16px; }
  .detail-skeleton { grid-template-columns: 1fr; gap: 26px; padding: 16px; min-height: 600px; }
  .product-heading h1 { font-size: 24px; }
  .purchase-actions { grid-template-columns: 1fr; }
}
@media (prefers-reduced-motion: reduce) { .detail-skeleton > div, .detail-skeleton span { animation: none; } }
</style>
