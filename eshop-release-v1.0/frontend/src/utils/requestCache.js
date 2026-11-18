const cache = new Map()
const inflight = new Map()

/**
 * Deduplicate in-flight GETs and keep a short TTL memory cache.
 * @param {string} key
 * @param {() => Promise<any>} loader
 * @param {number} ttlMs
 */
export const cachedRequest = (key, loader, ttlMs = 60_000) => {
  const hit = cache.get(key)
  if (hit && Date.now() < hit.expiresAt) {
    return Promise.resolve(hit.value)
  }

  const pending = inflight.get(key)
  if (pending) return pending

  const request = Promise.resolve()
    .then(loader)
    .then((value) => {
      cache.set(key, { value, expiresAt: Date.now() + ttlMs })
      inflight.delete(key)
      return value
    })
    .catch((error) => {
      inflight.delete(key)
      throw error
    })

  inflight.set(key, request)
  return request
}

export const invalidateRequestCache = (prefix = '') => {
  for (const key of [...cache.keys()]) {
    if (!prefix || key.startsWith(prefix)) cache.delete(key)
  }
  for (const key of [...inflight.keys()]) {
    if (!prefix || key.startsWith(prefix)) inflight.delete(key)
  }
}
