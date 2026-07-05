import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { mockProducts } from '../../data/mockProducts'
import { useChatStore } from '../../stores/chatStore'
import { ProductCard } from './ProductCard'

beforeEach(() => {
  useChatStore.setState({ draft: '' })
})

describe('ProductCard', () => {
  it('fills stock prompt without calling child-node APIs directly', async () => {
    render(<ProductCard product={mockProducts[0]} />)

    await userEvent.click(screen.getByRole('button', { name: '查库存' }))

    expect(useChatStore.getState().draft).toContain('查一下')
    expect(useChatStore.getState().draft).toContain(mockProducts[0].skuId)
  })

  it('fills add-to-cart natural language prompt', async () => {
    render(<ProductCard product={mockProducts[0]} />)

    await userEvent.click(screen.getByRole('button', { name: '加购' }))

    expect(useChatStore.getState().draft).toContain('帮我加购一台')
    expect(useChatStore.getState().draft).toContain(mockProducts[0].name)
  })
})
