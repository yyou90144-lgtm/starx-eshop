<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getHotProducts, getProducts } from '../../api/catalog'
import HotProductRanking from '../../components/catalog/HotProductRanking.vue'
import { productImageSrc, restoreProductImage } from '../../utils/productVisual'

const router = useRouter()
const categories = ref([])
const categoryLoading = ref(false)
const dealProducts = ref([])
const hotPreview = ref([])
const dealProductsError = ref(false)
const hotPreviewError = ref(false)
const currentBanner = ref(0)
let bannerTimer

const displayName = (name = '') => name
  .replace(/^演示分类-/, '')
  .replace(/^演示\s*/, '')
  .replace(/^演示/, '')

const findCategoryByKeywords = (keywords) => categories.value.find((item) => {
  const name = displayName(item.name)
  return keywords.some((word) => name.includes(word))
})

const banners = computed(() => {
  const phone = findCategoryByKeywords(['手机', '数码'])
  const computer = findCategoryByKeywords(['电脑', '办公'])
  const home = findCategoryByKeywords(['家居', '生活'])
  const first = categories.value[0]
  const second = categories.value[1]
  const third = categories.value[2]

  return [
    {
      key: 'phone',
      badge: '手机数码',
      title: '把日常装进更轻的设备',
      subtitle: '从通勤到创作，挑一件真正适合你使用方式的设备。',
      cta: '逛手机数码',
      image: '/images/catalog/phone-studio.jpg',
      target: phone || first ? { type: 'category', categoryId: (phone || first).id } : { type: 'catalog' },
    },
    {
      key: 'computer',
      badge: '电脑办公',
      title: '让工作区更顺手',
      subtitle: '笔记本、键鼠和显示器，按你的桌面节奏一次选齐。',
      cta: '逛电脑办公',
      image: '/images/catalog/laptop-studio.jpg',
      target: computer || second ? { type: 'category', categoryId: (computer || second).id } : { type: 'catalog' },
    },
    {
      key: 'home',
      badge: '家居生活',
      title: '让家里的每一盏灯都刚刚好',
      subtitle: '清洁、照明和小家电，买回去就能用得上的生活好物。',
      cta: '逛家居生活',
      image: '/images/catalog/lamp-studio.jpg',
      target: home || third ? { type: 'category', categoryId: (home || third).id } : { type: 'catalog' },
    },
    {
      key: 'hot',
      badge: '近 30 天热销',
      title: '先看看大家正在买什么',
      subtitle: '按有效订单销量排序，把真实购买行为放在你面前。',
      cta: '查看热销榜',
      image: '/images/catalog/headphones-studio.jpg',
      target: { type: 'anchor', id: 'hot-ranking' },
    },
  ]
})

const activeBanner = computed(() => banners.value[currentBanner.value] || banners.value[0])
const channelMenus = computed(() => categories.value.map((item, index) => ({
  id: item.id,
  name: displayName(item.name),
  desc: ['高频设备', '效率工具', '居家好物'][index] || '品质日常',
})))

const formatMoney = (value) => value === null || value === undefined
  ? '暂无报价'
  : `¥${Number(value).toFixed(2)}`

const goCatalog = (query = {}) => router.push({ name: 'pc-products', query })
const openCategory = (categoryId) => goCatalog(categoryId ? { categoryId } : {})
const openProduct = (id) => router.push({ name: 'pc-product-detail', params: { id } })
const scrollToHot = () => document.getElementById('hot-ranking')?.scrollIntoView({ behavior: 'smooth', block: 'start' })

const openCampaign = (banner = activeBanner.value) => {
  const target = banner?.target
  if (!target) return
  if (target.type === 'category') return openCategory(target.categoryId)
  if (target.type === 'catalog') return goCatalog()
  return scrollToHot()
}

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    categories.value = await getCategories()
  } catch {
    categories.value = []
  } finally {
    categoryLoading.value = false
  }
}

const loadDeals = async () => {
  const [pageResult, hotResult] = await Promise.allSettled([
    getProducts({ current: 1, size: 8 }),
    getHotProducts({ days: 30, limit: 3 }),
  ])

  let list = []
  dealProductsError.value = pageResult.status === 'rejected'
  hotPreviewError.value = hotResult.status === 'rejected'

  if (pageResult.status === 'fulfilled') {
    list = (pageResult.value.records || []).map((product) => ({
      ...product,
      name: displayName(product.name),
      imageFailed: false,
    }))
    dealProducts.value = [...list]
      .filter((item) => Number(item.totalStock) > 0)
      .sort((a, b) => Number(a.totalStock) - Number(b.totalStock))
      .slice(0, 3)
    if (dealProducts.value.length < 3) dealProducts.value = list.slice(0, 3)
  } else {
    dealProducts.value = []
  }

  if (hotResult.status === 'fulfilled') {
    hotPreview.value = (hotResult.value || []).map((item) => ({
      ...item,
      name: displayName(item.name),
      imageFailed: false,
      fallback: false,
    }))
  } else {
    hotPreview.value = list.slice(0, 3).map((product) => ({
      ...product,
      productId: product.id,
      salesQuantity: null,
      fallback: true,
    }))
  }
}

