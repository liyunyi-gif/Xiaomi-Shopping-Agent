export interface QuickPrompt {
  id: string
  label: string
  prompt: string
  tone?: 'brand' | 'ai' | 'neutral'
}

export const quickPrompts: QuickPrompt[] = [
  {
    id: 'spec-camera',
    label: '商品咨询',
    prompt: '小米14的影像规格怎么样？',
    tone: 'brand',
  },
  {
    id: 'game-phone',
    label: '游戏手机推荐',
    prompt: '帮我推荐一款适合打游戏的手机',
    tone: 'ai',
  },
  {
    id: 'add-cart',
    label: '加购演示',
    prompt: '帮我加购一台小米14 16GB+512GB',
    tone: 'brand',
  },
  {
    id: 'stock',
    label: '查库存',
    prompt: '查一下 sku-14 有没有库存',
  },
  {
    id: 'logistics',
    label: '查物流',
    prompt: '帮我查一下订单 order-12345678 的物流',
  },
  {
    id: 'clear-memory',
    label: '清除记忆',
    prompt: '清除我的记忆',
  },
]
