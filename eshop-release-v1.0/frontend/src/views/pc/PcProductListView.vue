<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getProducts } from '../../api/catalog'
import { productImageSrc } from '../../utils/productVisual'

const route = useRoute()
const router = useRouter()
const categories = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const categoryLoading = ref(false)
const errorMessage = ref('')
const keywordInput = ref('')
const size = 12

const current = computed(() => {
  const value = Number(route.query.current)
  return Number.isInteger(value) && value > 0 ? value : 1
})
const categoryId = computed(() => {
  const value = Number(route.query.categoryId)
  return Number.isInteger(value) && value > 0 ? value : undefined
})
const keyword = computed(() => typeof route.query.keyword === 'string' ? route.query.keyword.trim() : '')
const displayName = (name = '') => name.replace(/^演示分类-/, '').replace(/^演示\s*/, '').replace(/^演示/, '')
const activeCategoryName = computed(() => displayName(categories.value.find((item) => item.id === categoryId.value)?.name || '') || '全部商品')
const pageTitle = computed(() => keyword.value ? `“${keyword.value}” 的搜索结果` : activeCategoryName.value)
const formatMoney = (value) => value === null || value === undefined ? '暂无报价' : `¥${Number(value).toFixed(2)}`

const replaceQuery = (changes) => {
  const query = { ...route.query, ...changes }
  Object.keys(query).forEach((key) => {
    if (query[key] === undefined || query[key] === null || query[key] === '') delete query[key]
  })
  router.push({ name: 'pc-products', query })
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
    const page = await getProducts({ current: current.value, size, categoryId: categoryId.value, keyword: keyword.value || undefined })
    records.value = (page.records || []).map((product) => ({ ...product, name: displayName(product.name), imageFailed: false }))
    total.value = page.total
  } catch (error) {
    records.value = []
    total.value = 0
    errorMessage.value = error.message || '商品加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const selectCategory = (id) => {
  replaceQuery({ categoryId: id || undefined, current: undefined })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
const search = () => {
  replaceQuery({ keyword: keywordInput.value.trim() || undefined, current: undefined })
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
const openProduct = (id) => router.push({ name: 'pc-product-detail', params: { id } })

onMounted(loadCategories)
watch(() => [route.query.current, route.query.categoryId, route.query.keyword], loadProducts, { immediate: true })
</script>

<template>
  <div class="catalog-page">
    <el-breadcrumb separator="/" class="catalog-breadcrumb">
      <el-breadcrumb-item :to="{ name: 'pc-home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>全部商品</el-breadcrumb-item>
      <el-breadcrumb-item v-if="categoryId || keyword">{{ pageTitle }}</el-breadcrumb-item>
    </el-breadcrumb>

    <section class="catalog-heading">
      <div>
        <h1>{{ pageTitle }}</h1>
        <p>按品类或关键词找到合适的商品，规格和库存会在详情页明确展示。</p>
      </div>
      <span class="result-count">{{ total }} 件商品</span>
    </section>

    <section class="catalog-tools">
      <form class="search-row" @submit.prevent="search">
        <label for="catalog-search">搜索商品</label>
        <el-input id="catalog-search" v-model="keywordInput" clearable maxlength="120" placeholder="输入商品名称" size="large" @clear="search" />
        <el-button type="primary" size="large" native-type="submit">搜索</el-button>
        <el-button size="large" @click="resetFilters">清除</el-button>
      </form>
      <div v-loading="categoryLoading" class="category-row">
        <span class="filter-label">品类</span>
        <button type="button" class="category-chip" :class="{ active: !categoryId }" @click="selectCategory()">全部</button>
        <button v-for="category in categories" :key="category.id" type="button" class="category-chip" :class="{ active: categoryId === category.id }" @click="selectCategory(category.id)">
          {{ displayName(category.name) }}
        </button>
      </div>
    </section>

    <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" class="result-alert" />

    <div v-if="loading" class="product-grid product-grid-skeleton" aria-label="正在加载商品">
      <div v-for="index in 8" :key="index" class="product-skeleton"><span></span><i></i><b></b><em></em></div>
    </div>
    <div v-else class="product-grid">
      <button v-for="product in records" :key="product.id" type="button" class="product-card" @click="openProduct(product.id)">
        <div class="product-image">
          <img v-if="!product.imageFailed" :src="productImageSrc(product)" :alt="product.name" loading="lazy" @error="product.imageFailed = true" />
          <div v-else class="image-placeholder">STARX</div>
          <span v-if="product.totalStock <= 0" class="stock-badge sold-out">暂时缺货</span>
          <span v-else-if="product.totalStock <= 10" class="stock-badge low">仅剩 {{ product.totalStock }} 件</span>
        </div>
        <div class="product-content">
          <h2>{{ product.name }}</h2>
          <p>{{ product.subtitle || '查看规格、价格和库存信息' }}</p>
          <div class="product-meta">
            <strong>{{ formatMoney(product.minPrice) }}</strong>
            <span v-if="product.minPrice !== product.maxPrice && product.maxPrice">起</span>
            <em>查看详情</em>
          </div>
        </div>
      </button>
    </div>

    <el-empty v-if="!loading && !errorMessage && records.length === 0" description="没有找到符合条件的商品">
      <el-button type="primary" @click="resetFilters">查看全部商品</el-button>
    </el-empty>
    <div v-if="total > size" class="product-pagination">
      <el-pagination background layout="prev, pager, next, total" :current-page="current" :page-size="size" :total="total" @current-change="changePage" />
    </div>
  </div>
</template>

<style scoped>
.catalog-page { width: min(var(--eshop-container), calc(100% - 40px)); margin: 0 auto; padding: 28px 0 80px; }
.catalog-breadcrumb { margin-bottom: 28px; }
.catalog-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; margin-bottom: 20px; }
.catalog-heading h1 { margin: 0; color: var(--eshop-text); font-size: 44px; line-height: 1.15; }
.catalog-heading p { max-width: 540px; margin: 12px 0 0; color: var(--eshop-muted); font-size: 14px; line-height: 1.7; }
.result-count { color: var(--eshop-muted); font-size: 13px; white-space: nowrap; }
.catalog-tools { margin-bottom: 24px; padding: 18px 20px; background: #fff; border: 1px solid var(--eshop-border); border-radius: var(--eshop-radius); box-shadow: none; }
.search-row { display: grid; grid-template-columns: auto minmax(0, 1fr) auto auto; align-items: center; gap: 10px; }
.search-row label { color: var(--eshop-text); font-size: 13px; font-weight: 750; }
.search-row :deep(.el-input__wrapper) { min-height: 42px; border-radius: 8px; box-shadow: 0 0 0 1px var(--eshop-border) inset; }
.search-row :deep(.el-button) { height: 42px; border-radius: 8px; font-weight: 700; }
.category-row { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--eshop-border); }
.filter-label { margin-right: 4px; color: var(--eshop-text); font-size: 13px; font-weight: 750; }
.category-chip { padding: 7px 13px; color: var(--eshop-muted); background: var(--eshop-surface-soft); border: 1px solid var(--eshop-border); border-radius: 999px; font-size: 13px; cursor: pointer; }
.category-chip:hover, .category-chip.active { color: var(--eshop-primary); background: var(--eshop-primary-soft); border-color: #ffb3a7; }
.result-alert { margin-bottom: 16px; }
.product-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.product-card { overflow: hidden; padding: 0; text-align: left; background: #fff; border: 1px solid var(--eshop-border); border-radius: var(--eshop-radius); cursor: pointer; box-shadow: none; transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease; }
.product-card:hover { transform: translateY(-2px); border-color: #d2cfca; box-shadow: 0 12px 26px rgba(37, 36, 34, .09); }
.product-image { position: relative; display: grid; aspect-ratio: 1; place-items: center; overflow: hidden; background: var(--eshop-surface-soft); }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.image-placeholder { color: var(--eshop-muted); font-size: 19px; font-weight: 800; }
.stock-badge { position: absolute; right: 10px; bottom: 10px; padding: 4px 8px; color: #9a6700; background: rgba(255, 251, 235, .96); border-radius: 999px; font-size: 11px; font-weight: 700; }
.stock-badge.sold-out { color: #b42318; background: rgba(255, 241, 240, .96); }
.product-content { padding: 15px 15px 16px; }
.product-content h2 { display: -webkit-box; overflow: hidden; margin: 0; color: var(--eshop-text); font-size: 15px; line-height: 1.4; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.product-content p { display: -webkit-box; overflow: hidden; min-height: 36px; margin: 7px 0 14px; color: var(--eshop-muted); font-size: 12px; line-height: 1.5; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.product-meta { display: flex; align-items: baseline; gap: 4px; }
.product-meta strong { color: var(--eshop-primary); font-size: 19px; }
.product-meta > span { color: var(--eshop-muted); font-size: 11px; }
.product-meta em { margin-left: auto; color: var(--eshop-primary); font-size: 12px; font-style: normal; font-weight: 750; }
.product-grid-skeleton { min-height: 500px; }
.product-skeleton { overflow: hidden; background: #fff; border: 1px solid var(--eshop-border); border-radius: var(--eshop-radius); }
.product-skeleton span { display: block; aspect-ratio: 1; background: linear-gradient(100deg, #f0efec 25%, #fafaf8 37%, #f0efec 63%); background-size: 400% 100%; animation: skeleton-shimmer 1.3s ease infinite; }
.product-skeleton i, .product-skeleton b, .product-skeleton em { display: block; height: 12px; margin: 16px 16px 0; background: #efefeb; border-radius: 5px; }
.product-skeleton b { width: 66%; margin-top: 9px; }
.product-skeleton em { width: 36%; height: 18px; margin-top: 16px; margin-bottom: 16px; }
@keyframes skeleton-shimmer { to { background-position: -135% 0; } }
.product-pagination { display: flex; justify-content: center; margin-top: 30px; }
@media (max-width: 1060px) { .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 720px) {
  .catalog-page { width: min(100% - 28px, 600px); padding-top: 16px; }
  .catalog-breadcrumb { margin-bottom: 20px; }
  .catalog-heading { display: block; }
  .result-count { display: block; margin-top: 12px; }
  .catalog-heading h1 { font-size: 32px; }
  .catalog-tools { padding: 14px; }
  .search-row { grid-template-columns: 1fr auto; }
  .search-row label { grid-column: 1 / -1; }
  .search-row :deep(.el-input) { grid-column: 1 / -1; }
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
  .product-content { padding: 12px; }
  .product-content h2 { font-size: 14px; }
  .product-meta strong { font-size: 17px; }
}
@media (prefers-reduced-motion: reduce) { .product-card, .product-skeleton span { animation: none; transition: none; } }
</style>
