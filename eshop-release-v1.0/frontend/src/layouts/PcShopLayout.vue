<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartBadgeStore } from '../stores/cartBadge'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cartBadgeStore = useCartBadgeStore()

const keyword = ref('')
const cartBadge = computed(() => cartBadgeStore.badge)

const search = async () => {
  const value = keyword.value.trim()
  await router.push({
    name: 'pc-products',
    query: value ? { keyword: value } : {},
  })
}

const logout = async () => {
  await auth.signOut()
  cartBadgeStore.reset()
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
  cartBadgeStore.ensureListener()
  void cartBadgeStore.refresh()
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
    void cartBadgeStore.refresh()
  },
)
</script>

<template>
  <!--
    THESIS: STARX is a retail service desk where product truth stays visible.
    OWN-WORLD: cool white surfaces, deep navy structure, one red action color.
    STORY: shoppers move from a category signal to a product decision and checkout.
    FIRST VIEWPORT: compact utility bar, one-line search header, category navigation.
    FORM: navigation-grade retail shelf, assigned direction seed 78798ae1.
    FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, and DESIGN.md
  -->
  <div class="pc-shell">
    <div class="pc-topbar">
      <div class="pc-topbar-inner">
        <span>SKU 规格 · 库存状态 · 订单进度</span>
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
            <small>STARX · 商品选购与订单跟踪</small>
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
            <span class="label">购物车</span>
            <i v-if="cartBadge" class="pc-cart-badge">{{ cartBadge }}</i>
          </RouterLink>
          <RouterLink
            v-if="auth.isLoggedIn"
            class="pc-action-btn"
            to="/pc/profile"
          >
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
              提供商品浏览、SKU 选择、购物车下单、模拟支付与订单进度查看。
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
            <h4>功能一览</h4>
            <p>SKU 规格 · 库存状态</p>
            <p>购物车下单 · 模拟支付</p>
            <p>订单进度 · 商品评价</p>
          </div>
        </div>
        <div class="pc-footer-bottom">
          <span>© {{ new Date().getFullYear() }} 星选商城 STARX. All rights reserved.</span>
          <div class="ops-links">
            <RouterLink to="/seller/login">商家工作台</RouterLink>
            <RouterLink to="/admin/login">平台管理</RouterLink>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>
