package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 财务凭证错误码
 */
public interface ErrorCodeConstantsFinanceVoucher {

    ErrorCode FINANCE_VOUCHER_TEMPLATE_NOT_EXISTS = new ErrorCode(1_030_609_000, "凭证模板不存在");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_NOT_ENABLE = new ErrorCode(1_030_609_001, "凭证模板({})未启用");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_ITEM_EMPTY = new ErrorCode(1_030_609_002, "凭证模板分录不能为空");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_SUBJECT_REQUIRED = new ErrorCode(1_030_609_003, "凭证模板分录科目不能为空");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_LEDGER_MISMATCH = new ErrorCode(1_030_609_004, "凭证模板({})与账簿不一致");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_BIZ_MISMATCH = new ErrorCode(1_030_609_005, "凭证模板({})与业务类型不一致");
    ErrorCode FINANCE_VOUCHER_TEMPLATE_AMOUNT_SOURCE_VALUE_REQUIRED = new ErrorCode(1_030_609_006, "凭证模板分录金额来源值不能为空");

    ErrorCode FINANCE_VOUCHER_NOT_EXISTS = new ErrorCode(1_030_610_000, "财务凭证不存在");
    ErrorCode FINANCE_VOUCHER_ALREADY_EXISTS = new ErrorCode(1_030_610_001, "业务单据({})在账簿({})下已生成凭证");
    ErrorCode FINANCE_VOUCHER_SOURCE_NOT_SUPPORTED = new ErrorCode(1_030_610_002, "当前业务类型暂不支持生成财务凭证");
    ErrorCode FINANCE_VOUCHER_SOURCE_STATUS_INVALID = new ErrorCode(1_030_610_003, "业务单据({})当前状态不允许生成财务凭证");
    ErrorCode FINANCE_VOUCHER_UNBALANCED = new ErrorCode(1_030_610_004, "凭证借贷不平，借方({})，贷方({})");
    ErrorCode FINANCE_VOUCHER_APPROVE_FAIL_STATUS = new ErrorCode(1_030_610_005, "财务凭证({})当前状态不允许审核");
    ErrorCode FINANCE_VOUCHER_POST_FAIL_STATUS = new ErrorCode(1_030_610_006, "财务凭证({})当前状态不允许过账");
    ErrorCode FINANCE_VOUCHER_REVERSE_FAIL_STATUS = new ErrorCode(1_030_610_007, "财务凭证({})当前状态不允许冲销");
    ErrorCode FINANCE_VOUCHER_REVERSE_ALREADY_EXISTS = new ErrorCode(1_030_610_008, "财务凭证({})已生成冲销凭证");
    ErrorCode FINANCE_VOUCHER_REVERSE_OF_REVERSE_NOT_ALLOWED = new ErrorCode(1_030_610_009, "冲销凭证({})不允许再次冲销");
    ErrorCode FINANCE_VOUCHER_CANCEL_APPROVE_FAIL_STATUS = new ErrorCode(1_030_610_010, "财务凭证({})当前状态不允许反审核");
    ErrorCode FINANCE_VOUCHER_CANCEL_POST_FAIL_STATUS = new ErrorCode(1_030_610_011, "财务凭证({})当前状态不允许反过账");
    ErrorCode FINANCE_VOUCHER_CANCEL_POST_REVERSE_NOT_ALLOWED = new ErrorCode(1_030_610_012, "冲销凭证({})不允许反过账");
    ErrorCode FINANCE_VOUCHER_CANCEL_POST_BALANCE_NOT_EXISTS = new ErrorCode(1_030_610_013, "财务凭证({})反过账失败，科目余额不存在");
    ErrorCode FINANCE_VOUCHER_SOURCE_TIME_REQUIRED = new ErrorCode(1_030_610_014, "业务单据({})缺少可用的业务日期，请先补充或手工指定凭证日期");
}
