package cn.iocoder.yudao.module.erp.framework.listener;

import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.framework.event.VoucherGenerateFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 凭证生成失败监听器
 *
 * 当凭证生成失败时，记录告警日志并发送站内通知
 * 使用 @TransactionalEventListener 确保主事务提交后才执行
 */
@Component
@Slf4j
public class VoucherGenerateFailedListener {

    /**
     * 处理凭证生成失败事件
     *
     * - @Async：异步执行，不阻塞主流程
     * - @TransactionalEventListener(AFTER_COMMIT)：仅在事务提交后触发
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoucherGenerateFailed(VoucherGenerateFailedEvent event) {
        try {
            String bizTypeName = resolveBizTypeName(event.getBizType());

            // 1. 记录告警日志
            log.error("[凭证生成告警] 业务类型={}, 业务单据ID={}, 错误信息={}",
                    bizTypeName, event.getBizId(), event.getErrorMessage());

            // 2. 构建告警参数（可用于后续扩展发送站内通知）
            Map<String, Object> alertParams = buildAlertParams(event, bizTypeName);

            // 3. 输出告警摘要（后续可接入 NotifySendService 发送站内通知）
            log.warn("[凭证生成告警摘要] 业务类型={}, 单据ID={}, 错误={}, 建议操作: 进入财务凭证页面，使用'重算凭证'功能修复",
                    bizTypeName, event.getBizId(), event.getErrorMessage());

            // TODO: 后续可接入 NotifySendService 发送站内通知给财务管理员
            // notifySendService.sendSingleNotifyToAdmin(financeAdminUserId, "voucher_generate_failed", alertParams);

        } catch (Exception e) {
            log.error("[onVoucherGenerateFailed] 处理凭证生成失败事件异常", e);
        }
    }

    /**
     * 解析业务类型名称
     */
    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (value.getType().equals(bizType)) {
                return value.getName();
            }
        }
        return "未知类型(" + bizType + ")";
    }

    /**
     * 构建告警参数
     */
    private Map<String, Object> buildAlertParams(VoucherGenerateFailedEvent event, String bizTypeName) {
        Map<String, Object> params = new HashMap<>();
        params.put("bizType", event.getBizType());
        params.put("bizTypeName", bizTypeName);
        params.put("bizId", event.getBizId());
        params.put("errorMessage", event.getErrorMessage());
        params.put("errorStack", event.getErrorStack());
        params.put("suggestion", "请进入财务凭证页面，使用'重算凭证'功能修复");
        return params;
    }

}
