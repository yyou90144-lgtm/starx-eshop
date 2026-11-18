import { productImageSrc } from './productVisual'

const PLACEHOLDER = '/api/demo/products/placeholder-blank.svg'

const detailParseCache = {
  productId: null,
  detailRaw: null,
  parsed: null,
}

const safeParse = (raw) => {
  if (!raw || typeof raw !== 'string') return null
  const text = raw.trim()
  if (!text.startsWith('{')) return null
  try {
    const data = JSON.parse(text)
    if (!data || data.version !== 1) return null
    return data
  } catch {
    return null
  }
}

const getParsedDetail = (product = {}) => {
  const productId = product.id ?? null
  const detailRaw = product.detail ?? null
  if (
    productId != null
    && productId === detailParseCache.productId
    && detailRaw === detailParseCache.detailRaw
  ) {
    return detailParseCache.parsed
  }
  const parsed = safeParse(detailRaw)
  detailParseCache.productId = productId
  detailParseCache.detailRaw = detailRaw
  detailParseCache.parsed = parsed
  return parsed
}

const unique = (list = []) => {
  const out = []
  const seen = new Set()
  list.forEach((item) => {
    const value = String(item || '').trim()
    if (!value || seen.has(value)) return
    // also collapse identical filename with different query/cache suffixes
    const key = value.split('?')[0]
    if (seen.has(key)) return
    seen.add(value)
    seen.add(key)
    out.push(value)
  })
  return out
}

/**
 * Normalize product detail payload:
 * - structured JSON in `detail` (version 1)
 * - optional colorGalleries map for multi-color product photos
 * - plain text fallback
 * @param {object} product
 * @param {{ color?: string }} [options]
 */
export const resolveProductDetailContent = (product = {}, options = {}) => {
  const parsed = getParsedDetail(product)
  const main = productImageSrc(product) || PLACEHOLDER
  const preferColor = options.color || ''

  if (parsed) {
    const colorGalleries = parsed.colorGalleries && typeof parsed.colorGalleries === 'object'
      ? parsed.colorGalleries
      : null
    let gallery = []
    if (preferColor && colorGalleries?.[preferColor]?.length) {
      gallery = unique(colorGalleries[preferColor])
    } else if (parsed.defaultColor && colorGalleries?.[parsed.defaultColor]?.length) {
      gallery = unique(colorGalleries[parsed.defaultColor])
    } else {
      gallery = unique([...(parsed.gallery || []), main])
    }
    // 顶部轮播用 gallery；下方「图文详情」必须用 detailImages（小米详情页往下拉的长图）
    const detailImages = unique(parsed.detailImages || [])
    return {
      gallery: gallery.length ? gallery : [main],
      highlights: Array.isArray(parsed.highlights) ? parsed.highlights.filter(Boolean) : [],
      paragraphs: Array.isArray(parsed.paragraphs) && parsed.paragraphs.length
        ? parsed.paragraphs
        : (product.detail && !product.detail.trim().startsWith('{') ? [product.detail] : ['暂无更多商品详情。']),
      detailImages,
      specs: Array.isArray(parsed.specs) ? parsed.specs : [],
      colorGalleries,
      defaultColor: parsed.defaultColor || '',
      learningNote: parsed.learningNote || '',
      sourceNote: parsed.sourceNote || '',
      structured: true,
    }
  }

  return {
    gallery: [main],
    highlights: [],
    paragraphs: [product.detail || '暂无更多商品详情。'],
    detailImages: [],
    specs: [],
    colorGalleries: null,
    defaultColor: '',
    learningNote: '',
    sourceNote: '',
    structured: false,
  }
}

export const isPlaceholderImage = (src = '') => /placeholder-blank\.svg$/i.test(String(src))
