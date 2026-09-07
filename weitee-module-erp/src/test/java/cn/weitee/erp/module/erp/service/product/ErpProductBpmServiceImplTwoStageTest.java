package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_CANCEL_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_BPM_SUBMIT_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NO_CHANGES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 产品两段式变更/废除提交链路单元测试（ErpProductBpmServiceImpl）
 *
 * 核心验证点：
 * - 审批流守卫：审批流中状态禁止再次流转阶段
 * - 状态路由：当前状态不匹配期望状态 → 拒绝
 * - CAS 锁：update 返回 0 → 拒绝
 * - 正常提交：afterCommit 内调 BPM submit，场景码正确
 * - 撤回：按当前审批流状态路由到对应场景码
 * - 阶段二提交前校验暂存快照存在
 */
@ExtendWith(MockitoExtension.class)
class ErpProductBpmServiceImplTwoStageTest {

    @Mock
    private ErpProductMapper productMapper;
    @Mock
    private BpmApprovalRuntimeService approvalRuntimeService;
    @Mock
    private ErpProductPendingChangeService pendingChangeService;
    @InjectMocks
    private ErpProductBpmServiceImpl service;

    private static final long PRODUCT_ID = 100L;
    private static final long USER_ID = 1L;
    private static final String PROCESS_INSTANCE_ID = "pi-12345";

