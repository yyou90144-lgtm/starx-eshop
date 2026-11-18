import http from './http'
import { invalidateCatalogCache } from './catalog'

const afterCatalogWrite = async (promise) => {
  const result = await promise
  invalidateCatalogCache()
  return result
}

export const getAdminCategories = () => http.get('/admin/categories')

export const createAdminCategory = (payload) => afterCatalogWrite(http.post('/admin/categories', payload))

export const updateAdminCategory = (id, payload) => afterCatalogWrite(http.put(`/admin/categories/${id}`, payload))

export const updateAdminCategoryStatus = (id, status) => (
  afterCatalogWrite(http.patch(`/admin/categories/${id}/status`, { status }))
)

export const removeAdminCategory = (id) => afterCatalogWrite(http.delete(`/admin/categories/${id}`))

export const getAdminProducts = (params) => http.get('/admin/products', { params })

export const getAdminProduct = (id) => http.get(`/admin/products/${id}`)

export const createAdminProduct = (payload) => afterCatalogWrite(http.post('/admin/products', payload))

export const updateAdminProduct = (id, payload) => afterCatalogWrite(http.put(`/admin/products/${id}`, payload))

export const updateAdminProductStatus = (id, status) => (
  afterCatalogWrite(http.patch(`/admin/products/${id}/status`, { status }))
)

export const removeAdminProduct = (id) => afterCatalogWrite(http.delete(`/admin/products/${id}`))

export const createAdminSku = (productId, payload) => (
  afterCatalogWrite(http.post(`/admin/products/${productId}/skus`, payload))
)

export const updateAdminSku = (id, payload) => afterCatalogWrite(http.put(`/admin/skus/${id}`, payload))

export const updateAdminSkuStock = (id, stock) => (
  afterCatalogWrite(http.patch(`/admin/skus/${id}/stock`, { stock }))
)

export const removeAdminSku = (id) => afterCatalogWrite(http.delete(`/admin/skus/${id}`))

export const uploadAdminImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/admin/files/upload', form)
}
