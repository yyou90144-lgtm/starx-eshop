<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getHotProducts, getProducts } from '../../api/catalog'
import HotProductRanking from '../../components/catalog/HotProductRanking.vue'

const router = useRouter()

const categories = ref([])
const categoryLoading = ref(false)
const dealProducts = ref([])
const hotPreview = ref([])
const currentBanner = ref(0)
let bannerTimer

const displayName = (name = '') => name
  .replace(/^演示分类-/, '')
  .replace(/^演示\s*/, '')
  .replace(/^演示/, '')

const findCategoryByKeywords = (keywords) => (
  categories.value.find((item) => {
    const name = displayName(item.name)
    return keywords.some((word) => name.includes(word))
  })
)

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
      badge: '数码专场',
      title: '手机数码馆',
      subtitle: '旗舰影像、轻薄长续航与配件一次挑齐。',
      cta: '进入手机数码',
      tone: 'red',
      visual: 'phone',
      target: phone || first
        ? { type: 'category', categoryId: (phone || first).id }
        : { type: 'catalog' },
    },
    {
      key: 'computer',
      badge: '办公焕新',
      title: '电脑办公馆',
      subtitle: '笔记本、键鼠外设与显示器，高效办公更顺手。',
      cta: '进入电脑办公',
      tone: 'ink',
      visual: 'computer',
      target: computer || second
        ? { type: 'category', categoryId: (computer || second).id }
        : { type: 'catalog' },
    },
    {
      key: 'home',
      badge: '居家好物',
      title: '家居生活馆',
      subtitle: '清洁、照明与智能小电，把日子过得更舒服。',
      cta: '进入家居生活',
      tone: 'cream',
      visual: 'home',
      target: home || third
        ? { type: 'category', categoryId: (home || third).id }
        : { type: 'catalog' },
    },
    {
      key: 'hot',
      badge: '实时热销',
      title: '近 30 天热销榜',
      subtitle: '按有效订单销量排序，看看大家都在买什么。',
      cta: '查看热销榜',
      tone: 'dark',
      visual: 'hot',
      target: { type: 'anchor', id: 'hot-ranking' },
    },
  ]
})

const activeBanner = computed(() => banners.value[currentBanner.value] || banners.value[0])

const channelMenus = computed(() => {
  const icons = ['📱', '💻', '🏠', '🎁', '⚡', '🎧']
  return categories.value.map((item, index) => ({
    id: item.id,
    name: displayName(item.name),
    icon: icons[index % icons.length],
    desc: index === 0 ? '热卖尖货' : index === 1 ? '高效装备' : '品质日常',
  }))
})

const formatMoney = (value) => {
  if (value === null || value === undefined) return '暂无报价'
  return `¥${Number(value).toFixed(2)}`
}

const goCatalog = (query = {}) => {
  router.push({ name: 'pc-products', query })
}

const openCategory = (categoryId) => {
  goCatalog(categoryId ? { categoryId } : {})
}

const openProduct = (id) => {
  router.push({ name: 'pc-product-detail', params: { id } })
}

const scrollToHot = () => {
  document.getElementById('hot-ranking')?.scrollIntoView({
    behavior: 'smooth',
    block: 'start',
  })
}

const openCampaign = (banner = activeBanner.value) => {
  const target = banner?.target
  if (!target) return
  if (target.type === 'category' && target.categoryId) {
    openCategory(target.categoryId)
    return
  }
  if (target.type === 'catalog') {
    goCatalog()
    return
  }
  if (target.type === 'anchor' && target.id === 'hot-ranking') {
    scrollToHot()
  }
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
  try {
    const [page, hot] = await Promise.all([
      getProducts({ current: 1, size: 8 }),
      getHotProducts({ days: 30, limit: 3 }),
    ])
    const list = (page.records || []).map((product) => ({
      ...product,
      name: displayName(product.name),
      imageFailed: false,
    }))
    dealProducts.value = [...list]
      .filter((item) => Number(item.totalStock) > 0)
      .sort((a, b) => Number(a.totalStock) - Number(b.totalStock))
      .slice(0, 3)
    if (dealProducts.value.length < 3) {
      dealProducts.value = list.slice(0, 3)
    }
    hotPreview.value = (hot || []).map((item) => ({
      ...item,
      name: displayName(item.name),
      imageFailed: false,
    }))
  } catch {
    dealProducts.value = []
    hotPreview.value = []
  }
}

const startBanner = () => {
  stopBanner()
  bannerTimer = window.setInterval(() => {
    if (!banners.value.length) return
    currentBanner.value = (currentBanner.value + 1) % banners.value.length
  }, 5000)
}

