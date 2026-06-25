package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

/**
 * 财务双写错误码
 */
public interface ErrorCodeConstantsFinanceDualWrite {

    ErrorCode DUAL_WRITE_LOG_NOT_EXISTS = new ErrorCode(1_030_620_000, "双写日志不存在");
    ErrorCode DUAL_WRITE_RETRY_STATUS_INVALID = new ErrorCode(1_030_620_001, "只有失败状态的双写日志才能重试");
    ErrorCode DUAL_WRITE_RETRY_FAILED = new ErrorCode(1_030_620_002, "双写重试失败：{}");
    ErrorCode DUAL_WRITE_CONFIG_NOT_EXISTS = new ErrorCode(1_030_620_003, "双写配置不存在");
    ErrorCode DUAL_WRITE_CONFIG_ALREADY_EXISTS = new ErrorCode(1_030_620_004, "该账簿已存在双写配置");
}
