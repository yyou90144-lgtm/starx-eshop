<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getDashboardSalesTrend,
  getDashboardSummary,
  getDashboardTopProducts,
} from '../../api/admin'
import { getHotProducts } from '../../api/catalog'
import { formatDateTime, formatMoney } from '../../utils/shop'

const loading = ref(false)
const errorMessage = ref('')
const summary = ref(null)
const hotLoading = ref(false)
const hotProducts = ref([])
const trendLoading = ref(false)
const salesTrend = ref([])
const trendError = ref('')
const topLoading = ref(false)
const topProducts = ref([])
const topError = ref('')
const hotError = ref('')

const maxTrendOrders = computed(() => (
  Math.max(1, ...salesTrend.value.map((point) => Number(point.orderCount) || 0), 1)
))

const numericValue = (...values) => {
  for (const value of values) {
    const number = Number(value)
    if (Number.isFinite(number)) return number
  }
  return 0
}

const normalizeHotProduct = (item) => ({
  ...item,
  productId: item.productId ?? item.id,
  salesQuantity: numericValue(item.salesQuantity, item.soldQuantity, item.quantity),
  salesAmount: numericValue(item.salesAmount, item.totalSalesAmount, item.amount),
  totalStock: numericValue(item.totalStock, item.stock),
})

const loadSummary = async () => {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getDashboardSummary()
  } catch (error) {
    errorMessage.value = error.message || '运营数据加载失败'
  } finally {
    loading.value = false
  }
}

const loadSalesTrend = async () => {
  if (trendLoading.value) return
  trendLoading.value = true
  trendError.value = ''
  try {
    const data = await getDashboardSalesTrend(7)
    salesTrend.value = Array.isArray(data?.points) ? data.points : []
  } catch (error) {
    salesTrend.value = []
    trendError.value = error.message || '销售趋势加载失败'
  } finally {
    trendLoading.value = false
  }
}

const loadTopProducts = async () => {
  if (topLoading.value) return
  topLoading.value = true
  topError.value = ''
  try {
    const data = await getDashboardTopProducts(10)
    topProducts.value = Array.isArray(data?.items) ? data.items : []
  } catch (error) {
    topProducts.value = []
    topError.value = error.message || '销量排行加载失败'
  } finally {
    topLoading.value = false
  }
}

const loadHotProducts = async () => {
  if (hotLoading.value) return
  hotLoading.value = true
  hotError.value = ''
  try {
    const data = await getHotProducts({ days: 30, limit: 5 })
    hotProducts.value = (Array.isArray(data) ? data : []).map(normalizeHotProduct)
  } catch (error) {
    hotProducts.value = []
    hotError.value = error.message || '热销商品加载失败'
  } finally {
    hotLoading.value = false
  }
}

const overviewCards = computed(() => [
  {
    label: '买家总数',
    value: summary.value?.userCount ?? 0,
    note: '当前商城消费者账号',
    tone: 'blue',
  },
  {
    label: '商品总数',
    value: summary.value?.productCount ?? 0,
    note: `其中 ${summary.value?.onSaleProductCount ?? 0} 件正在销售`,
    tone: 'violet',
  },
  {
    label: '订单总数',
    value: summary.value?.orderCount ?? 0,
    note: '包含全部订单状态',
    tone: 'amber',
  },
  {
    label: '低库存 SKU',
    value: summary.value?.lowStockSkuCount ?? 0,
    note: '在售且库存不高于 10 件',
    tone: 'red',
  },
  {
    label: '有效销售额',
    value: formatMoney(summary.value?.paidSalesAmount),
    note: '已支付、已发货和已完成订单',
    tone: 'emerald',
  },
])

const priorityActions = computed(() => [
  {
    label: '确认待发货订单',
    value: summary.value?.paidOrderCount ?? 0,
    note: '支付已完成，订单可以进入发货处理。',
    action: '处理订单',
    to: { path: '/seller/orders', query: { status: 'PAID' } },
    tone: 'primary',
  },
  {
    label: '检查低库存 SKU',
    value: summary.value?.lowStockSkuCount ?? 0,
    note: '只显示在售且启用的 SKU。',
    action: '查看预警',
    to: '/seller/inventory',
    tone: 'warning',
  },
  {
    label: '跟进运输中订单',
    value: summary.value?.shippedOrderCount ?? 0,
    note: '等待消费者确认收货。',
    action: '查看订单',
    to: { path: '/seller/orders', query: { status: 'SHIPPED' } },
    tone: 'neutral',
  },
])

