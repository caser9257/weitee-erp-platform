import assert from 'node:assert/strict'

const { getTagsViewIdentityKey, normalizeTagsViewPath } = await import(
  new URL('./tagsViewIdentity.ts', import.meta.url).href
)

assert.equal(normalizeTagsViewPath('/erp/purchase/in'), '/scm/inbound')
assert.equal(normalizeTagsViewPath('/erp/purchase/order'), '/scm/purchase-order')

assert.equal(
  getTagsViewIdentityKey({
    path: '/scm/stock',
    fullPath: '/scm/stock?productId=99103&warehouseId=99201'
  }),
  '/scm/stock'
)

assert.equal(
  getTagsViewIdentityKey({
    path: '/erp/purchase/in',
    fullPath: '/erp/purchase/in?openId=992100&openType=detail'
  }),
  '/scm/inbound'
)

assert.equal(
  getTagsViewIdentityKey({
    path: '/scm/inbound',
    fullPath: '/scm/inbound?orderNo=CGMINI-0001'
  }),
  '/scm/inbound'
)

assert.equal(
  getTagsViewIdentityKey({
    path: '/crm/customer',
    fullPath: '/crm/customer?id=1'
  }),
  '/crm/customer?id=1'
)
