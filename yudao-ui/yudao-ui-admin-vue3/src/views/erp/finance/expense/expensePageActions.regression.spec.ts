import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const source = readFileSync(new URL('./index.vue', import.meta.url), 'utf8')

assert.ok(
  source.includes('@click.stop="handleUpdateStatus(row.id, 20)"'),
  '审核按钮必须传入费用单主键 id'
)

assert.ok(
  source.includes('@click.stop="handleUpdateStatus(row.id, 10)"'),
  '反审核按钮必须传入费用单主键 id'
)

assert.equal(
  source.includes('@click.stop="handleUpdateStatus(row, 20)"'),
  false,
  '审核按钮不能传入整行对象'
)

assert.equal(
  source.includes('@click.stop="handleUpdateStatus(row, 10)"'),
  false,
  '反审核按钮不能传入整行对象'
)

console.log('expense page action regression checks passed')
