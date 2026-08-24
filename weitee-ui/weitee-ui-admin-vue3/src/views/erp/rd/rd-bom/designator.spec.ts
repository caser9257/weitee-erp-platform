import assert from 'node:assert/strict'

const { parseDesignatorList, countDesignators } = await import(
  new URL('./designator.ts', import.meta.url).href
)

// 空文本
assert.deepEqual(parseDesignatorList(null), [])
assert.deepEqual(parseDesignatorList(''), [])
assert.deepEqual(parseDesignatorList('   '), [])

// 区间 + 单值 + 空白
assert.deepEqual(parseDesignatorList('R101-R105, R108, C12'), [
  'R101',
  'R102',
  'R103',
  'R104',
  'R105',
  'R108',
  'C12'
])
assert.equal(countDesignators('R101-R105, R108, C12'), 7)

// 零填充宽度
assert.deepEqual(parseDesignatorList('R001-R005'), ['R001', 'R002', 'R003', 'R004', 'R005'])

// 单值保留给定宽度
assert.deepEqual(parseDesignatorList('C12'), ['C12'])
assert.deepEqual(parseDesignatorList('U3'), ['U3'])

// 起止颠倒视为非法，保留原串
assert.deepEqual(parseDesignatorList('R105-R101'), ['R105-R101'])

// 段内空格
assert.deepEqual(parseDesignatorList('R101 - R105'), ['R101', 'R102', 'R103', 'R104', 'R105'])

// 无法识别形态原样大写保留
assert.deepEqual(parseDesignatorList('NOTE'), ['NOTE'])

// 连续逗号跳过空段
assert.deepEqual(parseDesignatorList('R1,,R2'), ['R1', 'R2'])

console.log('designator.spec.ts passed')