const orderStatuses = computed(() => [
  {
    label: '待支付',
    value: summary.value?.pendingPaymentOrderCount ?? 0,
    className: 'warning',
  },
  {
    label: '待发货',
    value: summary.value?.paidOrderCount ?? 0,
    className: 'primary',
  },
  {
    label: '待收货',
    value: summary.value?.shippedOrderCount ?? 0,
    className: 'success',
  },
  {
    label: '已完成',
    value: summary.value?.completedOrderCount ?? 0,
    className: 'completed',
  },
  {
    label: '已取消',
    value: summary.value?.canceledOrderCount ?? 0,
    className: 'muted',
  },
])

const refreshAll = () => {
  loadSummary()
  loadSalesTrend()
  loadTopProducts()
  loadHotProducts()
}

onMounted(refreshAll)
</script>

<template>
  <section class="admin-dashboard">
    <header class="dashboard-hero">
      <div>
        <h1>商城运营控制台</h1>
        <span>实时查看商城买家、商品、订单与有效销售额，快速进入日常运营模块。</span>
      </div>
      <div class="hero-actions">
        <small v-if="summary?.generatedAt">
          更新于 {{ formatDateTime(summary.generatedAt) }}
        </small>
        <el-button
          type="primary"
          plain
          :loading="loading || trendLoading || topLoading"
          @click="refreshAll"
        >
          刷新数据
        </el-button>
      </div>
    </header>

    <el-alert
      v-if="errorMessage"
      class="dashboard-error"
      type="error"
      :title="errorMessage"
      :closable="false"
      show-icon
    >
      <template #default>
        <el-button link type="primary" @click="loadSummary">重新加载</el-button>
      </template>
    </el-alert>

    <section class="action-queue" :aria-busy="loading">
      <div class="section-heading">
        <div>
          <h2>需要处理</h2>
          <span>优先处理会影响履约与库存的事项。</span>
        </div>
      </div>
      <div class="queue-grid">
        <RouterLink
          v-for="item in priorityActions"
          :key="item.label"
          :to="item.to"
          class="queue-item"
          :class="`queue-${item.tone}`"
        >
          <div>
            <span>{{ item.label }}</span>
            <small>{{ item.note }}</small>
          </div>
          <strong>{{ loading && !summary ? '-' : item.value }}</strong>
          <em>{{ item.action }}</em>
        </RouterLink>
      </div>
    </section>

    <div class="overview-grid" :aria-busy="loading">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="overview-card"
        :class="`tone-${card.tone}`"
      >
        <span>{{ card.label }}</span>
        <strong>{{ loading && !summary ? '—' : card.value }}</strong>
        <small>{{ card.note }}</small>
      </article>
    </div>

    <section class="order-overview">
      <div class="section-heading">
        <div>
          <h2>订单状态分布</h2>
        </div>
        <RouterLink to="/seller/orders">处理订单</RouterLink>
      </div>
      <div class="status-grid">
        <div
          v-for="status in orderStatuses"
          :key="status.label"
          class="status-item"
          :class="status.className"
        >
          <span>{{ status.label }}</span>
          <strong>{{ loading && !summary ? '—' : status.value }}</strong>
        </div>
      </div>
    </section>

    <section class="trend-overview">
      <div class="section-heading">
        <div>
          <h2>近 7 日销售趋势</h2>
        </div>
      </div>
      <div v-loading="trendLoading" class="trend-table">
        <div v-if="!trendLoading && trendError" class="compact-empty compact-error">
          <span>{{ trendError }}</span>
          <el-button link type="primary" @click="loadSalesTrend">重新加载</el-button>
        </div>
        <div v-for="point in salesTrend" :key="point.date" class="trend-row">
          <span class="trend-date">{{ point.date }}</span>
          <div class="trend-bar-wrap" :title="`订单 ${point.orderCount}`">
            <i :style="{ width: `${Math.max(6, (Number(point.orderCount) / maxTrendOrders) * 100)}%` }" />
          </div>
          <span><small>订单</small><strong>{{ point.orderCount }}</strong></span>
          <span><small>销售额</small><strong>{{ formatMoney(point.salesAmount) }}</strong></span>
        </div>
        <div v-if="!trendLoading && !trendError && !salesTrend.length" class="compact-empty">暂无趋势数据</div>
      </div>
    </section>

    <section class="hot-overview">
      <div class="section-heading">
        <div>
          <h2>销量 Top 商品</h2>
        </div>
        <RouterLink to="/seller/products">管理商品</RouterLink>
      </div>
      <div v-loading="topLoading" class="hot-table">
        <div v-if="!topLoading && topError" class="compact-empty compact-error">
          <span>{{ topError }}</span>
          <el-button link type="primary" @click="loadTopProducts">重新加载</el-button>
        </div>
        <div v-for="(product, index) in topProducts" :key="product.productId" class="hot-row">
          <b>{{ index + 1 }}</b>
          <span>
            <strong>{{ product.productName }}</strong>
            <small>商品编号 {{ product.productId }}</small>
          </span>
          <span><small>销量</small><strong>{{ product.soldQuantity }} 件</strong></span>
          <span><small>销售额</small><strong>{{ formatMoney(product.salesAmount) }}</strong></span>
        </div>
        <div v-if="!topLoading && !topError && !topProducts.length" class="compact-empty">暂无销量排行</div>
      </div>
    </section>

    <section class="hot-overview">
      <div class="section-heading">
        <div>
          <h2>近 30 天热销商品</h2>
        </div>
        <RouterLink to="/seller/products">管理商品</RouterLink>
      </div>
      <div v-loading="hotLoading" class="hot-table">
        <div v-if="!hotLoading && hotError" class="compact-empty compact-error">
          <span>{{ hotError }}</span>
          <el-button link type="primary" @click="loadHotProducts">重新加载</el-button>
        </div>
        <div v-for="(product, index) in hotProducts" :key="product.productId" class="hot-row">
          <b>{{ index + 1 }}</b>
          <span>
            <strong>{{ product.name }}</strong>
            <small>{{ product.subtitle || '暂无副标题' }}</small>
          </span>
          <span><small>有效销量</small><strong>{{ product.salesQuantity }} 件</strong></span>
          <span><small>销售额</small><strong>{{ formatMoney(product.salesAmount) }}</strong></span>
          <span><small>可售库存</small><strong>{{ product.totalStock }}</strong></span>
        </div>
        <div v-if="!hotLoading && !hotError && !hotProducts.length" class="compact-empty">暂无有效销售数据</div>
      </div>
    </section>

    <section class="quick-entry">
      <div class="section-heading">
        <div>
          <h2>快捷管理</h2>
        </div>
      </div>
      <div class="entry-grid">
        <RouterLink to="/seller/categories">
          <h3>分类管理</h3><p>维护分类结构、排序与启用状态。</p><span>进入管理</span>
        </RouterLink>
        <RouterLink to="/seller/products">
          <h3>商品与 SKU</h3><p>维护商品、规格、价格与库存。</p><span>进入管理</span>
        </RouterLink>
        <RouterLink to="/seller/inventory">
          <h3>库存预警</h3><p>定位在售商品的低库存和缺货 SKU。</p><span>查看预警</span>
        </RouterLink>
        <RouterLink to="/seller/orders">
          <h3>订单管理</h3><p>查询订单并完成发货与取消操作。</p><span>进入管理</span>
        </RouterLink>
        <RouterLink to="/seller/logs">
          <h3>操作日志</h3><p>审计商家工作台关键业务操作结果。</p><span>查看日志</span>
        </RouterLink>
        <RouterLink to="/seller/reviews">
          <h3>评价管理</h3><p>查看消费者评价并处理不适合公开展示的内容。</p><span>审核评价</span>
        </RouterLink>
      </div>
    </section>
  </section>
