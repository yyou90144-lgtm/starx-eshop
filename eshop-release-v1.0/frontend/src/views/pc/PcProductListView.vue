<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getProducts } from '../../api/catalog'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const categoryLoading = ref(false)
const errorMessage = ref('')
const keywordInput = ref('')

const current = computed(() => {
  const value = Number(route.query.current)
  return Number.isInteger(value) && value > 0 ? value : 1
})
const size = 12
const categoryId = computed(() => {
  const value = Number(route.query.categoryId)
  return Number.isInteger(value) && value > 0 ? value : undefined
})
const keyword = computed(() => (
  typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''
))

const displayName = (name = '') => name
  .replace(/^演示分类-/, '')
  .replace(/^演示\s*/, '')
  .replace(/^演示/, '')

const activeCategoryName = computed(() => (
  displayName(categories.value.find((item) => item.id === categoryId.value)?.name || '') || '全部商品'
))

const pageTitle = computed(() => {
  if (keyword.value) return `“${keyword.value}” 的搜索结果`
  return activeCategoryName.value
})

const formatMoney = (value) => {
  if (value === null || value === undefined) return '暂无报价'
  return `¥${Number(value).toFixed(2)}`
}

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    categories.value = await getCategories()
  } catch (error) {
    errorMessage.value = error.message || '商品分类加载失败'
  } finally {
    categoryLoading.value = false
  }
}

