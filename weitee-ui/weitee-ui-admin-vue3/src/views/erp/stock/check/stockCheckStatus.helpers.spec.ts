import assert from 'node:assert/strict'
import {
  deriveStockCheckActions,
  resolveStockCheckStatus,
  STOCK_CHECK_STATUS
} from './stockCheckStatus.helpers'

assert.deepEqual(STOCK_CHECK_STATUS, {
  DRAFT: 0,
  COUNTING: 10,
  REVIEWING: 20,
  APPROVED: 30,
  CLOSED: 40
})

const statusCases = [
  {
    name: 'DRAFT',
    status: 0,
    display: { label: '草稿', tone: 'neutral' },
    actions: {
      canEdit: true,
      canDelete: true,
      canStart: true,
      canSubmit: false,
      canApproveAndClose: false,
      canReject: false
    }
  },
  {
    name: 'COUNTING',
    status: 10,
    display: { label: '盘点中', tone: 'primary' },
    actions: {
      canEdit: true,
      canDelete: false,
      canStart: false,
      canSubmit: true,
      canApproveAndClose: false,
      canReject: true
    }
  },
  {
    name: 'REVIEWING',
    status: 20,
    display: { label: '审核中', tone: 'warning' },
    actions: {
      canEdit: false,
      canDelete: false,
      canStart: false,
      canSubmit: false,
      canApproveAndClose: true,
      canReject: true
    }
  },
  {
    name: 'APPROVED',
    status: 30,
    display: { label: '已审核', tone: 'primary' },
    actions: {
      canEdit: false,
      canDelete: false,
      canStart: false,
      canSubmit: false,
      canApproveAndClose: false,
      canReject: false
    }
  },
  {
    name: 'CLOSED',
    status: 40,
    display: { label: '已关闭', tone: 'success' },
    actions: {
      canEdit: false,
      canDelete: false,
      canStart: false,
      canSubmit: false,
      canApproveAndClose: false,
      canReject: false
    }
  },
  {
    name: 'undefined',
    status: undefined,
    display: { label: '未知状态', tone: 'neutral' },
    actions: {
      canEdit: false,
      canDelete: false,
      canStart: false,
      canSubmit: false,
      canApproveAndClose: false,
      canReject: false
    }
  },
  {
    name: 'unknown',
    status: 999,
    display: { label: '未知状态', tone: 'neutral' },
    actions: {
      canEdit: false,
      canDelete: false,
      canStart: false,
      canSubmit: false,
      canApproveAndClose: false,
      canReject: false
    }
  }
]

for (const testCase of statusCases) {
  assert.deepEqual(
    resolveStockCheckStatus(testCase.status),
    testCase.display,
    `${testCase.name} 状态展示`
  )
  assert.deepEqual(
    deriveStockCheckActions(testCase.status),
    testCase.actions,
    `${testCase.name} 动作矩阵`
  )
}
