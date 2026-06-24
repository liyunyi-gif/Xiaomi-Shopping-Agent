export interface ParsedShoppingState {
  type: 'empty' | 'added' | 'orderCreated' | 'logistics' | 'needClarify' | 'failed'
  title: string
  description: string
  fields?: Record<string, string | number>
}

function extractValue(text: string, key: string): string | undefined {
  const patterns = [
    new RegExp(`${key}=([^,}\\s]+)`),
    new RegExp(`"${key}"\\s*:\\s*"?([^,"}]+)"?`),
  ]
  for (const pattern of patterns) {
    const match = text.match(pattern)
    if (match?.[1]) return match[1]
  }
  return undefined
}

export function parseShoppingState(answer: string, needClarify?: boolean): ParsedShoppingState | undefined {
  if (!answer) return undefined

  if (needClarify || answer.includes('还需要补充')) {
    return {
      type: 'needClarify',
      title: '还需要补充信息',
      description: answer,
    }
  }

  if (answer.includes('操作失败')) {
    return {
      type: 'failed',
      title: '操作失败',
      description: answer,
    }
  }

  const cartId = extractValue(answer, 'cartId')
  if (cartId || answer.includes('add_to_cart')) {
    return {
      type: 'added',
      title: '已加入购物车',
      description: '商品已成功加入购物车，可以继续下单或查询库存。',
      fields: {
        cartId: cartId || '已生成',
        skuId: extractValue(answer, 'skuId') || '未知 SKU',
        spec: extractValue(answer, 'spec') || '默认规格',
        quantity: extractValue(answer, 'quantity') || 1,
      },
    }
  }

  const orderId = extractValue(answer, 'orderId')
  if (orderId || answer.includes('orderNo')) {
    return {
      type: 'orderCreated',
      title: '订单已创建',
      description: '订单创建成功，可以继续查询物流状态。',
      fields: {
        orderId: orderId || '已生成',
        orderNo: extractValue(answer, 'orderNo') || '待同步',
        status: extractValue(answer, 'status') || 'CREATED',
      },
    }
  }

  const logisticsNo = extractValue(answer, 'logisticsNo')
  if (logisticsNo || answer.includes('物流')) {
    return {
      type: 'logistics',
      title: '物流状态',
      description: '已查询到物流信息。',
      fields: {
        logisticsNo: logisticsNo || '待同步',
        logisticsStatus: extractValue(answer, 'logisticsStatus') || '运输中',
      },
    }
  }

  return undefined
}