const startBanner = () => {
  if (bannerTimer) window.clearInterval(bannerTimer)
  bannerTimer = window.setInterval(() => {
    if (banners.value.length) currentBanner.value = (currentBanner.value + 1) % banners.value.length
  }, 5600)
}

const stopBanner = () => {
  if (bannerTimer) window.clearInterval(bannerTimer)
  bannerTimer = undefined
}

const goBanner = (index) => {
  currentBanner.value = index
  stopBanner()
  startBanner()
}

onMounted(() => {
  void loadCategories()
  void loadDeals()
  startBanner()
})
onUnmounted(stopBanner)
watch(() => banners.value.length, (length) => {
  if (currentBanner.value >= length) currentBanner.value = 0
})
</script>

<template>
  <div class="home-page">
    <section class="home-intro">
      <div>
        <h1>买回去就会用上的好东西</h1>
        <p>从设备到家居，先看清规格和库存，再做一件确定的购买决定。</p>
      </div>
      <button type="button" class="home-intro-link" @click="goCatalog()">查看全部商品</button>
    </section>

    <section class="hero-stage">
      <aside class="channel-menu" v-loading="categoryLoading">
        <div class="channel-heading">
          <span>按品类逛</span>
          <small>{{ categories.length || 0 }} 个品类</small>
        </div>
        <button
          v-for="channel in channelMenus"
          :key="channel.id"
          type="button"
          class="channel-item"
          @click="openCategory(channel.id)"
        >
          <span class="channel-text">
            <b>{{ channel.name }}</b>
            <small>{{ channel.desc }}</small>
          </span>
        </button>
        <button type="button" class="channel-all" @click="goCatalog()">浏览全部品类</button>
      </aside>

      <div class="hero-banner" @mouseenter="stopBanner" @mouseleave="startBanner">
        <img class="hero-image" :src="activeBanner.image" alt="精选商品场景" />
        <div class="hero-shade" aria-hidden="true"></div>
        <div class="hero-copy">
          <span class="hero-badge">{{ activeBanner.badge }}</span>
          <h2>{{ activeBanner.title }}</h2>
          <p>{{ activeBanner.subtitle }}</p>
          <button type="button" class="hero-cta" @click="openCampaign()">{{ activeBanner.cta }}</button>
        </div>
        <div class="hero-controls" aria-label="精选内容切换">
          <button
            v-for="(item, index) in banners"
            :key="item.key"
            type="button"
            :class="{ active: currentBanner === index }"
            :aria-label="`切换到${item.badge}`"
            :aria-pressed="currentBanner === index"
            @click="goBanner(index)"
          ></button>
        </div>
      </div>

      <aside class="hero-side">
        <section class="side-section">
          <div class="side-heading">
            <h2>库存提醒</h2>
            <span>先到先得</span>
          </div>
          <button v-for="item in dealProducts" :key="item.id" type="button" class="deal-row" @click="openProduct(item.id)">
            <img
              v-if="!item.imageFailed"
              :src="productImageSrc(item)"
              :alt="item.name"
              @error="(event) => { if (event?.currentTarget?.dataset?.fallbackApplied === 'true') item.imageFailed = true; else restoreProductImage(event, item) }"
            />
            <span v-else class="deal-fallback">STARX</span>
            <span class="deal-info">
              <b>{{ item.name }}</b>
              <small>库存 {{ item.totalStock }} 件</small>
              <strong>{{ formatMoney(item.minPrice) }}</strong>
            </span>
          </button>
          <p v-if="!dealProducts.length" class="side-empty">
            {{ dealProductsError ? '商品信息暂时不可用' : '暂无库存提醒' }}
          </p>
        </section>

        <section class="side-section hot-side">
          <div class="side-heading">
            <h2>热销榜</h2>
            <button type="button" class="linkish" @click="scrollToHot">看完整榜单</button>
          </div>
          <button v-for="(item, index) in hotPreview" :key="item.productId" type="button" class="hot-row" @click="openProduct(item.productId)">
            <span class="rank-no">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="deal-info">
              <b>{{ item.name }}</b>
              <small>{{ item.fallback ? '为你推荐' : `已售 ${item.salesQuantity} 件` }}</small>
              <strong>{{ formatMoney(item.minPrice) }}</strong>
            </span>
          </button>
          <p v-if="!hotPreview.length" class="side-empty">
            {{ hotPreviewError ? '热销数据暂时不可用' : '暂无热销商品' }}
          </p>
        </section>
      </aside>
    </section>

    <section id="hot-ranking" class="hot-section">
      <div class="section-heading">
        <div>
          <h2>近 30 天热销榜</h2>
        </div>
        <p>按已支付、已发货和已完成订单销量排序</p>
      </div>
      <HotProductRanking :limit="5" :show-header="false" />
    </section>

    <section class="home-guides">
      <div class="guide-main">
        <h2>每一件商品，先看清再下单。</h2>
        <p>商品详情、SKU 规格、实时库存和订单状态都在同一条路径里，少一点猜测，多一点确定。</p>
        <button type="button" class="guide-link" @click="goCatalog()">开始挑选</button>
      </div>
      <div class="guide-list">
        <article><span><b>规格透明</b><small>多规格价格与库存分开呈现</small></span></article>
        <article><span><b>库存状态</b><small>无货规格会直接阻止误购</small></span></article>
        <article><span><b>订单可追</b><small>支付、发货和收货状态都有记录</small></span></article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page { width: min(var(--eshop-container), calc(100% - 40px)); margin: 0 auto; padding: 28px 0 80px; }
