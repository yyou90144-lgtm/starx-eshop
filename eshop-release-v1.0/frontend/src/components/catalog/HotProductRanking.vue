<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHotProducts, getProducts } from '../../api/catalog'
import { formatMoney } from '../../utils/shop'

const props = defineProps({
  mobile: {
    type: Boolean,
    default: false,
  },
  days: {
    type: Number,
    default: 30,
  },
  limit: {
    type: Number,
    default: 5,
  },
  showHeader: {
    type: Boolean,
    default: true,
  },
})

const router = useRouter()
const loading = ref(false)
const products = ref([])
const errorMessage = ref('')
const usingFallback = ref(false)

const title = computed(() => (
  usingFallback.value ? '人气推荐' : `近 ${props.days} 天热销榜`
))

const subtitle = computed(() => (
  usingFallback.value
    ? '暂无有效订单销量，先为你推荐在售好物'
    : '按已支付/已发货/已完成订单销量排序'
))

const displayName = (name = '') => name
  .replace(/^演示分类-/, '')
  .replace(/^演示\s*/, '')
  .replace(/^演示/, '')

const numericValue = (...values) => {
  for (const value of values) {
    const number = Number(value)
    if (Number.isFinite(number)) return number
  }
  return 0
}

const fallbackImage = (product) => {
  const name = `${product?.name || ''} ${product?.subtitle || ''}`
  if (/耳机|音箱/.test(name)) return '/images/catalog/headphones-studio.jpg'
  if (/手机|数码/.test(name)) return '/images/catalog/phone-studio.jpg'
  if (/键盘|鼠标/.test(name)) return '/images/catalog/keyboard-studio.jpg'
  if (/电脑|笔记本|显示器|硬盘|路由|打印|摄像头/.test(name)) return '/images/catalog/laptop-studio.jpg'
  if (/台灯|家居|电饭煲|电水壶|吸尘|空气|体脂|插线板|风扇/.test(name)) return '/images/catalog/lamp-studio.jpg'
  return '/images/catalog/home-studio.jpg'
}

const imageSource = (product) => {
  const main = product?.mainImage
  if (main && !/\/api\/demo\/(?:phone|computer|home)\.svg$/i.test(String(main))) {
    return main
  }
  return fallbackImage(product)
}

const onImageError = (product) => {
  const fallback = fallbackImage(product)
  if (product.mainImage === fallback) {
    product.imageFailed = true
    return
  }
  product.mainImage = fallback
}

const loadFallback = async () => {
  const page = await getProducts({ current: 1, size: props.limit })
  const records = Array.isArray(page?.records) ? page.records : []
  usingFallback.value = true
  products.value = records.map((item, index) => ({
    productId: item.id,
    name: displayName(item.name),
    subtitle: item.subtitle,
    mainImage: item.mainImage,
    salesQuantity: Math.max(3, (props.limit - index) * 7),
    minPrice: item.minPrice,
    totalStock: item.totalStock,
    imageFailed: false,
    fallback: true,
  }))
}

const load = async () => {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  usingFallback.value = false
  try {
    const list = await getHotProducts({ days: props.days, limit: props.limit })
    const ranked = (Array.isArray(list) ? list : []).map((item) => ({
      ...item,
      productId: item.productId ?? item.id,
      name: displayName(item.name || item.productName),
      salesQuantity: numericValue(item.salesQuantity, item.soldQuantity, item.quantity),
      minPrice: numericValue(item.minPrice, item.price),
      imageFailed: false,
      fallback: false,
    }))
    if (ranked.length) {
      products.value = ranked
      return
    }
    await loadFallback()
  } catch (error) {
    try {
      await loadFallback()
      errorMessage.value = ''
    } catch {
      products.value = []
      errorMessage.value = error.message || '热销榜加载失败'
    }
  } finally {
    loading.value = false
  }
}

const openProduct = (id) => {
  router.push({
    name: props.mobile ? 'mobile-product-detail' : 'pc-product-detail',
    params: { id },
  })
}

onMounted(load)
</script>

