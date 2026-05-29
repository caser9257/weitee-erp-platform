import type { ErpFinanceLedgerVO } from '@/api/erp/finance/ledger'
import type { ErpFinanceReportItemVO, ErpFinanceReportItemSubjectVO } from '@/api/erp/finance/report-item'
import type { ErpFinanceSubjectVO } from '@/api/erp/finance/subject'

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value))

export const reportItemDemoLedgers: ErpFinanceLedgerVO[] = [
  { id: 101, no: 'L-2026-001', name: '财务主账簿', status: 0, sort: 1, defaultStatus: true },
  { id: 102, no: 'L-2026-002', name: '制造辅助账簿', status: 0, sort: 2, defaultStatus: false }
]

const reportItemDemoSubjectsByLedger: Record<number, ErpFinanceSubjectVO[]> = {
  101: [
    { id: 1001, ledgerId: 101, subjectCode: '1001', subjectName: '库存现金', status: 0 },
    { id: 1002, ledgerId: 101, subjectCode: '1002', subjectName: '银行存款', status: 0 },
    { id: 1122, ledgerId: 101, subjectCode: '1122', subjectName: '应收账款', status: 0 },
    { id: 1405, ledgerId: 101, subjectCode: '1405', subjectName: '库存商品', status: 0 },
    { id: 6001, ledgerId: 101, subjectCode: '6001', subjectName: '主营业务收入', status: 0 },
    { id: 6602, ledgerId: 101, subjectCode: '6602', subjectName: '管理费用', status: 0 }
  ],
  102: [
    { id: 5001, ledgerId: 102, subjectCode: '5001', subjectName: '生产成本', status: 0 },
    { id: 5101, ledgerId: 102, subjectCode: '5101', subjectName: '制造费用', status: 0 },
    { id: 5401, ledgerId: 102, subjectCode: '5401', subjectName: '研发支出', status: 0 },
    { id: 6401, ledgerId: 102, subjectCode: '6401', subjectName: '销售费用', status: 0 },
    { id: 6403, ledgerId: 102, subjectCode: '6403', subjectName: '财务费用', status: 0 },
    { id: 2221, ledgerId: 102, subjectCode: '2221', subjectName: '应交税费', status: 0 }
  ]
}

const demoSubjects = (ledgerId: number, items: Array<{ subjectCode: string; amountRule: number; amountSign: number }>): ErpFinanceReportItemSubjectVO[] => {
  const subjectMap = new Map((reportItemDemoSubjectsByLedger[ledgerId] || []).map((item) => [item.subjectCode, item]))
  return items.map((item) => {
    const subject = subjectMap.get(item.subjectCode)
    return {
      subjectCode: item.subjectCode,
      subjectName: subject?.subjectName || item.subjectCode,
      amountRule: item.amountRule,
      amountRuleName:
        {
          10: '期初借方',
          20: '期初贷方',
          30: '本期借方',
          40: '本期贷方',
          50: '期末借方',
          60: '期末贷方'
        }[item.amountRule] || '-',
      amountSign: item.amountSign
    }
  })
}

export const reportItemDemoRows: ErpFinanceReportItemVO[] = [
  {
    id: 900101,
    ledgerId: 101,
    ledgerName: '财务主账簿',
    reportType: 10,
    reportTypeName: '资产负债表',
    itemCategory: 10,
    itemCategoryName: '资产',
    itemCode: 'BS-CASH',
    itemName: '货币资金',
    status: 0,
    sort: 10,
    remark: '示例数据：用于验证首屏密度与抽屉结构',
    subjects: demoSubjects(101, [
      { subjectCode: '1001', amountRule: 50, amountSign: 1 },
      { subjectCode: '1002', amountRule: 60, amountSign: 1 }
    ])
  },
  {
    id: 900102,
    ledgerId: 101,
    ledgerName: '财务主账簿',
    reportType: 10,
    reportTypeName: '资产负债表',
    itemCategory: 10,
    itemCategoryName: '资产',
    itemCode: 'BS-AR',
    itemName: '应收账款',
    status: 0,
    sort: 20,
    remark: '示例数据：查看分类与状态徽章',
    subjects: demoSubjects(101, [
      { subjectCode: '1122', amountRule: 50, amountSign: 1 }
    ])
  },
  {
    id: 900103,
    ledgerId: 101,
    ledgerName: '财务主账簿',
    reportType: 10,
    reportTypeName: '资产负债表',
    itemCategory: 10,
    itemCategoryName: '资产',
    itemCode: 'BS-INV',
    itemName: '存货',
    status: 1,
    sort: 30,
    remark: '示例数据：停用状态',
    subjects: demoSubjects(101, [
      { subjectCode: '1405', amountRule: 50, amountSign: 1 }
    ])
  },
  {
    id: 900104,
    ledgerId: 102,
    ledgerName: '制造辅助账簿',
    reportType: 20,
    reportTypeName: '利润表',
    itemCategory: 40,
    itemCategoryName: '收入',
    itemCode: 'IS-REV',
    itemName: '营业收入',
    status: 0,
    sort: 40,
    remark: '示例数据：用于验证不同报表类型',
    subjects: demoSubjects(102, [
      { subjectCode: '6401', amountRule: 40, amountSign: 1 },
      { subjectCode: '5401', amountRule: 30, amountSign: -1 }
    ])
  },
  {
    id: 900105,
    ledgerId: 102,
    ledgerName: '制造辅助账簿',
    reportType: 20,
    reportTypeName: '利润表',
    itemCategory: 50,
    itemCategoryName: '成本费用',
    itemCode: 'IS-EXP',
    itemName: '管理费用',
    status: 0,
    sort: 50,
    remark: '示例数据：用于验证映射条数和抽屉滚动',
    subjects: demoSubjects(102, [
      { subjectCode: '6602', amountRule: 40, amountSign: 1 },
      { subjectCode: '6403', amountRule: 40, amountSign: 1 },
      { subjectCode: '5101', amountRule: 30, amountSign: -1 }
    ])
  }
]

export const findDemoReportItem = (id?: number) => reportItemDemoRows.find((item) => item.id === id)

export const getDemoSubjectOptions = (ledgerId?: number) => {
  if (!ledgerId) {
    return []
  }
  return clone(reportItemDemoSubjectsByLedger[ledgerId] || [])
}