const stopBanner = () => {
  if (bannerTimer) {
    window.clearInterval(bannerTimer)
    bannerTimer = undefined
  }
}

const goBanner = (step) => {
  const total = banners.value.length
  if (!total) return
  currentBanner.value = (currentBanner.value + step + total) % total
  stopBanner()
  startBanner()
}

const prevBanner = () => goBanner(-1)
const nextBanner = () => goBanner(1)

onMounted(() => {
  void loadCategories()
  void loadDeals()
  startBanner()
})

onUnmounted(stopBanner)

watch(
  () => banners.value.length,
  (length) => {
    if (currentBanner.value >= length) currentBanner.value = 0
  },
)
</script>

<template>
  <div class="home-page">
    <section class="hero-stage">
      <aside class="channel-menu" v-loading="categoryLoading">
        <header>
          <strong>商品分类</strong>
          <span>进入分类页</span>
        </header>
        <button
          v-for="channel in channelMenus"
          :key="channel.id"
          type="button"
          class="channel-item"
          @click="openCategory(channel.id)"
        >
          <span class="channel-icon" aria-hidden="true">{{ channel.icon }}</span>
          <span class="channel-text">
            <b>{{ channel.name }}</b>
            <small>{{ channel.desc }}</small>
          </span>
          <em>›</em>
        </button>
        <button type="button" class="channel-all" @click="goCatalog()">
          进入全部商品
        </button>
      </aside>

      <div
        class="hero-banner"
        :class="[`tone-${activeBanner.tone}`, `visual-${activeBanner.visual}`]"
        @mouseenter="stopBanner"
        @mouseleave="startBanner"
        @click="openCampaign(activeBanner)"
      >
        <div class="hero-copy">
          <span class="hero-badge">{{ activeBanner.badge }}</span>
          <h1>{{ activeBanner.title }}</h1>
          <p>{{ activeBanner.subtitle }}</p>
          <div class="hero-actions" @click.stop>
            <el-button type="primary" size="large" round @click="openCampaign(activeBanner)">
              {{ activeBanner.cta }}
            </el-button>
            <el-button size="large" round plain @click="goCatalog()">
              全部商品
            </el-button>
          </div>
          <p class="hero-target-tip">
            将进入：
            <template v-if="activeBanner.target?.type === 'category'">
              {{ displayName(categories.find((c) => c.id === activeBanner.target.categoryId)?.name) || '分类会场' }}
            </template>
            <template v-else-if="activeBanner.target?.type === 'anchor'">热销榜专区</template>
            <template v-else>全部商品页</template>
          </p>
        </div>
        <button
          type="button"
          class="hero-arrow hero-arrow-prev"
          aria-label="上一张广告"
          @click.stop="prevBanner"
        >
          <span class="hero-arrow-icon" aria-hidden="true"></span>
        </button>
        <button
          type="button"
          class="hero-arrow hero-arrow-next"
          aria-label="下一张广告"
          @click.stop="nextBanner"
        >
          <span class="hero-arrow-icon" aria-hidden="true"></span>
        </button>
        <div class="hero-dots" @click.stop>
          <button
            v-for="(item, index) in banners"
            :key="item.key"
            type="button"
            :class="{ active: currentBanner === index }"
            :title="item.title"
            @click="currentBanner = index"
          />
        </div>
      </div>

      <aside class="hero-side">
        <div class="side-card deals">
          <div class="side-heading">
            <h3>库存紧张</h3>
            <span>优先抢货</span>
          </div>
          <button
            v-for="item in dealProducts"
            :key="item.id"
            type="button"
            class="deal-row"
            @click="openProduct(item.id)"
          >
            <span class="deal-thumb">
              <img
                v-if="item.mainImage && !item.imageFailed"
                :src="item.mainImage"
                :alt="item.name"
                @error="item.imageFailed = true"
              />
              <em v-else>STARX</em>
            </span>
            <span class="deal-info">
              <b>{{ item.name }}</b>
              <small>仅剩 {{ item.totalStock }} 件</small>
              <strong>{{ formatMoney(item.minPrice) }}</strong>
            </span>
          </button>
          <p v-if="!dealProducts.length" class="side-empty">暂无推荐，稍后再来</p>
        </div>

        <div class="side-card hot-side">
          <div class="side-heading">
            <h3>热卖快达</h3>
            <button type="button" class="linkish" @click="scrollToHot">完整榜单</button>
          </div>
          <button
            v-for="(item, index) in hotPreview"
            :key="item.productId"
            type="button"
            class="deal-row"
            @click="openProduct(item.productId)"
          >
            <span class="rank-no" :class="{ top: index === 0 }">{{ index + 1 }}</span>
            <span class="deal-info">
              <b>{{ item.name }}</b>
              <small>已售 {{ item.salesQuantity }} 件</small>
              <strong>{{ formatMoney(item.minPrice) }}</strong>
            </span>
          </button>
          <p v-if="!hotPreview.length" class="side-empty">暂无热销数据</p>
        </div>
      </aside>
    </section>

    <div id="hot-ranking" class="hot-anchor">
      <HotProductRanking :limit="5" />
    </div>

    <section class="home-entry">
      <div>
        <p class="eyebrow">PRODUCT MALL</p>
        <h2>逛全部商品</h2>
        <p>按分类筛选、关键词搜索，挑齐你想要的好物。</p>
      </div>
      <el-button type="primary" size="large" round @click="goCatalog()">
        进入全部商品
      </el-button>
    </section>

    <section class="promise-strip">
      <article>
        <strong>正品保障</strong>
        <span>规格透明可查</span>
      </article>
      <article>
        <strong>极速发货</strong>
        <span>库存实时锁定</span>
      </article>
      <article>
        <strong>安心支付</strong>
        <span>全链路状态追踪</span>
      </article>
      <article>
        <strong>售后服务</strong>
        <span>订单记录可回溯</span>
      </article>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  width: min(var(--eshop-container, 1280px), calc(100% - 40px));
  margin: 0 auto;
  padding: 8px 0 48px;
}

