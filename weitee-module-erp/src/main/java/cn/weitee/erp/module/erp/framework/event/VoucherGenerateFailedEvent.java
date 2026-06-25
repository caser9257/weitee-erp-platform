package cn.weitee.erp.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 凭证生成失败事件
 *
 * 当业务单据审核通过后，自动生成凭证失败时触发
 */
@Getter
@AllArgsConstructor
public class VoucherGenerateFailedEvent {

    /**
     * 业务类型
     */
    private final Integer bizType;

    /**
     * 业务单据编号
     */
    private final Long bizId;

    /**
     * 错误信息
     */
    private final String errorMessage;

    /**
     * 异常堆栈（简化版）
     */
    private final String errorStack;

}
