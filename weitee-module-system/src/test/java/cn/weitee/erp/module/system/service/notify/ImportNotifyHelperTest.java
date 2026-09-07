package cn.weitee.erp.module.system.service.notify;

import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ImportNotifyHelper 事务感知行为单测（AGENTS 14.3 铁律 1 机制性防护）。
 *
 * 无事务 → 立即发送；活动事务内 → 推迟到 afterCommit；事务回滚 → 不发送。
 */
@ExtendWith(MockitoExtension.class)
class ImportNotifyHelperTest {

    @Mock
    private NotifySendService notifySendService;

    @InjectMocks
    private ImportNotifyHelper importNotifyHelper;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<java.util.Map<String, Object>> paramsCaptor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sendImportResult_oversizedFailSamples_shouldTruncateToColumnLimit() {
        setLoginUserId(1L);
        // 模拟真实导入失败：10 条超长失败明细（每条 500 字符），历史版本直接拼接
        // 导致 template_params(原 255) INSERT Data too long，站内信被静默吞掉
        List<String> huge = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            huge.add("第" + (i + 1) + "行 ABC-123：物料校验失败原因" + "长".repeat(500));
        }

        importNotifyHelper.sendImportResult("erp_import_result_product", "产品导入",
                10, 0, 10, huge);

        verify(notifySendService, times(1))
                .sendSingleNotifyToAdmin(eq(1L), eq("erp_import_result_product"), paramsCaptor.capture());
        String failSample = String.valueOf(paramsCaptor.getValue().get("failSample"));
        // JSON 序列化后总长必须小于 template_params 列宽（1024），留余量断言 800
        assertTrue(failSample.length() <= 800, "failSample 超长：" + failSample.length());
        assertTrue(failSample.contains("已截断"));
    }

    @Test
    void sendImportResultWithFullDetails_shouldKeepEveryDetail() {
        setLoginUserId(1L);
        List<String> details = new java.util.ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            details.add("第" + i + "行 MAT-" + i + "：[警告] 元器件未填写位号");
        }
        details.add("产品编码重复，请再次核对清单后进行提交：MAT-18（第18、19行）");

        importNotifyHelper.sendImportResultWithFullDetails("erp_import_result_rd_bom", "研发BOM导入",
                19, 18, 0, details);

        verify(notifySendService, times(1))
                .sendSingleNotifyToAdmin(eq(1L), eq("erp_import_result_rd_bom"), paramsCaptor.capture());
        String failSample = String.valueOf(paramsCaptor.getValue().get("failSample"));
        assertEquals(String.join("\n", details), failSample);
        assertTrue(failSample.contains("第18行 MAT-18"));
        assertTrue(failSample.contains("产品编码重复，请再次核对清单后进行提交"));
        assertTrue(!failSample.contains("…等共"));
    }

    @Test
    void sendImportResult_noTransaction_shouldSendImmediately() {
        setLoginUserId(1L);

        importNotifyHelper.sendImportResult("erp_import_result_product", "产品导入",
                3, 2, 1, List.of("第2行 X：原因"));

        verify(notifySendService, times(1))
                .sendSingleNotifyToAdmin(eq(1L), eq("erp_import_result_product"), anyMap());
    }

    @Test
    void sendImportResult_insideTransaction_shouldDeferToAfterCommit() {
        setLoginUserId(1L);
        TransactionSynchronizationManager.initSynchronization();
        try {
            importNotifyHelper.sendImportResult("erp_import_result_product", "产品导入",
                    1, 1, 0, List.of());

            // 事务内绝不发送（铁律 1）
            verify(notifySendService, never()).sendSingleNotifyToAdmin(any(), any(), any());

            // 模拟事务提交：afterCommit 触发发送
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }
            verify(notifySendService, times(1))
                    .sendSingleNotifyToAdmin(eq(1L), eq("erp_import_result_product"), anyMap());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sendImportResult_rolledBackTransaction_shouldNeverSend() {
        setLoginUserId(1L);
        TransactionSynchronizationManager.initSynchronization();
        importNotifyHelper.sendImportResult("erp_import_result_product", "产品导入",
                1, 0, 1, List.of());
        // 模拟回滚：不触发 afterCommit，直接清理同步状态
        TransactionSynchronizationManager.clearSynchronization();

        verify(notifySendService, never()).sendSingleNotifyToAdmin(any(), any(), any());
    }

    private void setLoginUserId(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

}