</template>

<style scoped>
.admin-dashboard {
  max-width: 1240px;
  margin: 0 auto;
}

.trend-overview {
  margin: 22px 0;
  padding: 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
}

/* The trend is a compact factual comparison, not a decorative chart treatment. */
.trend-bar-wrap {
  height: 4px;
  background: transparent;
  border-radius: 2px;
}

.trend-bar-wrap i {
  background: #e5484d;
  border-radius: inherit;
}

.trend-table {
  display: grid;
  gap: 12px;
}

.trend-row {
  display: grid;
  grid-template-columns: 110px minmax(120px, 1fr) 90px 120px;
  gap: 14px;
  align-items: center;
}

.trend-date {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.trend-bar-wrap {
  height: 10px;
  overflow: hidden;
  background: #e2e8f0;
  border-radius: 999px;
}

.trend-bar-wrap i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
  border-radius: inherit;
}

.trend-row small {
  display: block;
  color: #94a3b8;
  font-size: 11px;
}

.trend-row strong {
  color: #0f172a;
  font-size: 14px;
}

.dashboard-hero h1 {
  margin: 0 0 10px;
  font-size: 32px;
}

.dashboard-hero span {
  color: #cbd5e1;
}

.hero-actions {
  display: flex;
  flex: 0 0 auto;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.hero-actions small {
  color: #bfdbfe;
}

.dashboard-error {
  margin-top: 18px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.overview-card {
  position: relative;
  overflow: hidden;
  padding: 22px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
}

.overview-card::after {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 90px;
  height: 90px;
  background: currentColor;
  border-radius: 50%;
  content: "";
  opacity: 0.08;
}

.overview-card > span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.overview-card strong {
  display: block;
  margin: 12px 0 8px;
  color: #0f172a;
  font-size: 30px;
  line-height: 1;
}

.overview-card small {
  color: #94a3b8;
  line-height: 1.5;
}

.tone-blue { color: #2563eb; }
.tone-violet { color: #7c3aed; }
.tone-amber { color: #d97706; }
.tone-red { color: #dc2626; }
.tone-emerald { color: #059669; }

.order-overview,
.hot-overview,
.quick-entry {
  margin-top: 20px;
  padding: 24px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.hot-table {
  min-height: 0;
}

.compact-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 52px;
  padding: 0 10px;
  color: #68788e;
  font-size: 13px;
}

.compact-error span {
  color: #b42318;
}

.hot-row {
  display: grid;
  grid-template-columns: 42px minmax(240px, 1fr) 120px 140px 100px;
  align-items: center;
  gap: 16px;
  padding: 13px 14px;
  border-bottom: 1px solid #f1f5f9;
}

.hot-row:last-child {
  border-bottom: 0;
}

.hot-row > b {
  display: grid;
  width: 30px;
  height: 30px;
  color: #fff;
  background: #f97316;
  border-radius: 50%;
  place-items: center;
}

.hot-row > span {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.hot-row strong {
  overflow: hidden;
  color: #0f172a;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-row small {
  overflow: hidden;
  color: #94a3b8;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.section-heading h2 {
  margin: 0;
  color: #0f172a;
  font-size: 21px;
}

.section-heading a {
  color: #2563eb;
  font-weight: 700;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.status-item {
  padding: 16px;
  background: #f8fafc;
  border-left: 4px solid currentColor;
  border-radius: 10px;
}

.status-item span {
  color: #64748b;
  font-size: 13px;
}

.status-item strong {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 24px;
}

.status-item.warning { color: #f59e0b; }
.status-item.primary { color: #3b82f6; }
.status-item.success { color: #10b981; }
.status-item.completed { color: #0f766e; }
.status-item.muted { color: #94a3b8; }

.entry-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.entry-grid a {
  min-height: 170px;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  transition: 0.2s ease;
}

.entry-grid a:hover {
  background: #fff;
  border-color: #60a5fa;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.1);
  transform: translateY(-2px);
}

.entry-grid h3 {
  margin: 10px 0 6px;
  color: #0f172a;
}

.entry-grid p {
  min-height: 42px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.55;
}

.entry-grid span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
}

/* The workbench keeps current actions in a single scan line before reporting. */
.dashboard-hero {
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 0 20px;
  color: #16243a;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.dashboard-hero h1 {
  margin: 0 0 6px;
  color: #16243a;
  font-size: 26px;
  line-height: 1.25;
}

.dashboard-hero span {
  color: #68788e;
  font-size: 13px;
}

.hero-actions {
  align-items: flex-end;
}

.hero-actions small {
  color: #68788e;
}

.action-queue {
  margin-bottom: 18px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #dce5ef;
  border-radius: 10px;
}

.action-queue .section-heading {
  align-items: flex-end;
  margin: 0;
  padding: 18px 20px 12px;
}

.action-queue .section-heading h2 {
  margin-bottom: 3px;
  font-size: 18px;
}

.action-queue .section-heading span {
  color: #68788e;
  font-size: 13px;
}

.queue-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  border-top: 1px solid #e9eef4;
}

.queue-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px 16px;
  min-height: 106px;
  padding: 18px 20px;
  color: #16243a;
  border-right: 1px solid #e9eef4;
  transition: background-color .18s ease;
}

.queue-item:last-child {
  border-right: 0;
}

.queue-item:hover {
  background: #fff7f6;
}

.queue-item > div {
  display: grid;
  gap: 5px;
}

.queue-item span {
  font-size: 14px;
  font-weight: 800;
}

.queue-item small {
  color: #68788e;
  font-size: 12px;
  line-height: 1.5;
}

.queue-item strong {
  align-self: start;
  color: #16243a;
  font-size: 28px;
  line-height: 1;
}

.queue-item em {
  grid-column: 1 / -1;
  color: #ff4d4f;
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.queue-warning strong {
  color: #b7791f;
}

.overview-grid {
  gap: 0;
  margin-top: 0;
  overflow: hidden;
  background: #fff;
  border: 1px solid #dce5ef;
  border-radius: 10px;
}

.overview-card {
  min-height: 124px;
  padding: 20px;
  background: transparent;
  border: 0;
  border-right: 1px solid #e9eef4;
  border-radius: 0;
  box-shadow: none;
}

.overview-card:last-child {
  border-right: 0;
}

.overview-card::after {
  display: none;
}

.overview-card > span {
  color: #68788e;
  font-size: 12px;
}

.overview-card strong {
  margin: 11px 0 7px;
  color: #16243a;
  font-size: 27px;
}

.overview-card small {
  color: #68788e;
  font-size: 12px;
}

.tone-red,
.tone-amber {
  color: #ff4d4f;
}

.tone-blue,
.tone-violet {
  color: #746f68;
}

.tone-amber {
  color: #b87712;
}

.tone-red {
  color: #e5484d;
}

.tone-emerald {
  color: #38845b;
}

.order-overview,
.trend-overview,
.hot-overview,
.quick-entry {
  margin-top: 18px;
  padding: 20px;
  border-color: #dce5ef;
  border-radius: 10px;
  box-shadow: none;
}

.section-heading {
  align-items: flex-start;
  margin-bottom: 14px;
}

.section-heading h2 {
  color: #16243a;
  font-size: 18px;
}

.section-heading a {
  color: #ff4d4f;
  font-size: 13px;
}

.status-grid {
  gap: 0;
  border-top: 1px solid #e9eef4;
}

.status-item {
  padding: 15px 16px;
  background: transparent;
  border-left: 0;
  border-right: 1px solid #e9eef4;
  border-radius: 0;
}

.status-item:last-child {
  border-right: 0;
}

.status-item span {
  color: #68788e;
  font-size: 12px;
}

.status-item strong {
  margin-top: 6px;
  color: #16243a;
  font-size: 22px;
}

.status-item.primary {
  color: #e5484d;
}

.hot-row {
  padding: 13px 10px;
  border-color: #e9eef4;
}

.hot-row > b {
  width: 26px;
  height: 26px;
  color: #ff4d4f;
  background: #fff1f0;
  border-radius: 6px;
}

.entry-grid {
  gap: 0;
  overflow: hidden;
  border: 1px solid #e9eef4;
  border-radius: 8px;
}

.entry-grid a {
  min-height: 150px;
  padding: 18px;
  background: #fff;
  border: 0;
  border-right: 1px solid #e9eef4;
  border-bottom: 1px solid #e9eef4;
  border-radius: 0;
}

.entry-grid a:hover {
  background: #fff7f6;
  border-color: #e9eef4;
  box-shadow: none;
  transform: none;
}

.entry-grid h3 {
  color: #16243a;
}

.entry-grid p {
  color: #68788e;
}

.entry-grid span {
  color: #ff4d4f;
}

.action-queue,
.overview-grid,
.order-overview,
.trend-overview,
.hot-overview,
.quick-entry,
.entry-grid {
  border-color: #e2e1dd;
}

.queue-grid,
.status-grid {
  border-color: #e2e1dd;
}

.queue-item,
.overview-card,
.status-item,
.entry-grid a {
  border-color: #e2e1dd;
}

.queue-item:hover,
.entry-grid a:hover {
  background: #fff5f4;
  border-color: #e2e1dd;
}

.hot-row {
  border-color: #efefeb;
}

.hot-row > b {
  color: #c83b40;
  background: #fff1f0;
}

.trend-bar-wrap {
  height: 4px;
  background: transparent;
  border-radius: 2px;
}

.trend-bar-wrap i {
  background: #e5484d;
  border-radius: inherit;
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .queue-grid {
    grid-template-columns: 1fr;
  }

  .queue-item,
  .queue-item:last-child {
    min-height: auto;
    border-right: 0;
    border-bottom: 1px solid #e9eef4;
  }

  .queue-item:last-child {
    border-bottom: 0;
  }
}

@media (max-width: 760px) {
  .dashboard-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-actions {
    align-items: flex-start;
  }

  .overview-grid,
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .status-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hot-row {
    grid-template-columns: 36px minmax(180px, 1fr) 95px;
  }

  .hot-row > span:nth-last-child(-n + 2) {
    display: none;
  }
}
</style>
