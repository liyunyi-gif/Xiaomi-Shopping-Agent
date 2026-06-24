export function createId(prefix: string): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return `${prefix}-${crypto.randomUUID().slice(0, 8)}`
  }
  return `${prefix}-${Math.random().toString(36).slice(2, 10)}`
}

export function getOrCreateLocalId(key: string, prefix: string): string {
  const existing = localStorage.getItem(key)
  if (existing) {
    return existing
  }
  const next = createId(prefix)
  localStorage.setItem(key, next)
  return next
}
