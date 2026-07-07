import assert from 'node:assert/strict'

const {
  erpCountInputFormatter,
  erpPriceInputFormatter,
  erpCountTableColumnFormatter,
  erpPriceTableColumnFormatter,
  erpPriceDisplayFormatter,
  erpCountDisplayFormatter
} = await import(
  new URL('./index.ts', import.meta.url).href
)

assert.equal(erpCountInputFormatter(400), '400')
assert.equal(erpCountInputFormatter(400.1), '400.1')
assert.equal(erpCountInputFormatter(400.12), '400.12')
assert.equal(erpCountInputFormatter(400.123), '400.123')
assert.equal(erpCountInputFormatter('0.500'), '0.5')
assert.equal(erpCountInputFormatter(undefined), '')

assert.equal(erpPriceInputFormatter(1234.5), '1234.50')
assert.equal(erpCountTableColumnFormatter(undefined, undefined, 1234567.5, undefined), '1,234,567.5')
assert.equal(erpPriceTableColumnFormatter(undefined, undefined, 1234567.89, undefined), '1,234,567.89')
assert.equal(erpPriceDisplayFormatter(1234.5), '1,234.50')
assert.equal(erpCountDisplayFormatter(1234567.5), '1,234,567.5')
