export interface MockProduct {
  id: string
  name: string
  skuId: string
  spec: string
  tags: string[]
  reason: string
}

export const mockProducts: MockProduct[] = [
  {
    id: 'mi-14',
    name: '小米14',
    skuId: 'sku-14',
    spec: '16GB+512GB',
    tags: ['影像旗舰', '高性能', '小屏旗舰'],
    reason: '适合关注影像、性能和手感平衡的用户。',
  },
  {
    id: 'redmi-k70',
    name: 'Redmi K70',
    skuId: 'sku-k70',
    spec: '12GB+256GB',
    tags: ['游戏性能', '高性价比', '高刷屏'],
    reason: '适合预算敏感但希望获得强性能体验的用户。',
  },
]
