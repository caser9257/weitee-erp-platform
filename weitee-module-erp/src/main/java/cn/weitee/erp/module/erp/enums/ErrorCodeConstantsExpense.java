package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

/**
 * ERP 费用报销错误码
 */
public interface ErrorCodeConstantsExpense {

    ErrorCode EXPENSE_NOT_EXISTS = new ErrorCode(1_030_607_000, "费用单不存在");
    ErrorCode EXPENSE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_607_001, "费用单({})已审核，无法删除");
    ErrorCode EXPENSE_PROCESS_FAIL = new ErrorCode(1_030_607_002, "反审核失败，只有已审核的费用单才能反审核");
    ErrorCode EXPENSE_APPROVE_FAIL = new ErrorCode(1_030_607_003, "审核失败，只有未审核的费用单才能审核");
    ErrorCode EXPENSE_NO_EXISTS = new ErrorCode(1_030_607_004, "生成费用单号失败，请重新提交");
    ErrorCode EXPENSE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_607_005, "费用单({})已审核，无法修改");
    ErrorCode EXPENSE_TYPE_INVALID = new ErrorCode(1_030_607_006, "费用类型不合法");
    ErrorCode EXPENSE_PROJECT_REQUIRED = new ErrorCode(1_030_607_007, "研发费用必须选择研发项目");
    ErrorCode EXPENSE_SUPPLIER_REQUIRED = new ErrorCode(1_030_607_008, "费用单审核前必须绑定付款对象");
    ErrorCode EXPENSE_ITEM_AMOUNT_INVALID = new ErrorCode(1_030_607_009, "费用明细金额必须大于 0");
    ErrorCode EXPENSE_ITEM_AMOUNT_NOT_MATCH = new ErrorCode(1_030_607_010, "费用明细合计与单据金额不一致");
    ErrorCode EXPENSE_ROLLBACK_FAIL_PAID = new ErrorCode(1_030_607_011, "费用单({})已存在付款或核销记录，不允许反审核");
    ErrorCode EXPENSE_BPM_SUBMIT_FAIL = new ErrorCode(1_030_607_012, "当前费用单不允许提交审批");
    ErrorCode EXPENSE_BPM_CANCEL_FAIL = new ErrorCode(1_030_607_013, "当前费用单不存在可撤回的审批流程");
    ErrorCode EXPENSE_RD_ACCOUNTING_TYPE_REQUIRED = new ErrorCode(1_030_607_014, "研发费用必须选择费用化或资本化口径");
    ErrorCode EXPENSE_RD_ACCOUNTING_TYPE_INVALID = new ErrorCode(1_030_607_015, "当前费用类型不允许设置研发费用化/资本化口径");

    // 费用类型混合方案相关错误码
    ErrorCode EXPENSE_TYPE_NOT_EXISTS = new ErrorCode(1_030_607_016, "费用类型不存在");
    ErrorCode EXPENSE_COST_CENTER_REQUIRED = new ErrorCode(1_030_607_017, "当前费用类型必须选择成本中心");
    ErrorCode EXPENSE_LEASE_CONTRACT_REQUIRED = new ErrorCode(1_030_607_018, "当前费用类型必须选择租赁合同");

}
