/**
 * Parse each SKU specsJson once and index by sku id.
 * @param {Array<{ id: number|string, specsJson?: string }>|null|undefined} skus
 * @returns {Map<number|string, Record<string, string>>}
 */
export const buildSkuSpecIndex = (skus) => {
  const index = new Map()
  ;(skus || []).forEach((sku) => {
    if (!sku || sku.id == null) return
    let map = {}
    try {
      map = JSON.parse(sku.specsJson || '{}') || {}
    } catch {
      map = {}
    }
    index.set(sku.id, map)
  })
  return index
}

export const specsEntries = (map = {}) => (
  Object.entries(map).map(([key, value]) => ({ key, value }))
)

export const specsLabel = (map = {}) => (
  specsEntries(map).map((item) => item.value).join(' / ')
)
