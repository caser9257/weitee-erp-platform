import assert from 'node:assert/strict'

async function main() {
  const module = await import(
    new URL('../../../../views/erp/finance/voucher/voucherStatus.helpers.ts', import.meta.url).href
  )
  const { canRecomputeVoucher } = module.default ?? module

  assert.equal(canRecomputeVoucher({ status: 10, bizType: 32, bizId: 1 }), true)
  assert.equal(canRecomputeVoucher({ status: 50, bizType: 32, bizId: 1 }), false)
  assert.equal(canRecomputeVoucher({ status: 10, bizType: 32 }), false)
}

void main()
