import { describe, expect, it } from 'vitest'
import { parseShoppingState } from './responseParser'

const baseReply = {
  answer: '已处理成功',
  intent: 'SHOPPING',
  needClarify: false,
}

describe('parseShoppingState', () => {
  it('parses structured cart result', () => {
    const result = parseShoppingState({
      ...baseReply,
      data: {
        status: 'SUCCESS',
        tool: 'add_to_cart',
        cart: { cartId: 'cart-1234', skuCode: 'sku-14', spec: '16GB+512GB', quantity: 1 },
      },
    })

    expect(result?.type).toBe('added')
    expect(result?.fields?.cartId).toBe('cart-1234')
    expect(result?.fields?.skuCode).toBe('sku-14')
  })

  it('parses clarify result from ChatReply fields', () => {
    const result = parseShoppingState({
      answer: '还需要补充[商品规格]，请告诉我后我再继续处理。',
      intent: 'SHOPPING',
      needClarify: true,
      clarifyQuestion: '还需要补充商品规格。',
      data: { status: 'NEED_CLARIFY', missingSlots: ['spec'] },
    })

    expect(result?.type).toBe('needClarify')
  })

  it('keeps stock result distinct from an empty shopping state', () => {
    const result = parseShoppingState({
      ...baseReply,
      data: {
        status: 'SUCCESS',
        tool: 'query_stock',
        stock: { skuCode: 'sku-14', stock: 12 },
      },
    })

    expect(result?.type).toBe('stock')
    expect(result?.fields?.skuCode).toBe('sku-14')
    expect(result?.fields?.stock).toBe(12)
  })

  it('does not parse natural-language answer as authoritative state', () => {
    expect(parseShoppingState({ ...baseReply, answer: '已处理成功：{cartId=cart-1234, skuCode=sku-14}' })).toBeUndefined()
  })
})