const loadProducts = async () => {
  loading.value = true
  errorMessage.value = ''
  keywordInput.value = keyword.value
  try {
    const page = await getProducts({
      current: current.value,
      size,
      categoryId: categoryId.value,
      keyword: keyword.value || undefined,
    })
    records.value = page.records.map((product) => ({
      ...product,
      name: displayName(product.name),
      imageFailed: false,
    }))
    total.value = page.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorMessage.value = error.message || '商品加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const replaceQuery = (changes) => {
  const query = { ...route.query, ...changes }
  Object.keys(query).forEach((key) => {
    if (query[key] === undefined || query[key] === null || query[key] === '') delete query[key]
  })
  router.push({ name: 'pc-products', query })
}

const selectCategory = (id) => {
  replaceQuery({
    categoryId: id || undefined,
    current: undefined,
  })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const search = () => {
  replaceQuery({
    keyword: keywordInput.value.trim() || undefined,
    current: undefined,
  })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const resetFilters = () => {
  keywordInput.value = ''
  router.push({ name: 'pc-products' })
}

const changePage = (page) => {
  replaceQuery({ current: page === 1 ? undefined : page })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const openProduct = (id) => {
  router.push({ name: 'pc-product-detail', params: { id } })
}

onMounted(loadCategories)

watch(
  () => [route.query.current, route.query.categoryId, route.query.keyword],
  loadProducts,
  { immediate: true },
)
</script>

<template>
  <div class="catalog-page">
    <el-breadcrumb separator="›" class="catalog-breadcrumb">
      <el-breadcrumb-item :to="{ name: 'pc-home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>全部商品</el-breadcrumb-item>
      <el-breadcrumb-item v-if="categoryId || keyword">{{ pageTitle }}</el-breadcrumb-item>
    </el-breadcrumb>

    <section class="catalog-panel">
      <div class="catalog-heading">
        <div>
          <p class="eyebrow">{{ keyword ? 'SEARCH RESULT' : 'ALL PRODUCTS' }}</p>
          <h1>{{ pageTitle }}</h1>
          <p class="catalog-desc">支持分类筛选与关键词搜索，点击商品进入详情选购。</p>
        </div>
        <span>共 {{ total }} 件</span>
      </div>

      <form class="search-row" @submit.prevent="search">
        <el-input
          v-model="keywordInput"
          clearable
          maxlength="120"
          placeholder="搜索商品名称"
          size="large"
          @clear="search"
        />
        <el-button type="primary" size="large" native-type="submit">搜索</el-button>
        <el-button size="large" @click="resetFilters">重置</el-button>
      </form>

      <div v-loading="categoryLoading" class="category-row">
        <span class="filter-label">分类</span>
        <button
          type="button"
          class="category-chip"
          :class="{ active: !categoryId }"
          @click="selectCategory()"
        >
          全部
        </button>
        <button
          v-for="category in categories"
          :key="category.id"
          type="button"
          class="category-chip"
          :class="{ active: categoryId === category.id }"
          @click="selectCategory(category.id)"
        >
          {{ displayName(category.name) }}
        </button>
      </div>
    </section>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="false"
      class="result-alert"
    />

    <div v-loading="loading" class="product-grid">
      <button
        v-for="product in records"
        :key="product.id"
        type="button"
        class="product-card"
        @click="openProduct(product.id)"
      >
        <div class="product-image">
          <img
            v-if="product.mainImage && !product.imageFailed"
            :src="product.mainImage"
            :alt="product.name"
            loading="lazy"
            @error="product.imageFailed = true"
          />
          <div v-else class="image-placeholder">STARX</div>
          <span v-if="product.totalStock <= 0" class="stock-badge sold-out">暂时缺货</span>
          <span v-else-if="product.totalStock <= 10" class="stock-badge low">仅剩 {{ product.totalStock }}</span>
        </div>
        <div class="product-content">
          <h3>{{ product.name }}</h3>
          <p>{{ product.subtitle || '品质甄选，放心下单' }}</p>
          <div class="product-meta">
            <strong>{{ formatMoney(product.minPrice) }}</strong>
            <span v-if="product.minPrice !== product.maxPrice && product.maxPrice">起</span>
            <em>立即购买</em>
          </div>
        </div>
      </button>
    </div>

    <el-empty
      v-if="!loading && !errorMessage && records.length === 0"
      description="没有找到符合条件的商品"
    >
      <el-button type="primary" @click="resetFilters">查看全部商品</el-button>
    </el-empty>

    <div v-if="total > size" class="product-pagination">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :current-page="current"
        :page-size="size"
        :total="total"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.catalog-page {
  width: min(var(--eshop-container, 1280px), calc(100% - 40px));
  margin: 0 auto;
  padding: 8px 0 56px;
}
.catalog-breadcrumb { margin-bottom: 14px; }
.catalog-panel {
  margin-bottom: 20px;
  padding: 24px 26px;
  background: #fff;
  border: 1px solid #ebe4de;
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(28, 20, 16, .04);
}
.catalog-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}
.catalog-heading .eyebrow {
  margin: 0 0 6px;
  color: #ff4d4f;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .14em;
}
.catalog-heading h1 {
  margin: 0 0 8px;
  font-size: 28px;
  letter-spacing: -.02em;
}
.catalog-desc { margin: 0; color: #8a837c; font-size: 13px; }
.catalog-heading > span {
  color: #8a837c;
  font-size: 13px;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}
.search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.search-row :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 12px;
}
.search-row :deep(.el-button) {
  height: 42px;
  border-radius: 12px;
  font-weight: 600;
}
.category-row { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.filter-label { color: #8a837c; font-size: 13px; font-weight: 600; }
.category-chip {
  padding: 8px 14px;
  color: #5c564f;
  background: #faf7f5;
  border: 1px solid #ebe4de;
  border-radius: 999px;
  cursor: pointer;
  transition: .18s ease;
}
.category-chip:hover,
.category-chip.active {
  color: #ff4d4f;
  background: #fff1f0;
  border-color: #ffd6d2;
}
.result-alert { margin-bottom: 16px; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  min-height: 240px;
}
.product-card {
  overflow: hidden;
  padding: 0;
  text-align: left;
  background: #fff;
  border: 1px solid #ebe4de;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(28, 20, 16, .04);
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
}
.product-card:hover {
  transform: translateY(-4px);
  border-color: #ffd6d2;
  box-shadow: 0 18px 40px rgba(28, 20, 16, .08);
}
.product-image {
  position: relative;
  display: grid;
  height: 220px;
  place-items: center;
  overflow: hidden;
  background: #f3ece6;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s ease;
}
.product-card:hover .product-image img { transform: scale(1.045); }
.image-placeholder {
  color: #ffccc7;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: .08em;
}
.stock-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 4px 9px;
  color: #a16207;
  background: rgba(255, 251, 235, .96);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.stock-badge.sold-out {
  color: #cf1322;
  background: rgba(255, 241, 240, .96);
}
.product-content { padding: 14px 14px 16px; }
.product-content h3 {
  display: -webkit-box;
  min-height: 44px;
  margin: 0;
  overflow: hidden;
  color: #1c1917;
  font-size: 15px;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.product-content p {
  overflow: hidden;
  margin: 8px 0 14px;
  color: #8a837c;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-meta { display: flex; align-items: baseline; gap: 4px; }
.product-meta strong {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}
.product-meta span { color: #ff4d4f; font-size: 12px; }
.product-meta em {
  margin-left: auto;
  color: #b8b0a8;
  font-size: 12px;
  font-style: normal;
  transition: color .18s ease;
}
.product-card:hover .product-meta em { color: #ff4d4f; }
.product-pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
@media (max-width: 1000px) {
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 760px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .product-image { height: 150px; }
  .search-row { grid-template-columns: 1fr; }
  .catalog-heading { flex-direction: column; align-items: flex-start; }
}
</style>
