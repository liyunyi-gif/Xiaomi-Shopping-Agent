import { describe, expect, it } from 'vitest'
import { parseShoppingState } from './responseParser'

describe('parseShoppingState', () => {
  it('parses cart result', () => {
    const result = parseShoppingState('已处理成功：{cartId=cart-1234, skuId=sku-14, spec=16GB+512GB, quantity=1}')

    expect(result?.type).toBe('added')
    expect(result?.fields?.cartId).toBe('cart-1234')
    expect(result?.fields?.skuId).toBe('sku-14')
  })

  it('parses clarify result', () => {
    const result = parseShoppingState('还需要补充[商品规格]，请告诉我后我再继续处理。', true)

    expect(result?.type).toBe('needClarify')
  })

  it('returns undefined when answer cannot be parsed', () => {
    expect(parseShoppingState('根据当前知识库资料：这是一段普通回复')).toBeUndefined()
  })
})
