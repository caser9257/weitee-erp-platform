import { strict as assert } from 'node:assert'

import { normalizeArStatementSummary, type ArStatementSummaryVO } from './arStatement.helpers'

const summary: ArStatementSummaryVO = {
  customerId: 3,
  customerName: '客户A',
  totalAmount: 100,
  totalReceivedAmount: 20,
  totalRemainAmount: 80,
  statementCount: 2
}

assert.deepEqual(normalizeArStatementSummary([summary]), [summary])
assert.deepEqual(normalizeArStatementSummary(summary), [summary])
assert.deepEqual(normalizeArStatementSummary(null), [])
