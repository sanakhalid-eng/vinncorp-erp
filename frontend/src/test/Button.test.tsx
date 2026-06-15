import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Button from '../components/Button'

describe('Button', () => {
  it('renders children', () => {
    render(<Button onClick={() => {}}>Click me</Button>)
    expect(screen.getByText('Click me')).toBeInTheDocument()
  })

  it('renders as button type by default', () => {
    render(<Button onClick={() => {}}>Submit</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('type', 'button')
  })

  it('accepts submit type', () => {
    render(<Button type="submit" onClick={() => {}}>Submit</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('type', 'submit')
  })
})
