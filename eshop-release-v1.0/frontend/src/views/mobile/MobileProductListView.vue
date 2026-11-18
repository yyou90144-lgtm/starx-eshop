<script setup>
import { computed, onActivated, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getCategories, getProducts } from '../../api/catalog'
import HotProductRanking from '../../components/catalog/HotProductRanking.vue'
import { formatMoney } from '../../utils/shop'

defineOptions({ name: 'MobileProductListView' })

const route = useRoute()
const router = useRouter()

const categories = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const errorMessage = ref('')
const failedPage = ref(null)
const keywordInput = ref('')
const size = 10
const current = ref(1)
const finished = ref(false)

const categoryId = computed(() => {
  const value = Number(route.query.categoryId)
  return Number.isInteger(value) && value > 0 ? value : undefined
})

const keyword = computed(() => (
  typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''
))

const categoryOptions = computed(() => {
  const result = []
  const append = (list, depth = 0) => {
    ;(list || []).forEach((item) => {
      result.push({
        ...item,
        displayName: `${depth ? '—'.repeat(depth) + ' ' : ''}${item.name}`,
      })
      append(item.children, depth + 1)
    })
  }
  append(categories.value)
  return result
})

const activeCategoryName = computed(() => (
  categoryOptions.value.find((item) => item.id === categoryId.value)?.name || '全部商品'
))

let requestSequence = 0

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '商品分类加载失败' })
  }
}

const loadProducts = async ({ reset = false, page = reset ? 1 : current.value } = {}) => {
  if (loading.value && !reset) return false
  const requestId = ++requestSequence
  loading.value = true
  if (reset) errorMessage.value = ''

  try {
    const data = await getProducts({
      current: page,
      size,
      categoryId: categoryId.value,
      keyword: keyword.value || undefined,
    })
    if (requestId !== requestSequence) return false

    const list = (data.records || []).map((product) => ({ ...product, imageFailed: false }))
    records.value = reset ? list : records.value.concat(list)
    current.value = page
    total.value = data.total ?? records.value.length
    finished.value = list.length < size || records.value.length >= total.value
    failedPage.value = null
    errorMessage.value = ''
    return true
  } catch (error) {
    if (requestId !== requestSequence) return false
    errorMessage.value = error.message || '商品加载失败，请稍后重试'
    failedPage.value = page
    if (reset) records.value = []
    finished.value = true
    return false
  } finally {
    if (requestId === requestSequence) loading.value = false
  }
}

const replaceQuery = (changes) => {
  const query = { ...route.query, ...changes }
  Object.keys(query).forEach((key) => {
    if (query[key] === undefined || query[key] === null || query[key] === '') {
      delete query[key]
    }
  })
  router.push({ name: 'mobile-products', query })
}

const selectCategory = (id) => {
  replaceQuery({ categoryId: id || undefined })
}

const search = () => {
  const value = keywordInput.value.trim()
  replaceQuery({ keyword: value || undefined })
}

const resetFilters = () => {
  keywordInput.value = ''
  router.push({ name: 'mobile-products' })
}

const loadMore = () => {
  if (finished.value || loading.value || errorMessage.value) return
  loadProducts({ page: current.value + 1 })
}

const retryLoad = () => {
  const page = failedPage.value || 1
  finished.value = false
  loadProducts({ reset: page === 1, page })
}

const onRefresh = async () => {
  finished.value = false
  await loadProducts({ reset: true, page: 1 })
  refreshing.value = false
  if (!errorMessage.value) showToast('已刷新')
}

const openProduct = (id) => {
  router.push({ name: 'mobile-product-detail', params: { id } })
}

const handleImageError = (product) => {
  product.imageFailed = true
}

watch(
  () => [route.query.categoryId, route.query.keyword],
  () => {
    keywordInput.value = keyword.value
    finished.value = false
    loadProducts({ reset: true, page: 1 })
  },
)

onMounted(() => {
  keywordInput.value = keyword.value
  void Promise.all([
    loadCategories(),
    loadProducts({ reset: true, page: 1 }),
  ])
})

onActivated(() => {
  if (!records.value.length && !loading.value) {
    void loadProducts({ reset: true, page: 1 })
  }
})
</script>

