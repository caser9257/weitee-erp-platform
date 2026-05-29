import assert from 'node:assert/strict'

const {
  cloneBomItemSubstitutes,
  buildBomPayload,
  createDefaultBomFormData,
  createEmptyBomItem,
  createEmptyBomSubstitute,
  getCanSubmitBomForm,
  getBomSubstituteValidationMessage,
  serializeBomItemSubstitutes,
  normalizeBomFormData
} = await import(new URL('./bomForm.helpers.ts', import.meta.url).href)

const emptyForm = createDefaultBomFormData()
assert.equal(getCanSubmitBomForm(emptyForm, false), false)

const validForm = {
  ...emptyForm,
  bomCode: 'MFG-BOM-001',
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

assert.equal(validForm.items[0].mrpEnableFlag, true)
assert.equal(validForm.items[0].supplyOwner, 'COMPANY')

const payload = buildBomPayload(
  normalizeBomFormData({
    id: undefined,
    bomCode: 'MFG-BOM-002',
    productId: 101,
    version: '',
    status: 0,
    remark: '',
    items: [
      {
        id: undefined,
        materialId: 1002,
        materialType: undefined,
        usageQty: 1,
        lossRate: undefined,
        leadTimeDay: undefined,
        mrpEnableFlag: false,
        supplyOwner: 'CUSTOMER',
        sort: undefined,
        remark: '',
        substitutes: [{ ...createEmptyBomSubstitute(), substituteMaterialId: 1003 }]
      }
    ]
  })
)

assert.equal(payload.status, 0)
assert.equal(payload.items[0].lossRate, undefined)
assert.equal(payload.items[0].leadTimeDay, undefined)
assert.equal(payload.items[0].mrpEnableFlag, false)
assert.equal(payload.items[0].supplyOwner, 'CUSTOMER')
assert.equal(payload.items[0].substitutes[0].substituteMaterialId, 1003)

const substitutes = [
  { ...createEmptyBomSubstitute(), substituteMaterialId: 2001 },
  { ...createEmptyBomSubstitute(), substituteMaterialId: 2002 }
]
const clonedSubstitutes = cloneBomItemSubstitutes(substitutes)
assert.deepEqual(clonedSubstitutes, substitutes)
assert.notEqual(clonedSubstitutes, substitutes)
assert.notEqual(clonedSubstitutes[0], substitutes[0])
assert.equal(
  serializeBomItemSubstitutes(substitutes),
  serializeBomItemSubstitutes(clonedSubstitutes)
)

assert.equal(getBomSubstituteValidationMessage(substitutes), undefined)
assert.equal(
  getBomSubstituteValidationMessage([
    { ...createEmptyBomSubstitute(), substituteMaterialId: 2001 },
    { ...createEmptyBomSubstitute(), substituteMaterialId: 2001 }
  ]),
  '替代物料不能重复'
)