.home-intro { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; padding: 12px 0 30px; }
.home-intro h1 { max-width: 620px; margin: 0; color: var(--eshop-text); font-size: 48px; line-height: 1.12; }
.home-intro p:not(.home-kicker) { max-width: 540px; margin: 14px 0 0; color: var(--eshop-muted); font-size: 15px; line-height: 1.7; }
.home-intro-link, .guide-link, .linkish { padding: 0; color: var(--eshop-primary); background: none; border: 0; font: inherit; font-size: 13px; font-weight: 750; cursor: pointer; }
.hero-stage { display: grid; grid-template-columns: 212px minmax(0, 1fr) 264px; gap: 14px; min-height: 430px; }
.channel-menu, .side-section { background: #fff; border: 1px solid var(--eshop-border); border-radius: var(--eshop-radius); box-shadow: none; }
.channel-menu { display: flex; flex-direction: column; padding: 18px 14px 14px; }
.channel-heading, .side-heading { display: flex; align-items: baseline; justify-content: space-between; gap: 10px; }
.channel-heading { padding: 0 8px 14px; border-bottom: 1px solid var(--eshop-border); }
.channel-heading span, .side-heading h2 { color: var(--eshop-text); font-size: 15px; font-weight: 800; }
.channel-heading small, .side-heading > span { color: var(--eshop-muted); font-size: 11px; }
.channel-item { display: flex; align-items: center; gap: 8px; width: 100%; padding: 17px 8px; text-align: left; background: transparent; border: 0; border-bottom: 1px solid var(--eshop-border); cursor: pointer; transition: color .18s ease, background-color .18s ease; }
.channel-item:hover { background: var(--eshop-surface-soft); }
.channel-text { display: grid; flex: 1; min-width: 0; gap: 4px; }
.channel-text b { color: var(--eshop-text); font-size: 14px; }
.channel-text small { color: var(--eshop-muted); font-size: 11px; }
.channel-all { margin-top: auto; padding: 12px 10px; color: var(--eshop-primary); background: var(--eshop-primary-soft); border: 0; border-radius: 8px; font-size: 13px; font-weight: 750; cursor: pointer; }
.hero-banner { position: relative; min-height: 430px; overflow: hidden; border-radius: var(--eshop-radius); background: var(--eshop-ink); isolation: isolate; }
.hero-image { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; object-position: center; z-index: -2; }
.hero-shade { position: absolute; inset: 0; z-index: -1; background: rgba(36, 35, 33, .46); }
.hero-copy { max-width: 460px; padding: 58px 44px; color: #fff; }
.hero-badge { display: inline-flex; padding: 6px 10px; color: #fff; border: 1px solid rgba(255, 255, 255, .38); border-radius: 999px; font-size: 11px; font-weight: 750; }
.hero-copy h2 { max-width: 390px; margin: 18px 0 12px; color: #fff !important; font-size: 44px; line-height: 1.12; }
.hero-copy p { max-width: 360px; margin: 0; color: rgba(255, 255, 255, .78); font-size: 14px; line-height: 1.7; }
.hero-cta { display: inline-flex; align-items: center; gap: 8px; margin-top: 28px; padding: 12px 17px; color: #fff; background: var(--eshop-primary); border: 0; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; transition: background .2s ease; }
.hero-cta:hover { background: var(--eshop-primary-strong); }
.hero-controls { position: absolute; right: 28px; bottom: 26px; display: flex; gap: 8px; }
.hero-controls button { width: 24px; height: 4px; padding: 0; background: rgba(255, 255, 255, .38); border: 0; border-radius: 99px; cursor: pointer; }
.hero-controls button.active { background: #fff; }
.hero-side { display: grid; grid-template-rows: 1fr 1fr; gap: 16px; }
.side-section { min-width: 0; padding: 18px 16px; }
.side-heading { margin-bottom: 12px; }
.side-heading h2 { margin: 0; }
.side-heading > span { color: var(--eshop-primary); font-weight: 700; }
.deal-row, .hot-row { display: flex; align-items: center; gap: 10px; width: 100%; padding: 10px 0; text-align: left; background: none; border: 0; border-bottom: 1px solid var(--eshop-border); cursor: pointer; }
.deal-row:last-of-type, .hot-row:last-of-type { border-bottom: 0; }
.deal-row img, .deal-fallback { flex: 0 0 56px; width: 56px; height: 56px; object-fit: cover; border-radius: 8px; background: var(--eshop-surface-soft); }
.deal-fallback { display: grid; place-items: center; color: var(--eshop-primary); font-size: 10px; font-weight: 800; }
.deal-info { display: grid; min-width: 0; gap: 3px; }
.deal-info b { overflow: hidden; color: var(--eshop-text); font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.deal-info small { color: var(--eshop-muted); font-size: 11px; }
.deal-info strong { color: var(--eshop-primary); font-size: 13px; }
.hot-row { align-items: flex-start; }
.rank-no { flex: 0 0 28px; color: #94a3b8; font-size: 13px; font-weight: 800; line-height: 1.5; }
.hot-row:first-of-type .rank-no { color: var(--eshop-primary); }
.side-empty { margin: 22px 0 8px; color: var(--eshop-muted); font-size: 12px; text-align: center; }
.hot-section { margin-top: 56px; scroll-margin-top: 130px; }
.section-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 16px; }
.section-heading h2 { margin: 0; color: var(--eshop-text); font-size: 28px; }
.section-heading > p { margin: 0 0 2px; color: var(--eshop-muted); font-size: 12px; }
.home-guides { display: grid; grid-template-columns: 1.1fr .9fr; gap: 34px; margin-top: 62px; padding: 34px 0 0; border-top: 1px solid var(--eshop-border); }
.guide-main h2 { max-width: 520px; margin: 0; color: var(--eshop-text); font-size: 36px; line-height: 1.16; }
.guide-main > p:not(.section-kicker) { max-width: 520px; margin: 14px 0 22px; color: var(--eshop-muted); font-size: 14px; line-height: 1.75; }
.guide-list { display: grid; gap: 0; }
.guide-list article { padding: 15px 0; border-bottom: 1px solid var(--eshop-border); }
.guide-list article:first-child { border-top: 1px solid var(--eshop-border); }
.guide-list span { display: grid; gap: 3px; }
.guide-list b { color: var(--eshop-text); font-size: 14px; }
.guide-list small { color: var(--eshop-muted); font-size: 12px; }
@media (max-width: 1080px) { .hero-stage { grid-template-columns: 190px minmax(0, 1fr); } .hero-side { display: none; } }
@media (max-width: 720px) {
  .home-page { width: min(100% - 28px, 600px); padding-top: 8px; }
  .home-intro { display: block; padding-bottom: 22px; }
  .home-intro h1 { font-size: 34px; }
  .home-intro-link { display: inline-block; margin-top: 16px; }
  .hero-stage { display: block; min-height: 0; }
  .channel-menu { margin-bottom: 14px; }
  .channel-item { padding: 12px 8px; }
  .channel-all { margin-top: 12px; }
  .hero-banner { min-height: 370px; }
  .hero-copy { padding: 38px 26px; }
  .hero-copy h2 { font-size: 34px; }
  .section-heading { display: block; }
  .section-heading > p { margin-top: 8px; }
  .home-guides { grid-template-columns: 1fr; gap: 24px; margin-top: 46px; }
}
@media (prefers-reduced-motion: reduce) { .hero-cta, .channel-item { transition: none; } }
</style>
