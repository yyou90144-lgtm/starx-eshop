import { defineStore } from 'pinia'
import { getCart } from '../api/cart'
import { CART_UPDATED_EVENT } from '../utils/cartBadge'
import { useAuthStore } from './auth'

let listenerBound = false
let refreshSequence = 0

export const useCartBadgeStore = defineStore('cartBadge', {
  state: () => ({
    count: 0,
  }),
  getters: {
    badge(state) {
      if (state.count <= 0) return ''
      return state.count > 99 ? '99+' : String(state.count)
    },
  },
  actions: {
    ensureListener() {
      if (listenerBound || typeof window === 'undefined') return
      listenerBound = true
      window.addEventListener(CART_UPDATED_EVENT, () => {
        void this.refresh()
      })
    },
    reset() {
      refreshSequence += 1
      this.count = 0
    },
    async refresh() {
      this.ensureListener()
      const auth = useAuthStore()
      const requestId = ++refreshSequence
      if (!auth.isLoggedIn) {
        this.count = 0
        return
      }
      try {
        const items = await getCart()
        if (requestId !== refreshSequence || !auth.isLoggedIn) return
        this.count = (Array.isArray(items) ? items : []).reduce(
          (total, item) => total + Math.max(0, Number(item.quantity) || 0),
          0,
        )
      } catch {
        if (requestId === refreshSequence) this.count = 0
      }
    },
  },
})
