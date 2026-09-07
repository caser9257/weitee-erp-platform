import assert from 'node:assert/strict'
import {
  RD_BOM_STATUS,
  canCancel,
  canChange,
  canEdit,
  canPublish,
  canSubmit,
  canUnvoid,
  canVoid,
  isApprovalRunning,
  isVoid
} from './rdBomStatus'

const draft = { status: RD_BOM_STATUS.DRAFT, processInstanceId: undefined }
const approving = { status: RD_BOM_STATUS.APPROVING, processInstanceId: 'process-1' }
const approved = { status: RD_BOM_STATUS.APPROVED, processInstanceId: undefined }
const voided = { status: RD_BOM_STATUS.VOIDED, processInstanceId: undefined }
const failed = { status: RD_BOM_STATUS.FAILED, processInstanceId: undefined }

assert.equal(isApprovalRunning(null), false)
assert.equal(isVoid(undefined), false)
assert.equal(canEdit(null), false)
assert.equal(canSubmit(null), false)
assert.equal(canPublish(null), false)
assert.equal(canChange(null), false)
assert.equal(canVoid(null), false)
assert.equal(canUnvoid(null), false)

assert.equal(canEdit(draft), true)
assert.equal(canSubmit(draft), true)
assert.equal(canSubmit(failed), true)
assert.equal(isApprovalRunning(approving), true)
assert.equal(canCancel(approving), true)
assert.equal(canEdit(approving), false)
assert.equal(canSubmit(approving), false)
assert.equal(canPublish(approved), true)
assert.equal(canChange(approved), true)
assert.equal(canVoid(approved), true)
assert.equal(isVoid(voided), true)
assert.equal(canEdit(voided), false)
assert.equal(canUnvoid(voided), true)

console.log('rdBomStatus.spec.ts passed')