.hero-stage {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 268px;
  gap: 14px;
  margin-bottom: 22px;
  min-height: 360px;
}

.channel-menu {
  display: flex;
  flex-direction: column;
  padding: 16px 12px 12px;
  background: linear-gradient(180deg, #faf6f2 0%, #fff 48%);
  border: 1px solid var(--eshop-border, #ebe4de);
  border-radius: 18px;
  box-shadow: var(--eshop-shadow-sm, 0 4px 16px rgba(28, 20, 16, .04));
}

.channel-menu header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  padding: 0 8px 14px;
  border-bottom: 1px solid #f0ebe6;
  margin-bottom: 8px;
}

.channel-menu header strong {
  color: #1c1917;
  font-size: 15px;
  letter-spacing: .02em;
}

.channel-menu header span {
  color: #b8b0a8;
  font-size: 11px;
}

.channel-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 10px;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 12px;
  cursor: pointer;
  transition: .18s ease;
}

.channel-item:hover {
  background: #fff1f0;
}

.channel-icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  background: #fff;
  border: 1px solid #f0ebe6;
  border-radius: 11px;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(28, 20, 16, .03);
}

.channel-text {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 2px;
}

.channel-text b {
  color: #1c1917;
  font-size: 14px;
  font-weight: 650;
}

.channel-text small {
  color: #8a837c;
  font-size: 11px;
}

.channel-item em {
  color: #d6cfc7;
  font-style: normal;
  font-size: 16px;
}

.channel-all {
  margin-top: auto;
  padding: 11px;
  color: #ff4d4f;
  background: #fff1f0;
  border: 0;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: .18s ease;
}

.channel-all:hover {
  background: #ffe4e1;
}

.hero-banner {
  position: relative;
  min-height: 360px;
  overflow: hidden;
  padding: 40px 40px 56px;
  border-radius: 18px;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 18px 48px rgba(28, 20, 16, .12);
}

