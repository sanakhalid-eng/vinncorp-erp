import { describe, it, expect } from 'vitest'
import API from '../api/axios'

describe('API instance', () => {
  it('creates an axios instance with correct baseURL', () => {
    expect(API.defaults.baseURL).toBe('/api')
  })

  it('sets Content-Type header', () => {
    expect(API.defaults.headers['Content-Type']).toBe('application/json')
  })
})