<template>
  <section class="mobile-products">
    <div class="search-bar">
      <form class="m-search" @submit.prevent="search">
        <van-field
          v-model="keywordInput"
          class="m-search-field"
          clearable
          left-icon="search"
          placeholder="搜索商品、品牌或关键词"
          enterkeyhint="search"
          @clear="search"
          @keyup.enter="search"
        />
        <button class="m-search-btn" type="submit">搜索</button>
      </form>
    </div>

    <div v-if="categoryOptions.length" class="category-bar">
      <button
        type="button"
        class="category-chip"
        :class="{ active: !categoryId }"
        @click="selectCategory()"
      >
        全部
      </button>
      <button
        v-for="category in categoryOptions"
        :key="category.id"
        type="button"
        class="category-chip"
        :class="{ active: categoryId === category.id }"
        @click="selectCategory(category.id)"
      >
        {{ category.displayName }}
      </button>
    </div>

    <HotProductRanking mobile />

    <div v-if="keyword || categoryId" class="result-heading">
      <div>
        <strong>{{ activeCategoryName }}</strong>
        <span v-if="keyword"> · 关键词“{{ keyword }}”</span>
      </div>
      <button type="button" @click="resetFilters">重置筛选</button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        :immediate-check="false"
        :finished-text="records.length ? '没有更多商品了' : ''"
        :error="Boolean(errorMessage)"
        :error-text="errorMessage || '请求失败，点击重试'"
        class="product-list"
        @load="loadMore"
        @click-error-text="retryLoad"
      >
        <button
          v-for="product in records"
          :key="product.id"
          type="button"
          class="product-card"
          @click="openProduct(product.id)"
        >
          <span class="product-thumb">
            <img
              v-if="product.mainImage && !product.imageFailed"
              :src="product.mainImage"
              :alt="product.name"
              loading="lazy"
              @error="handleImageError(product)"
            />
            <span v-else class="image-placeholder">E-Shop</span>
            <span v-if="product.totalStock <= 0" class="stock-badge sold-out">暂时缺货</span>
            <span v-else class="stock-badge">库存 {{ product.totalStock }}</span>
          </span>
          <span class="product-content">
            <strong class="product-name">{{ product.name }}</strong>
            <span class="product-subtitle">{{ product.subtitle || '品质商品，放心选购' }}</span>
            <span class="product-meta">
              <strong>{{ formatMoney(product.minPrice) }}</strong>
              <span v-if="product.totalStock > 0">查看详情</span>
              <span v-else class="sold-out-text">暂时缺货</span>
            </span>
          </span>
        </button>

        <van-empty
          v-if="!loading && !errorMessage && records.length === 0"
          description="没有找到符合条件的商品"
          class="empty-block"
        >
          <van-button round type="primary" size="small" @click="resetFilters">
            查看全部商品
          </van-button>
        </van-empty>
      </van-list>
    </van-pull-refresh>
  </section>
</template>

<style scoped>
.mobile-products {
  min-height: 100%;
  padding: 0 0 24px;
  background: #f5f5f2;
}

.search-bar {
  position: sticky;
  top: 0;
  z-index: 12;
  padding: 10px 12px 8px;
  background: #fff;
  border-bottom: 1px solid #e2e1dd;
}

.m-search {
  display: flex;
  align-items: stretch;
  height: 40px;
  overflow: hidden;
  background: #fafaf8;
  border: 1px solid #e2e1dd;
  border-radius: 8px;
  box-sizing: border-box;
}

.m-search-field {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 4px 0 8px;
  background: transparent;
}

.m-search-field :deep(.van-cell) {
  padding: 0;
  background: transparent;
  align-items: center;
}

.m-search-field :deep(.van-cell::after) {
  display: none;
}

.m-search-field :deep(.van-field__body) {
  height: 100%;
  align-items: center;
}

.m-search-field :deep(.van-field__control) {
  height: 100%;
  font-size: 14px;
  color: #252422;
}

.m-search-field :deep(.van-field__left-icon) {
  display: flex;
  align-items: center;
  margin-right: 4px;
  color: #746f68;
}

.m-search-field :deep(.van-field__clear) {
  margin-right: 4px;
  color: #a9a59e;
}

.m-search-btn {
  flex: 0 0 auto;
  min-width: 68px;
  margin: 0;
  padding: 0 14px;
  border: 0;
  color: #fff;
  background: #e5484d;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
}

.m-search-btn:active {
  background: #c83b40;
}

.category-bar {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none;
}

.category-bar::-webkit-scrollbar {
  display: none;
}

.category-chip {
  flex-shrink: 0;
  padding: 6px 14px;
  color: #45433f;
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  background: #fff;
  border: 1px solid #e2e1dd;
  border-radius: 999px;
}

.category-chip.active {
  color: #e5484d;
  background: #fff1f0;
  border-color: #f2bbb9;
}

.result-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px 4px;
  color: #746f68;
  font-size: 13px;
}

.result-heading strong {
  margin-right: 4px;
  color: #252422;
  font-size: 15px;
}

.result-heading button {
  padding: 4px;
  color: #e5484d;
  font: inherit;
  font-weight: 600;
  background: transparent;
  border: 0;
}

.product-list {
  min-height: 200px;
  padding: 8px 12px 20px;
}

.product-card {
  display: flex;
  width: 100%;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  color: inherit;
  font: inherit;
  text-align: left;
  background: #fff;
  border: 1px solid #e2e1dd;
  border-radius: 10px;
  box-shadow: none;
  cursor: pointer;
  transition: border-color .18s ease, background-color .18s ease;
}

.product-card:active {
  background: #fafaf8;
  border-color: #b6b3ad;
}

.product-thumb {
  position: relative;
  display: grid;
  flex-shrink: 0;
  width: 100px;
  height: 100px;
  place-items: center;
  overflow: hidden;
  background: #efefeb;
  border-radius: 8px;
}

.product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  color: #d9a7a5;
  font-size: 11px;
  font-weight: 800;
}

.stock-badge {
  position: absolute;
  right: 4px;
  bottom: 4px;
  padding: 2px 6px;
  color: #166534;
  background: rgba(220, 252, 231, .94);
  border-radius: 6px;
  font-size: 10px;
}

.stock-badge.sold-out {
  color: #991b1b;
  background: rgba(254, 226, 226, .94);
}

.product-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.product-name {
  display: -webkit-box;
  overflow: hidden;
  color: #252422;
  font-size: 14px;
  line-height: 1.4;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-subtitle {
  display: -webkit-box;
  margin: 4px 0 6px;
  overflow: hidden;
  color: #94a3b8;
  font-size: 11px;
  line-height: 1.4;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-meta strong {
  color: #c83b40;
  font-size: 16px;
}

.product-meta > span {
  color: #e5484d;
  font-size: 12px;
  font-weight: 600;
}

.product-meta .sold-out-text {
  color: #94a3b8;
}

.empty-block {
  padding: 40px 0;
}
</style>