    private final AtomicReference<ErpProductDO> current = new AtomicReference<>();

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), ErpProductDO.class);
    }

    @BeforeEach
    void setUp() {
        current.set(product(ErpAuditStatus.APPROVE.getStatus()));
        // lenient：部分用例（如 pending 为 null 时先抛异常）不走到 selectById
        org.mockito.Mockito.lenient().when(productMapper.selectById(PRODUCT_ID)).thenAnswer(inv -> current.get());
        // 默认 CAS 成功；个别用例覆盖返回 0（lenient：守卫/异常用例可能不走到 update）
        org.mockito.Mockito.lenient().when(productMapper.update(any(), any())).thenReturn(1);
    }

    private ErpProductDO product(Integer auditStatus) {
        return new ErpProductDO()
                .setId(PRODUCT_ID)
                .setName("测试物料")
                .setMaterialCode("MAT-TEST-001")
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAuditStatus(auditStatus)
                .setProcessInstanceId(null);
    }

    private void expectSubmitFail(Integer auditStatus, Executable action) {
        current.set(product(auditStatus));
        ServiceException ex = assertThrows(ServiceException.class, action);
        assertEquals(PRODUCT_BPM_SUBMIT_FAIL.getCode(), ex.getCode());
    }

    private void expectIllegal(Integer auditStatus, Executable action) {
        current.set(product(auditStatus));
        ServiceException ex = assertThrows(ServiceException.class, action);
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    // ========== 审批流守卫：审批流中禁止再次流转 ==========

    @Test
    void submitChangeRequest_whenInCrPending_shouldThrow() {
        expectSubmitFail(ErpAuditStatus.CR_PENDING.getStatus(),
                () -> service.submitChangeRequest(USER_ID, PRODUCT_ID, "重复"));
    }

    @Test
    void submitChangeRequest_whenInConfirmPending_shouldThrow() {
        expectSubmitFail(ErpAuditStatus.CONFIRM_PENDING.getStatus(),
                () -> service.submitChangeRequest(USER_ID, PRODUCT_ID, "重复"));
    }

    @Test
    void submitObsoleteRequest_whenInStopPending_shouldThrow() {
        expectSubmitFail(ErpAuditStatus.STOP_PENDING.getStatus(),
                () -> service.submitObsoleteRequest(USER_ID, PRODUCT_ID, "重复"));
    }

    @Test
    void submitChangeConfirm_whenInCrPending_shouldThrow() {
        // submitChangeConfirm 先校验 pending 存在，需要 stub
        org.mockito.Mockito.lenient().when(pendingChangeService.getPendingChange(PRODUCT_ID))
                .thenReturn(new ErpProductPendingChangeDO().setStatus(ErpProductPendingChangeDO.STATUS_PENDING));
        expectSubmitFail(ErpAuditStatus.CR_PENDING.getStatus(),
                () -> service.submitChangeConfirm(USER_ID, PRODUCT_ID, "重复"));
    }

    // ========== 状态路由非法 ==========

    @Test
    void submitChangeRequest_whenDraft_shouldThrow() {
        // submitChangeRequest 期望 APPROVE；DRAFT 不匹配
        expectIllegal(ErpAuditStatus.DRAFT.getStatus(),
                () -> service.submitChangeRequest(USER_ID, PRODUCT_ID, null));
    }

    @Test
    void submitChangeConfirm_whenApprove_shouldThrow() {
        // submitChangeConfirm 期望 EDITING；APPROVE 不匹配
        org.mockito.Mockito.lenient().when(pendingChangeService.getPendingChange(PRODUCT_ID))
                .thenReturn(new ErpProductPendingChangeDO().setStatus(ErpProductPendingChangeDO.STATUS_PENDING));
        expectIllegal(ErpAuditStatus.APPROVE.getStatus(),
                () -> service.submitChangeConfirm(USER_ID, PRODUCT_ID, null));
    }

    @Test
    void submitStatusChange_whenEditing_shouldThrow() {
        // submitStatusChange 仅允许 APPROVE 态发起；EDITING 走状态非法分支
        expectIllegal(ErpAuditStatus.EDITING.getStatus(),
                () -> service.submitStatusChange(USER_ID, PRODUCT_ID, 1, null));
    }

    // ========== CAS 锁失败 ==========

    @Test
    void submitChangeRequest_whenCasConflict_shouldThrow() {
        when(productMapper.update(any(), any())).thenReturn(0);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitChangeRequest(USER_ID, PRODUCT_ID, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    // ========== 正常提交 ==========

    @Test
    void submitChangeRequest_shouldSubmitChangeRequestScene() {
        when(approvalRuntimeService.submit(any(), anyLong(), anyLong())).thenReturn(PROCESS_INSTANCE_ID);
        service.submitChangeRequest(USER_ID, PRODUCT_ID, "变更原因");
        verify(approvalRuntimeService).submit(
                "erp.product.change.request", PRODUCT_ID, USER_ID);
    }

    @Test
    void submitChangeConfirm_shouldSubmitChangeConfirmScene() {
        current.set(product(ErpAuditStatus.EDITING.getStatus()));
        when(pendingChangeService.getPendingChange(PRODUCT_ID))
                .thenReturn(new ErpProductPendingChangeDO().setStatus(ErpProductPendingChangeDO.STATUS_PENDING));
        when(approvalRuntimeService.submit(any(), anyLong(), anyLong())).thenReturn(PROCESS_INSTANCE_ID);
        service.submitChangeConfirm(USER_ID, PRODUCT_ID, "变更完成");
        verify(approvalRuntimeService).submit(
                "erp.product.change.confirm", PRODUCT_ID, USER_ID);
    }

    @Test
    void submitChangeConfirm_whenNoPending_shouldThrowAndNotSubmit() {
        current.set(product(ErpAuditStatus.EDITING.getStatus()));
        when(pendingChangeService.getPendingChange(PRODUCT_ID)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitChangeConfirm(USER_ID, PRODUCT_ID, null));
        assertEquals(PRODUCT_NO_CHANGES.getCode(), ex.getCode());
        verify(approvalRuntimeService, never()).submit(any(), anyLong(), anyLong());
    }

    @Test
    void submitObsoleteRequest_shouldSubmitObsoleteRequestScene() {
        when(approvalRuntimeService.submit(any(), anyLong(), anyLong())).thenReturn(PROCESS_INSTANCE_ID);
        service.submitObsoleteRequest(USER_ID, PRODUCT_ID, "废除原因");
        verify(approvalRuntimeService).submit(
                "erp.product.obsolete.request", PRODUCT_ID, USER_ID);
    }

    @Test
    void submitStatusChange_shouldSubmitStatusChangeScene() {
        // 测试产品 status=0(ENABLE)，目标停用(1) → diff 有变更
        when(pendingChangeService.diffProduct(any(), any()))
                .thenReturn(java.util.Map.of("status", 1));
        when(approvalRuntimeService.submit(any(), anyLong(), anyLong())).thenReturn(PROCESS_INSTANCE_ID);
        service.submitStatusChange(USER_ID, PRODUCT_ID, 1, "停用理由");
        verify(approvalRuntimeService).submit(
                "erp.product.status.change", PRODUCT_ID, USER_ID);
    }

    // ========== 撤回：按当前审批流状态路由场景码 ==========

    @Test
    void cancel_whenCrPending_shouldCancelChangeRequestScene() {
        current.set(product(ErpAuditStatus.CR_PENDING.getStatus()).setProcessInstanceId(PROCESS_INSTANCE_ID));
        service.cancelTwoStageApproval(USER_ID, PRODUCT_ID, "撤回");
        verify(approvalRuntimeService).cancel(
                "erp.product.change.request", PRODUCT_ID, USER_ID, "撤回");
    }

    @Test
    void cancel_whenStopPending_shouldCancelStatusChangeScene() {
        current.set(product(ErpAuditStatus.STOP_PENDING.getStatus()).setProcessInstanceId(PROCESS_INSTANCE_ID));
        service.cancelTwoStageApproval(USER_ID, PRODUCT_ID, null);
        verify(approvalRuntimeService).cancel(
                "erp.product.status.change", PRODUCT_ID, USER_ID, "发起人撤回");
    }

    @Test
    void cancel_whenNotPending_shouldThrow() {
        current.set(product(ErpAuditStatus.APPROVE.getStatus()));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.cancelTwoStageApproval(USER_ID, PRODUCT_ID, null));
        assertEquals(PRODUCT_BPM_CANCEL_FAIL.getCode(), ex.getCode());
    }

    @Test
    void cancel_whenPendingButNoProcessInstance_shouldThrow() {
        current.set(product(ErpAuditStatus.CR_PENDING.getStatus())); // processInstanceId 为空
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.cancelTwoStageApproval(USER_ID, PRODUCT_ID, null));
        assertEquals(PRODUCT_BPM_CANCEL_FAIL.getCode(), ex.getCode());
    }

}