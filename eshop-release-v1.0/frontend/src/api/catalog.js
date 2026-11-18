import http from './http'
import { cachedRequest, invalidateRequestCache } from '../utils/requestCache'

const CATEGORIES_TTL_MS = 5 * 60 * 1000
const HOT_TTL_MS = 60 * 1000

export const invalidateCatalogCache = () => {
  invalidateRequestCache('catalog:')
}

export const getCategories = () => cachedRequest(
  'catalog:categories',
  () => http.get('/categories'),
  CATEGORIES_TTL_MS,
)

export const getProducts = (params) => http.get('/products', { params })

export const getProduct = (id) => http.get(`/products/${id}`)

export const getHotProducts = ({ days = 30, limit = 5 } = {}) => cachedRequest(
  `catalog:hot:${days}:${limit}`,
  () => http.get('/products/hot-ranking', { params: { days, limit } }),
  HOT_TTL_MS,
)
