import assert from 'node:assert/strict'
import { displayLedgerName, displayLedgerSide, financeDisplayLabel, toFinanceDisplayText } from './financeDisplay'

const auditRoles = ['finance_audit']

assert.equal(toFinanceDisplayText('外部账簿', '-', auditRoles), '账簿')
assert.equal(toFinanceDisplayText('内部账凭证', '-', auditRoles), '账目凭证')
assert.equal(toFinanceDisplayText('外部金额', '-', auditRoles), '金额一')
assert.equal(toFinanceDisplayText('内部来源值', '-', auditRoles), '账目二来源值')
assert.equal(toFinanceDisplayText('双账套差异', '-', auditRoles), '账目差异')
assert.equal(toFinanceDisplayText(undefined, '-', auditRoles), '-')
assert.equal(financeDisplayLabel('外账凭证', '凭证一', auditRoles), '凭证一')

assert.equal(toFinanceDisplayText('外部账簿', '-', []), '外部账簿')
assert.equal(toFinanceDisplayText('内部账簿', '-', ['super_admin']), '内部账簿')
assert.equal(financeDisplayLabel('内账凭证', '凭证二', ['super_admin']), '内账凭证')
assert.equal(displayLedgerName('内部账簿'), '内部账簿')
assert.equal(displayLedgerSide('external'), '账目一')
assert.equal(displayLedgerSide('internal'), '账目二')
