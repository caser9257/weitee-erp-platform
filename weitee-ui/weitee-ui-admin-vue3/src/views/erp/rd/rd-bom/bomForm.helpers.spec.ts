import assert from 'node:assert/strict'

const {
  buildBomPayload,
  createDefaultBomFormData,
  createEmptyBomItem,
  createEmptyBomSubstitute,
  getCanSubmitBomForm,
  normalizeBomFormData
} = await import(new URL('./bomForm.helpers.ts', import.meta.url).href)

const emptyForm = createDefaultBomFormData()
assert.equal(getCanSubmitBomForm(emptyForm, false), false)

const validForm = {
  ...emptyForm,
  bomCode: 'RD-BOM-001',
  productId: 100,
  items: [
    {
      ...createEmptyBomItem(),
      materialId: 1001,
      usageQty: 2,
      substitutes: [{ ...createEmptyBomSubstitute(), substituteMaterialId: 1002 }]
    }
  ]
}

assert.equal(getCanSubmitBomForm(validForm, false), true)
assert.equal(getCanSubmitBomForm(validForm, true), false)

const payload = buildBomPayload(
  normalizeBomFormData({
    id: undefined,
    bomCode: 'RD-BOM-002',
    productId: 101,
    version: '',
    status: 0,
    remark: '',
    items: [
      {
        ...createEmptyBomItem(),
        id: undefined,
        materialId: 1002,
        materialType: undefined,
        usageQty: 1,
        lossRate: undefined,
        leadTimeDay: undefined,
        sort: undefined,
        remark: '',
        substitutes: [{ ...createEmptyBomSubstitute(), substituteMaterialId: 1003 }]
      }
    ]
  })
)

assert.equal(payload.items[0].lossRate, undefined)
assert.equal(payload.items[0].leadTimeDay, undefined)
assert.equal(payload.items[0].substitutes[0].substituteMaterialId, 1003)