<template>
  <section class="hot-ranking" :class="{ mobile, fallback: usingFallback }">
    <header v-if="showHeader">
      <div>
        <h2>{{ title }}</h2>
      </div>
      <button v-if="errorMessage" type="button" @click="load">重新加载</button>
      <small v-else>{{ subtitle }}</small>
    </header>

    <div v-if="loading && !products.length" class="loading-list">
      <span v-for="index in Math.min(limit, 5)" :key="index" />
    </div>

    <div v-else-if="products.length" class="ranking-list">
      <button
        v-for="(product, index) in products"
        :key="product.productId"
        type="button"
        class="ranking-card"
        @click="openProduct(product.productId)"
      >
        <b :class="{ champion: index === 0 }">{{ index + 1 }}</b>
        <span class="product-image">
          <img
            v-if="!product.imageFailed"
            :src="imageSource(product)"
            :alt="product.name"
            loading="lazy"
            @error="onImageError(product)"
          />
          <span v-else>STARX</span>
        </span>
        <span class="product-info">
          <strong>{{ product.name }}</strong>
          <small>
            {{ product.fallback ? `人气值 ${product.salesQuantity}` : `已售 ${product.salesQuantity} 件` }}
          </small>
          <em>{{ formatMoney(product.minPrice) }}</em>
        </span>
      </button>
    </div>

    <div v-else class="empty-box">
      <p>{{ errorMessage || '暂时还没有热销数据' }}</p>
      <button type="button" @click="load">刷新试试</button>
    </div>
  </section>
</template>

<style scoped>
.hot-ranking {
  margin: 20px 0 8px;
}

.hot-ranking header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.hot-ranking h2 {
  margin: 0;
  color: var(--eshop-text);
  font-size: 24px;
}

.hot-ranking header small {
  color: var(--eshop-muted);
  max-width: 300px;
  text-align: right;
  line-height: 1.4;
}

.hot-ranking header button,
.empty-box button {
  padding: 6px 12px;
  color: #ff4d4f;
  background: #fff1f0;
  border: 1px solid #ffd6d2;
  border-radius: 10px;
  cursor: pointer;
}

.ranking-list,
.loading-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.ranking-card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 10px;
  padding: 12px;
  text-align: left;
  background: #fff;
  border: 1px solid var(--eshop-border);
  border-radius: 12px;
  cursor: pointer;
  transition: .2s ease;
}

.ranking-card:hover {
  border-color: #ffb3a7;
  box-shadow: 0 12px 28px rgba(21, 46, 79, .08);
  transform: translateY(-3px);
}

.ranking-card > b {
  position: absolute;
  z-index: 1;
  top: 8px;
  left: 8px;
  display: grid;
  width: 24px;
  height: 24px;
  color: #fff;
  background: #94a3b8;
  border-radius: 6px;
  font-size: 12px;
  place-items: center;
}

.ranking-card > b.champion {
  background: #ff4d4f;
}

.product-image {
  display: grid;
  width: 100%;
  height: 120px;
  place-items: center;
  overflow: hidden;
  color: #9bb2cf;
  background: #edf3f8;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 800;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
}

.product-info strong {
  overflow: hidden;
  color: var(--eshop-text);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info small {
  margin: 4px 0;
  color: var(--eshop-muted);
  font-size: 11px;
}

.product-info em {
  color: #ff4d4f;
  font-size: 15px;
  font-style: normal;
  font-weight: 800;
}

.loading-list span {
  height: 86px;
  background: linear-gradient(100deg, #faf7f5 30%, #f0ebe6 50%, #faf7f5 70%);
  background-size: 300% 100%;
  border-radius: 12px;
  animation: pulse 1.2s infinite;
}

.empty-box {
  display: grid;
  gap: 10px;
  justify-items: start;
  padding: 8px 0 2px;
}

.empty-box p {
  margin: 0;
  color: #8a837c;
  font-size: 13px;
}

@keyframes pulse {
  to { background-position: -100% 0; }
}

@media (max-width: 1000px) {
  .ranking-list,
  .loading-list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.hot-ranking.mobile {
  margin: 8px 12px;
}

.hot-ranking.mobile header {
  align-items: center;
  margin-bottom: 12px;
}

.hot-ranking.mobile h2 {
  font-size: 16px;
}

.hot-ranking.mobile header small {
  font-size: 10px;
}

.hot-ranking.mobile .ranking-list,
.hot-ranking.mobile .loading-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
}

.hot-ranking.mobile .ranking-list::-webkit-scrollbar,
.hot-ranking.mobile .loading-list::-webkit-scrollbar {
  display: none;
}

.hot-ranking.mobile .ranking-card,
.hot-ranking.mobile .loading-list span {
  flex: 0 0 180px;
}
</style>
