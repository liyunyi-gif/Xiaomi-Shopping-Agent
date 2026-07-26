import type { ChatReply } from '../api/types'

export interface ParsedShoppingState {
  type: 'empty' | 'added' | 'orderCreated' | 'logistics' | 'stock' | 'needClarify' | 'failed'
  title: string
  description: string
  fields?: Record<string, string | number>
}

function asRecord(value: unknown): Record<string, unknown> | undefined {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return undefined
  return value as Record<string, unknown>
}

function text(value: unknown, fallback = ''): string {
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }
  return fallback
}

function numberOrText(value: unknown, fallback: string | number): string | number {
  if (typeof value === 'number') return value
  if (value === null || value === undefined) return fallback
  return String(value)
}

export function parseShoppingState(reply: ChatReply): ParsedShoppingState | undefined {
  const data = asRecord(reply.data)

  if (reply.needClarify) {
    return {
      type: 'needClarify',
      title: '还需要补充信息',
      description: reply.clarifyQuestion || reply.answer,
    }
  }

  if (data?.status === 'FAILED') {
    return {
      type: 'failed',
      title: '操作失败',
      description: reply.answer,
    }
  }

  if (!data) return undefined

  const cart = asRecord(data.cart)
  if (cart) {
    return {
      type: 'added',
      title: '已加入购物车',
      description: '商品已成功加入购物车，可以继续下单或查询库存。',
      fields: {
        cartId: text(cart.cartId, '已生成'),
        skuCode: text(cart.skuCode, '未知 SKU'),
        spec: text(cart.spec, '默认规格'),
        quantity: numberOrText(cart.quantity, 1),
      },
    }
  }

  const order = asRecord(data.order)
  if (order) {
    return {
      type: 'orderCreated',
      title: '订单已创建',
      description: '订单创建成功，可以继续查询物流状态。',
      fields: {
        orderId: text(order.orderId, '已生成'),
        orderNo: text(order.orderNo, '待同步'),
        status: text(order.status, 'CREATED'),
      },
    }
  }

  const logistics = asRecord(data.logistics)
  if (logistics) {
    return {
      type: 'logistics',
      title: '物流状态',
      description: '已查询到物流信息。',
      fields: {
        logisticsNo: text(logistics.logisticsNo, '待同步'),
        logisticsStatus: text(logistics.logisticsStatus, '运输中'),
      },
    }
  }

  const stock = asRecord(data.stock)
  if (stock) {
    return {
      type: 'stock',
      title: '库存状态',
      description: '已查询到库存信息。',
      fields: {
        skuCode: text(stock.skuCode, '未知 SKU'),
        stock: numberOrText(stock.stock, '待同步'),
      },
    }
  }

  return undefined
}
