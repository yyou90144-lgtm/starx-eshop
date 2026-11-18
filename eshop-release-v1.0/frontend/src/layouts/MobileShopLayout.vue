<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartBadgeStore } from '../stores/cartBadge'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cartBadgeStore = useCartBadgeStore()
const loggingOut = ref(false)

const pageTitle = computed(() => route.meta.title || 'E-Shop')
const showTabbar = computed(() => Boolean(route.meta.mobileTabbar))
const cartBadge = computed(() => cartBadgeStore.badge)
const loginTarget = computed(() => ({
  name: 'mobile-login',
  query: { redirect: route.fullPath },
}))

const keepAliveViews = ['MobileProductListView', 'MobileOrdersView']

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.replace({ name: 'mobile-products' })
}

const logout = async () => {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await auth.signOut()
    cartBadgeStore.reset()
    await router.replace({ name: 'mobile-login' })
  } finally {
    loggingOut.value = false
  }
}

onMounted(() => {
  cartBadgeStore.ensureListener()
  void cartBadgeStore.refresh()
})

watch(
  () => auth.token,
  () => {
    void cartBadgeStore.refresh()
  },
)
</script>

<template>
  <div class="mobile-shell">
    <van-nav-bar
      :title="pageTitle"
      :left-arrow="!showTabbar"
      fixed
      placeholder
      safe-area-inset-top
      @click-left="goBack"
    >
      <template #right>
        <div class="mobile-nav-actions">
          <van-button
            v-if="auth.isLoggedIn"
            :loading="loggingOut"
            size="small"
            type="primary"
            plain
            hairline
            @click="logout"
          >
            退出
          </van-button>
          <RouterLink v-else class="mobile-login-link" :to="loginTarget">登录</RouterLink>
        </div>
      </template>
    </van-nav-bar>
    <main class="mobile-main">
      <RouterView v-slot="{ Component }">
        <KeepAlive :include="keepAliveViews">
          <component :is="Component" />
        </KeepAlive>
      </RouterView>
    </main>
    <van-tabbar
      v-if="showTabbar"
      class="mobile-tabbar"
      route
      fixed
      placeholder
      safe-area-inset-bottom
    >
      <van-tabbar-item replace to="/m/products" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/m/cart" icon="cart-o" :badge="cartBadge">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/m/orders" icon="orders-o">订单</van-tabbar-item>
      <van-tabbar-item replace to="/m/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>
