package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 财务科目 / 报表错误码
 */
public interface ErrorCodeConstantsFinanceReport {

    ErrorCode FINANCE_SUBJECT_NOT_EXISTS = new ErrorCode(1_030_611_000, "财务科目不存在");
    ErrorCode FINANCE_SUBJECT_CODE_DUPLICATE = new ErrorCode(1_030_611_001, "账簿({})下科目编码({})已存在");
    ErrorCode FINANCE_SUBJECT_NOT_ENABLE = new ErrorCode(1_030_611_002, "财务科目({})未启用");
    ErrorCode FINANCE_SUBJECT_DELETE_FAIL_USED_BY_REPORT = new ErrorCode(1_030_611_003, "财务科目({})已被报表项目引用，无法删除");
    ErrorCode FINANCE_SUBJECT_NOT_LEAF = new ErrorCode(1_030_611_004, "财务科目({})不是末级科目，不能用于凭证模板");

    ErrorCode FINANCE_REPORT_ITEM_NOT_EXISTS = new ErrorCode(1_030_612_000, "财务报表项目不存在");
    ErrorCode FINANCE_REPORT_ITEM_CODE_DUPLICATE = new ErrorCode(1_030_612_001, "账簿({})下报表项目编码({})已存在");
    ErrorCode FINANCE_REPORT_ITEM_SUBJECT_EMPTY = new ErrorCode(1_030_612_002, "财务报表项目取数科目不能为空");
    ErrorCode FINANCE_REPORT_ITEM_SUBJECT_NOT_EXISTS = new ErrorCode(1_030_612_003, "报表项目引用的科目({})不存在");
    ErrorCode FINANCE_REPORT_ITEM_SUBJECT_LEDGER_MISMATCH = new ErrorCode(1_030_612_004, "报表项目引用的科目({})不属于当前账簿");
    ErrorCode FINANCE_REPORT_ITEM_AMOUNT_SIGN_INVALID = new ErrorCode(1_030_612_005, "报表项目取数金额符号只能为 1 或 -1");
    ErrorCode FINANCE_REPORT_ITEM_CATEGORY_MISMATCH = new ErrorCode(1_030_612_006, "报表项目分类({})不适用于当前报表类型({})");
}
