const CATALOG_IMAGES = {
  headphones: '/images/catalog/headphones-studio.jpg',
  phone: '/images/catalog/phone-studio.jpg',
  keyboard: '/images/catalog/keyboard-studio.jpg',
  laptop: '/images/catalog/laptop-studio.jpg',
  home: '/images/catalog/home-studio.jpg',
  lamp: '/images/catalog/lamp-studio.jpg',
}

const displayName = (value = '') => String(value)
  .replace(/^演示分类-/, '')
  .replace(/^演示\s*/, '')
  .replace(/^演示/, '')

const imageFrom = (product = {}) => (
  product.productImage || product.mainImage || product.image || ''
)

const PRODUCT_IMAGE_REVISION = '20260813-coverfix'

const withProductImageRevision = (image) => {
  const value = String(image || '').trim()
  if (!/\/api\/demo\/products\//i.test(value)) return value
  return `${value}${value.includes('?') ? '&' : '?'}rev=${PRODUCT_IMAGE_REVISION}`
}

// 仅占位 SVG 走本地类目兜底；/api/demo/products/* 为真实商品图
const isDemoImage = (image) => /\/api\/demo\/(?:phone|computer|home)\.svg$/i.test(String(image).trim())

export const visualForProduct = (product = {}) => {
  const name = displayName(product.name || product.productName || product.title)
  if (/耳机|音箱/.test(name)) return CATALOG_IMAGES.headphones
  if (/手机|数码/.test(name)) return CATALOG_IMAGES.phone
  if (/键盘|鼠标/.test(name)) return CATALOG_IMAGES.keyboard
  if (/电脑|笔记本|显示器|硬盘|路由|打印|摄像头/.test(name)) return CATALOG_IMAGES.laptop
  if (/台灯|家居|电饭煲|电水壶|吸尘|空气|体脂|插线板|风扇/.test(name)) return CATALOG_IMAGES.lamp
  return CATALOG_IMAGES.home
}

export const productImageSrc = (product = {}) => {
  const image = imageFrom(product)
  return image && !isDemoImage(image) ? withProductImageRevision(image) : visualForProduct(product)
}

export const restoreProductImage = (event, product) => {
  const image = event?.currentTarget
  if (!image || image.dataset.fallbackApplied === 'true') return
  image.dataset.fallbackApplied = 'true'
  image.src = visualForProduct(product)
}
