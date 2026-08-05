<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCart } from '../api/cart'
import { useAuthStore } from '../stores/auth'
import { CART_UPDATED_EVENT } from '../utils/cartBadge'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const keyword = ref('')
const cartCount = ref(0)
let cartRequestSequence = 0

const cartBadge = computed(() => {
  if (cartCount.value <= 0) return ''
  return cartCount.value > 99 ? '99+' : String(cartCount.value)
})

const loadCartCount = async () => {
  const requestId = ++cartRequestSequence
  if (!auth.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const items = await getCart()
    if (requestId !== cartRequestSequence || !auth.isLoggedIn) return
    cartCount.value = (Array.isArray(items) ? items : []).reduce(
      (total, item) => total + Math.max(0, Number(item.quantity) || 0),
      0,
    )
  } catch {
    if (requestId === cartRequestSequence) cartCount.value = 0
  }
}

const search = async () => {
  const value = keyword.value.trim()
  await router.push({
    name: 'pc-products',
    query: value ? { keyword: value } : {},
  })
}

const logout = async () => {
  await auth.signOut()
  cartCount.value = 0
  router.push({ name: 'pc-home' })
}

const goLogin = () => {
  router.push({
    name: 'pc-login',
    query: { redirect: route.fullPath },
  })
}

onMounted(() => {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  void loadCartCount()
  window.addEventListener(CART_UPDATED_EVENT, loadCartCount)
})

onUnmounted(() => {
  cartRequestSequence += 1
  window.removeEventListener(CART_UPDATED_EVENT, loadCartCount)
})

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : ''
  },
)

watch(
  () => auth.token,
  () => {
    void loadCartCount()
  },
)
</script>

<template>
  <div class="pc-shell">
    <div class="pc-topbar">
      <div class="pc-topbar-inner">
        <span>正品保障 · 极速发货 · 售后可追溯</span>
        <div class="pc-topbar-links">
          <template v-if="auth.isLoggedIn">
            <span>你好，{{ auth.user?.nickname || auth.user?.username }}</span>
            <span class="divider" />
            <a href="javascript:;" @click.prevent="logout">退出登录</a>
          </template>
          <template v-else>
            <a href="javascript:;" @click.prevent="goLogin">请登录</a>
            <span class="divider" />
            <RouterLink :to="{ name: 'pc-login', query: { mode: 'register' } }">免费注册</RouterLink>
          </template>
          <span class="divider" />
          <RouterLink to="/pc/orders">我的订单</RouterLink>
          <RouterLink to="/pc/favorites">我的收藏</RouterLink>
          <RouterLink to="/m">手机版</RouterLink>
        </div>
      </div>
    </div>

    <header class="pc-header-main">
      <div class="pc-header-inner">
        <RouterLink class="pc-brand" :to="{ name: 'pc-home' }">
          <span class="pc-brand-mark">星</span>
          <span class="pc-brand-text">
            <strong>星选商城</strong>
            <small>STARX · 品质生活一站购</small>
          </span>
        </RouterLink>

        <form class="pc-search" @submit.prevent="search">
          <el-input
            v-model="keyword"
            clearable
            maxlength="120"
            placeholder="搜索商品、品牌或关键词"
            @clear="search"
          />
          <button class="pc-search-btn" type="submit">搜索</button>
        </form>

        <div class="pc-header-actions">
          <RouterLink class="pc-action-btn" to="/pc/cart">
            <span aria-hidden="true">🛒</span>
            <span class="label">购物车</span>
            <i v-if="cartBadge" class="pc-cart-badge">{{ cartBadge }}</i>
          </RouterLink>
          <RouterLink
            v-if="auth.isLoggedIn"
            class="pc-action-btn"
            to="/pc/profile"
          >
            <span aria-hidden="true">👤</span>
            <span class="label">我的</span>
          </RouterLink>
          <button
            v-else
            type="button"
            class="pc-action-btn primary"
            @click="goLogin"
          >
            <span class="label">登录 / 注册</span>
          </button>
        </div>
      </div>

      <nav class="pc-nav">
        <div class="pc-nav-inner">
          <!-- exact-active：避免 /pc/products 时父路径 /pc 也把「首页」点亮 -->
          <RouterLink
            :to="{ name: 'pc-home' }"
            active-class=""
            exact-active-class="router-link-active"
          >
            首页
          </RouterLink>
          <RouterLink
            :to="{ name: 'pc-products' }"
            active-class=""
            :class="{ 'router-link-active': route.name === 'pc-products' }"
          >
            全部商品
          </RouterLink>
          <RouterLink to="/pc/favorites">收藏夹</RouterLink>
          <RouterLink to="/pc/history">浏览足迹</RouterLink>
          <RouterLink to="/pc/orders">订单中心</RouterLink>
          <RouterLink to="/pc/addresses">收货地址</RouterLink>
          <RouterLink to="/pc/reviews">我的评价</RouterLink>
        </div>
      </nav>
    </header>

    <main class="pc-main">
      <RouterView />
    </main>

    <footer class="pc-footer">
      <div class="pc-footer-inner">
        <div class="pc-footer-grid">
          <div>
            <div class="pc-footer-brand">
              <span>星</span>
              星选商城
            </div>
            <p>
              面向品质生活的新一代购物平台。支持商品浏览、多规格选购、购物车下单、
              模拟支付与订单全链路追踪。
            </p>
          </div>
          <div>
            <h4>购物指南</h4>
            <p><RouterLink :to="{ name: 'pc-products' }">全部商品</RouterLink></p>
            <p><RouterLink to="/pc/cart">购物车结算</RouterLink></p>
            <p><RouterLink to="/pc/orders">查看订单</RouterLink></p>
          </div>
          <div>
            <h4>会员服务</h4>
            <p><RouterLink to="/pc/profile">个人中心</RouterLink></p>
            <p><RouterLink to="/pc/favorites">我的收藏</RouterLink></p>
            <p><RouterLink to="/pc/addresses">地址管理</RouterLink></p>
          </div>
          <div>
            <h4>服务承诺</h4>
            <p>正品保障 · 极速发货</p>
            <p>七天无忧 · 售后可追溯</p>
            <p>库存实时 · 安全支付</p>
          </div>
        </div>
        <div class="pc-footer-bottom">
          <span>© {{ new Date().getFullYear() }} 星选商城 STARX. All rights reserved.</span>
          <div class="ops-links">
            <RouterLink to="/seller/login">商家入驻</RouterLink>
            <RouterLink to="/admin/login">平台管理</RouterLink>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>
