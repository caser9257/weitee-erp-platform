package cn.weitee.erp.module.system.service.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入结果站内信通知通用发送器
 *
 * 覆盖系统全部 Excel 导入入口（产品/研发BOM/合同/用户/部门），
 * 在导入 Service 返回结果前同步发送站内信给操作人本人。
 *
 * 设计约束：
 * 1. 失败不阻断导入主流程：发送异常只 log，绝不抛出；
 * 2. 收件人 = 操作人本人（SecurityFrameworkUtils.getLoginUserId()）；
 * 3. 无登录上下文（如系统任务）不发送；
 * 4. 事务感知（AGENTS 14.3 铁律 1 机制性防护）：调用方处于活动事务时，
 *    发送自动推迟到 afterCommit，调用方无需关心自身事务边界；
 *    事务回滚则不发送（导入未生效，不应通知）。
 */
@Slf4j
@Component
public class ImportNotifyHelper {

    /** 单条失败明细限长：防超长 reason 撑爆 template_params（兜底限长，见 buildFailSample） */
    private static final int FAIL_SAMPLE_LINE_MAX = 160;

    /** 失败摘要总长上限：JSON 序列化后须小于 template_params 列宽（1024） */
    private static final int FAIL_SAMPLE_TOTAL_MAX = 800;

    private static final String TRUNCATE_SUFFIX = "…（内容过长已截断）";

    @Resource
    private NotifySendService notifySendService;

    /**
     * 发送导入结果站内信
     *
     * @param templateCode 站内信模板编码（如 erp_import_result_product）
     * @param scene        场景名（如 "产品导入"），用于模板占位 {scene}
     * @param totalCount   总行数
     * @param successCount 成功行数
     * @param failCount    失败行数
     * @param failSamples  失败摘要（每行一条，建议格式："第N行 条码X：原因"）；由调用方从 failDetails 映射，本方法只负责取前 3 条拼接
     */
    public void sendImportResult(String templateCode, String scene,
                                 int totalCount, int successCount, int failCount,
                                 List<String> failSamples) {
        sendImportResultInternal(templateCode, scene, totalCount, successCount, failCount, failSamples, false);
    }

    /**
     * 发送包含完整明细的导入结果站内信。
     *
     * 仅用于需要在站内信中逐条核对导入结果的业务；调用前须确保
     * system_notify_message.template_params 已升级为 MEDIUMTEXT。
     */
    public void sendImportResultWithFullDetails(String templateCode, String scene,
                                                int totalCount, int successCount, int failCount,
                                                List<String> failSamples) {
        sendImportResultInternal(templateCode, scene, totalCount, successCount, failCount, failSamples, true);
    }

    private void sendImportResultInternal(String templateCode, String scene,
                                          int totalCount, int successCount, int failCount,
                                          List<String> failSamples, boolean fullDetails) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 事务内：推迟到提交后发送，避免事务内外部调用（铁律 1）；回滚则不发送
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(templateCode, scene, totalCount, successCount, failCount, failSamples, fullDetails);
                }
            });
            return;
        }
        doSend(templateCode, scene, totalCount, successCount, failCount, failSamples, fullDetails);
    }

    private void doSend(String templateCode, String scene,
                        int totalCount, int successCount, int failCount,
                        List<String> failSamples, boolean fullDetails) {
        try {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return; // 无登录上下文（如系统任务）不发送
            }
            String failSample = fullDetails ? buildFullFailSample(failSamples) : buildFailSample(failSamples);
            notifySendService.sendSingleNotifyToAdmin(userId, templateCode, Map.of(
                    "scene", scene,
                    "totalCount", String.valueOf(totalCount),
                    "successCount", String.valueOf(successCount),
                    "failCount", String.valueOf(failCount),
                    "failSample", failSample,
                    "detailUrl", ""
            ));
        } catch (Exception e) {
            log.error("[sendImportResult] 导入站内信发送失败，templateCode={}", templateCode, e);
            // 通知失败绝不阻断导入主流程
        }
    }

    /**
     * 失败摘要：取前 10 条，每条独立一行（站内信中逐行展示，便于复制逐条修改）；
     * 超出截断并提示总条数；全部成功时为空串。
     * 兜底限长：template_params 列宽有限（历史 255，现 1024），单条失败 reason 可能极长，
     * 必须按字符数硬截断，防止 INSERT Data too long 导致整条站内信落库失败被吞。
     */
    private String buildFailSample(List<String> failSamples) {
        if (CollUtil.isEmpty(failSamples)) {
            return "";
        }
        List<String> limited = failSamples.stream()
                .limit(10)
                .map(s -> StrUtil.maxLength(s, FAIL_SAMPLE_LINE_MAX))
                .collect(Collectors.toList());
        String joined = String.join("\n", limited);
        if (joined.length() > FAIL_SAMPLE_TOTAL_MAX) {
            joined = StrUtil.sub(joined, 0, FAIL_SAMPLE_TOTAL_MAX - TRUNCATE_SUFFIX.length()) + TRUNCATE_SUFFIX;
        }
        return failSamples.size() > 10
                ? joined + "\n…等共 " + failSamples.size() + " 条失败"
                : joined;
    }

    private String buildFullFailSample(List<String> failSamples) {
        return CollUtil.isEmpty(failSamples) ? "" : String.join("\n", failSamples);
    }

}