.hero-banner.tone-red {
  background:
    radial-gradient(circle at 88% 18%, rgba(255, 255, 255, .22), transparent 30%),
    linear-gradient(128deg, #ff4d4f 0%, #ff6b4a 52%, #e03638 100%);
}

.hero-banner.tone-ink {
  background:
    radial-gradient(circle at 88% 18%, rgba(255, 77, 79, .18), transparent 32%),
    linear-gradient(128deg, #2a2522 0%, #3f3833 55%, #1c1917 100%);
}

.hero-banner.tone-cream {
  color: #1c1917;
  background:
    radial-gradient(circle at 88% 18%, rgba(255, 77, 79, .12), transparent 30%),
    linear-gradient(128deg, #f7efe8 0%, #efe2d6 55%, #e8d5c6 100%);
  box-shadow: 0 18px 48px rgba(28, 20, 16, .08);
}

.hero-banner.tone-dark {
  background:
    radial-gradient(circle at 88% 18%, rgba(255, 107, 74, .28), transparent 32%),
    linear-gradient(128deg, #1c1917 0%, #3a342f 100%);
}

.hero-banner.visual-phone::after,
.hero-banner.visual-computer::after,
.hero-banner.visual-home::after,
.hero-banner.visual-hot::after {
  position: absolute;
  right: 28px;
  bottom: 24px;
  opacity: .14;
  font-size: 108px;
  line-height: 1;
  pointer-events: none;
}

.hero-banner.visual-phone::after { content: "📱"; }
.hero-banner.visual-computer::after { content: "💻"; }
.hero-banner.visual-home::after { content: "🏠"; }
.hero-banner.visual-hot::after { content: "🔥"; }

.hero-badge {
  display: inline-flex;
  padding: 6px 12px;
  background: rgba(255, 255, 255, .18);
  border: 1px solid rgba(255, 255, 255, .28);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .04em;
}

.tone-cream .hero-badge {
  color: #cf1322;
  background: rgba(255, 255, 255, .7);
  border-color: rgba(255, 77, 79, .18);
}

.hero-copy h1 {
  max-width: 440px;
  margin: 16px 0 12px;
  font-size: clamp(30px, 3.8vw, 42px);
  line-height: 1.15;
  letter-spacing: -.03em;
  font-weight: 800;
  text-wrap: balance;
}

.hero-copy > p {
  max-width: 420px;
  margin: 0;
  color: rgba(255, 255, 255, .9);
  font-size: 14px;
  line-height: 1.75;
}

.tone-cream .hero-copy > p {
  color: #5c564f;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
}

.hero-actions :deep(.el-button--primary) {
  --el-button-bg-color: #fff;
  --el-button-border-color: #fff;
  --el-button-text-color: #cf1322;
  --el-button-hover-bg-color: #fff1f0;
  --el-button-hover-border-color: #fff1f0;
  --el-button-hover-text-color: #cf1322;
}

.tone-cream .hero-actions :deep(.el-button--primary) {
  --el-button-bg-color: #ff4d4f;
  --el-button-border-color: #ff4d4f;
  --el-button-text-color: #fff;
  --el-button-hover-bg-color: #ff7875;
  --el-button-hover-border-color: #ff7875;
  --el-button-hover-text-color: #fff;
}

.hero-actions :deep(.el-button.is-plain) {
  --el-button-bg-color: transparent;
  --el-button-border-color: rgba(255, 255, 255, .55);
  --el-button-text-color: #fff;
  --el-button-hover-bg-color: rgba(255, 255, 255, .12);
  --el-button-hover-border-color: #fff;
  --el-button-hover-text-color: #fff;
}

.tone-cream .hero-actions :deep(.el-button.is-plain) {
  --el-button-border-color: rgba(28, 25, 23, .22);
  --el-button-text-color: #1c1917;
  --el-button-hover-bg-color: rgba(28, 25, 23, .04);
  --el-button-hover-border-color: #1c1917;
  --el-button-hover-text-color: #1c1917;
}

.hero-target-tip {
  margin: 16px 0 0 !important;
  color: rgba(255, 255, 255, .72) !important;
  font-size: 12px !important;
}

.tone-cream .hero-target-tip {
  color: #8a837c !important;
}

.hero-arrow {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin: auto 0;
  padding: 0;
  color: #fff;
  background: rgba(28, 25, 23, .32);
  border: 1px solid rgba(255, 255, 255, .4);
  border-radius: 50%;
  cursor: pointer;
  backdrop-filter: blur(6px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, .12);
  transition: opacity .2s ease, background .18s ease, transform .15s ease, border-color .18s ease;
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}

/* 仅鼠标悬停在广告区域时显示箭头 */
.hero-banner:hover .hero-arrow {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}

.hero-arrow:hover {
  background: #ff4d4f;
  border-color: #ff4d4f;
  transform: scale(1.06);
}

.hero-arrow:active {
  transform: scale(0.96);
}

.hero-arrow-prev {
  left: 16px;
}

.hero-arrow-next {
  right: 16px;
}

/* 用边框画居中箭头，避免字符基线偏移 */
.hero-arrow-icon {
  display: block;
  width: 10px;
  height: 10px;
  border-top: 2px solid currentColor;
  border-right: 2px solid currentColor;
  box-sizing: border-box;
}

.hero-arrow-prev .hero-arrow-icon {
  margin-left: 3px;
  transform: rotate(-135deg);
}

.hero-arrow-next .hero-arrow-icon {
  margin-right: 3px;
  transform: rotate(45deg);
}

.tone-cream .hero-arrow {
  color: #1c1917;
  background: rgba(255, 255, 255, .82);
  border-color: rgba(28, 25, 23, .1);
}

.tone-cream .hero-arrow:hover {
  color: #fff;
  background: #ff4d4f;
  border-color: #ff4d4f;
}

.hero-dots {
  position: absolute;
  left: 40px;
  bottom: 20px;
  display: flex;
  gap: 8px;
}

.hero-dots button {
  width: 22px;
  height: 4px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, .35);
  cursor: pointer;
  transition: .18s ease;
}

.tone-cream .hero-dots button {
  background: rgba(28, 25, 23, .18);
}

.hero-dots button.active {
  width: 34px;
  background: #fff;
}

.tone-cream .hero-dots button.active {
  background: #ff4d4f;
}

.hero-side {
  display: grid;
  grid-template-rows: 1fr 1fr;
  gap: 14px;
  min-height: 0;
}

.side-card {
  min-height: 0;
  padding: 16px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--eshop-border, #ebe4de);
  border-radius: 18px;
  box-shadow: var(--eshop-shadow-sm, 0 4px 16px rgba(28, 20, 16, .04));
}

.side-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.side-heading h3 {
  margin: 0;
  font-size: 15px;
  letter-spacing: .01em;
}

.side-heading span,
.linkish {
  color: #ff4d4f;
  background: none;
  border: 0;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}

.deal-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  margin-bottom: 8px;
  padding: 7px;
  text-align: left;
  background: #faf7f5;
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: .18s ease;
}

.deal-row:last-child {
  margin-bottom: 0;
}

.deal-row:hover {
  border-color: #ffd6d2;
  background: #fff1f0;
}

.deal-thumb {
  display: grid;
  flex: 0 0 46px;
  width: 46px;
  height: 46px;
  place-items: center;
  overflow: hidden;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #f0ebe6;
}

.deal-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.deal-thumb em {
  color: #ffccc7;
  font-size: 9px;
  font-style: normal;
  font-weight: 800;
}

.deal-info {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 2px;
}

.deal-info b {
  overflow: hidden;
  color: #1c1917;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deal-info small {
  color: #8a837c;
  font-size: 11px;
}

.deal-info strong {
  color: #ff4d4f;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.rank-no {
  display: grid;
  flex: 0 0 22px;
  width: 22px;
  height: 22px;
  place-items: center;
  color: #fff;
  background: #c4bbb2;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
}

.rank-no.top {
  background: #ff4d4f;
}

.side-empty {
  margin: 18px 0 0;
  color: #b8b0a8;
  font-size: 12px;
  text-align: center;
}

.hot-anchor {
  scroll-margin-top: 140px;
}

.home-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin: 10px 0 24px;
  padding: 30px 34px;
  background:
    radial-gradient(circle at 100% 0%, rgba(255, 77, 79, .08), transparent 36%),
    #fff;
  border: 1px solid var(--eshop-border, #ebe4de);
  border-radius: 18px;
  box-shadow: var(--eshop-shadow-sm, 0 4px 16px rgba(28, 20, 16, .04));
}

.home-entry .eyebrow {
  margin: 0 0 6px;
  color: #ff4d4f;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .14em;
}

.home-entry h2 {
  margin: 0 0 8px;
  font-size: 26px;
  letter-spacing: -.02em;
}

.home-entry p {
  margin: 0;
  color: #8a837c;
  font-size: 14px;
}

.promise-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  padding: 8px 6px;
  background: #fff;
  border: 1px solid var(--eshop-border, #ebe4de);
  border-radius: 18px;
  box-shadow: var(--eshop-shadow-sm, 0 4px 16px rgba(28, 20, 16, .04));
  overflow: hidden;
}

.promise-strip article {
  display: grid;
  gap: 4px;
  padding: 18px 20px;
  border-right: 1px solid #f0ebe6;
}

.promise-strip article:last-child {
  border-right: 0;
}

.promise-strip strong {
  color: #1c1917;
  font-size: 14px;
}

.promise-strip span {
  color: #8a837c;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .hero-stage {
    grid-template-columns: 1fr 1fr;
  }

  .channel-menu {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 6px;
  }

  .channel-menu header,
  .channel-all {
    grid-column: 1 / -1;
  }

  .hero-banner {
    grid-column: 1 / -1;
  }

  .hero-side {
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: none;
  }
}

@media (max-width: 760px) {
  .hero-banner {
    min-height: 250px;
    padding: 28px 22px 70px;
  }

  .hero-side,
  .channel-menu {
    grid-template-columns: 1fr;
  }

  .home-entry {
    flex-direction: column;
    align-items: flex-start;
  }

  .promise-strip {
    grid-template-columns: 1fr 1fr;
  }

  .promise-strip article {
    border-right: 0;
    border-bottom: 1px solid #f0ebe6;
  }
}
</style>
