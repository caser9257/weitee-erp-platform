package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

/**
 * 财务账簿 / 会计期间错误码
 */
public interface ErrorCodeConstantsFinanceLedger {

    ErrorCode FINANCE_LEDGER_NOT_EXISTS = new ErrorCode(1_030_607_000, "财务账簿不存在");
    ErrorCode FINANCE_LEDGER_NOT_ENABLE = new ErrorCode(1_030_607_001, "财务账簿({})未启用");
    ErrorCode FINANCE_LEDGER_NO_DUPLICATE = new ErrorCode(1_030_607_002, "财务账簿编码({})已存在");
    ErrorCode FINANCE_LEDGER_DELETE_FAIL_PERIOD_EXISTS = new ErrorCode(1_030_607_003, "财务账簿({})下已存在会计期间，无法删除");
    ErrorCode FINANCE_LEDGER_DEFAULT_MUST_ENABLE = new ErrorCode(1_030_607_004, "默认账簿必须为启用状态");
    ErrorCode FINANCE_LEDGER_DEFAULT_DUPLICATE = new ErrorCode(1_030_607_005, "存在多个启用的默认账簿，请先修正数据");
    ErrorCode FINANCE_DUAL_LEDGER_CONFIG_NOT_EXISTS = new ErrorCode(1_030_607_006, "双账套账簿映射不存在");
    ErrorCode FINANCE_DUAL_LEDGER_CONFIG_BIZ_TYPE_DUPLICATE = new ErrorCode(1_030_607_007, "业务类型({})的双账套账簿映射已存在");
    ErrorCode FINANCE_DUAL_LEDGER_CONFIG_LEDGER_SAME = new ErrorCode(1_030_607_008, "对外账与内部账不能为同一账簿");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_NOT_EXISTS = new ErrorCode(1_030_607_009, "双账套差异项口径配置不存在");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_BIZ_ITEM_DUPLICATE = new ErrorCode(1_030_607_010, "业务类型({})下差异项({})的双账套口径配置已存在");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_SAME = new ErrorCode(1_030_607_011, "对外账与内部账的口径来源不能完全相同");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_REQUIRED = new ErrorCode(1_030_607_012, "当前口径来源必须配置有效的来源值");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_SOURCE_VALUE_FORBIDDEN = new ErrorCode(1_030_607_013, "当前口径来源不允许配置来源值");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_DEPRECIATION_SOURCE_ITEM_INVALID = new ErrorCode(1_030_607_014, "固定资产折旧来源只能用于“折旧”差异项");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_RATIO_INVALID = new ErrorCode(1_030_607_015, "比例系数必须大于 1，才能保证内账金额小于外账金额");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CONFIG_FIXED_AMOUNT_INVALID = new ErrorCode(1_030_607_016, "固定差额必须小于 0，才能保证内账金额小于外账金额");
    ErrorCode FINANCE_DUAL_LEDGER_DIFF_CALCULATION_DIRECTION_INVALID = new ErrorCode(1_030_607_017, "规则计算结果不符合约束：内账金额({})必须小于外账金额({})");

    ErrorCode FINANCE_PERIOD_NOT_EXISTS = new ErrorCode(1_030_608_000, "会计期间不存在");
    ErrorCode FINANCE_PERIOD_ALREADY_EXISTS = new ErrorCode(1_030_608_001, "账簿({})下会计期间({})已存在");
    ErrorCode FINANCE_PERIOD_CLOSE_FAIL_ALREADY_CLOSED = new ErrorCode(1_030_608_002, "会计期间({})已关闭");
    ErrorCode FINANCE_PERIOD_REOPEN_FAIL_ALREADY_OPEN = new ErrorCode(1_030_608_003, "会计期间({})当前不是已关闭状态");
    ErrorCode FINANCE_PERIOD_CLOSE_FAIL_EARLIER_OPEN = new ErrorCode(1_030_608_004, "会计期间({})前仍有未关账月份，无法跳关");
    ErrorCode FINANCE_PERIOD_REOPEN_FAIL_LATER_CLOSED = new ErrorCode(1_030_608_005, "会计期间({})后续已有已关闭月份，无法直接反关账");
    ErrorCode FINANCE_PERIOD_CURRENT_OPEN_NOT_EXISTS = new ErrorCode(1_030_608_006, "账簿({})在业务日期({})下没有可用的打开期间");

}
