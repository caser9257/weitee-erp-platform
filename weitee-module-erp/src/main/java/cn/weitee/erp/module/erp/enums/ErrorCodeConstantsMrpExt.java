package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

public interface ErrorCodeConstantsMrpExt {

    ErrorCode PRODUCTION_MAN_HOUR_NOT_EXISTS = new ErrorCode(1_030_700_037, "生产工时归集记录不存在");
    ErrorCode PRODUCTION_MAN_HOUR_INVALID = new ErrorCode(1_030_700_038, "生产工时必须大于 0");
    ErrorCode PRODUCTION_COST_ALLOCATION_RULE_NOT_EXISTS = new ErrorCode(1_030_700_039, "生产成本分摊规则不存在");
    ErrorCode PRODUCTION_COST_ALLOCATION_RULE_DISABLED = new ErrorCode(1_030_700_040, "当前生产成本分摊规则已停用");
    ErrorCode PRODUCTION_COST_ALLOCATION_NOT_EXISTS = new ErrorCode(1_030_700_041, "生产成本分摊单不存在");
    ErrorCode PRODUCTION_COST_ALLOCATION_STATUS_INVALID = new ErrorCode(1_030_700_042, "当前生产成本分摊单状态不允许执行该操作");
    ErrorCode PRODUCTION_COST_ALLOCATION_BASIS_INVALID = new ErrorCode(1_030_700_043, "当前分摊基准不合法或暂不支持");
    ErrorCode PRODUCTION_COST_ALLOCATION_BASIS_EMPTY = new ErrorCode(1_030_700_044, "当前分摊月份没有可用的分摊基数数据");
    ErrorCode PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED = new ErrorCode(1_030_700_045, "当前生产成本分摊单已执行，请勿重复执行");
    ErrorCode PRODUCTION_COST_ALLOCATION_RULE_TYPE_MISMATCH = new ErrorCode(1_030_700_046, "分摊单的成本类型与规则不匹配");
    ErrorCode PRODUCTION_ACCOUNTING_MONTH_INVALID = new ErrorCode(1_030_700_047, "核算月份格式不正确，或工时日期与核算月份不一致");
    ErrorCode PRODUCTION_MAN_HOUR_MONTH_LOCKED = new ErrorCode(1_030_700_048, "当前核算月份已有已执行的成本分摊，不允许修改工时");
    ErrorCode PRODUCTION_COST_ALLOCATION_DUPLICATED = new ErrorCode(1_030_700_049, "当前核算月份和成本类型的分摊单已存在");

}
