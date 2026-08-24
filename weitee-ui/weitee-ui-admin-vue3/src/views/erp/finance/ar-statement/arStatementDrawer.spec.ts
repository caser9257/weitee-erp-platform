import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const source = readFileSync(new URL('./index.vue', import.meta.url), 'utf8')

assert.match(
  source,
  /<el-drawer[\s\S]*append-to-body[\s\S]*:z-index="3000"/,
  'AR statement detail drawer must render in body above the global backtop layer'
)
