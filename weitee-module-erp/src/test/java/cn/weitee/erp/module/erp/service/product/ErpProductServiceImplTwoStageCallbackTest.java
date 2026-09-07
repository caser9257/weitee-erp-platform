package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 产品两段式回调状态机流转测试（ErpProductServiceImpl.completeXxx）
 *
 * 通过解析 LambdaUpdateWrapper 的 SET 子句值，精确断言状态机流转目标：
 * - completeChangeRequest：CR_PENDING → EDITING（通过）/ APPROVE（驳回）
 * - completeChangeConfirm：CONFIRM_PENDING → APPROVE（通过）/ EDITING（驳回）
 * - completeObsoleteConfirm：OBSOLETE_CONFIRM_PENDING → OBSOLETED（废除留痕落库）
 */
@ExtendWith(MockitoExtension.class)
class ErpProductServiceImplTwoStageCallbackTest {

    @Mock
    private ErpProductMapper erpProductMapper;
    @Mock
    private ErpProductPendingChangeService pendingChangeService;
    @InjectMocks
    private ErpProductServiceImpl service;

    private static final long ID = 100L;
    private static final String PI = "pi-12345";

    private final AtomicReference<ErpProductDO> current = new AtomicReference<>();

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), ErpProductDO.class);
    }

    @BeforeEach
    void setUp() {
        current.set(product(ErpAuditStatus.CR_PENDING.getStatus()));
        when(erpProductMapper.selectById(ID)).thenAnswer(inv -> current.get());
        // lenient：状态不符/守卫用例可能不走到 update
        org.mockito.Mockito.lenient().when(erpProductMapper.update(any(), any())).thenReturn(1);
    }

    private ErpProductDO product(Integer auditStatus) {
        return new ErpProductDO()
                .setId(ID)
                .setName("测试物料")
                .setMaterialCode("MAT-TEST-001")
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAuditStatus(auditStatus)
                .setProcessInstanceId(PI);
    }

    /**
     * 从 LambdaUpdateWrapper 解析 SET 子句中指定列的目标值。
     * MyBatis-Plus 的 getSqlSet() 形如 "audit_status=#{ew.paramNameValuePairs.MPGENVAL1}",
     * 实际值在 getParamNameValuePairs() 中，key 为 "paramNameValuePairs.MPGENVAL1"。
     */
    private Object resolveSetValue(LambdaUpdateWrapper<ErpProductDO> wrapper, String column) {
        String sqlSet = wrapper.getSqlSet();
        Map<String, Object> pairs = wrapper.getParamNameValuePairs();
        if (sqlSet == null) {
            return null;
        }
        // 匹配 "列名=#{ew.xxx.MPGENVALx}"，列名必须是完整匹配（前导逗号或字符串开头），避免误匹配 audit_status 等子串
        Pattern p = Pattern.compile("(?:^|,\\s*)" + column + "=#\\{ew\\.([^}]+)\\}");
        Matcher m = p.matcher(sqlSet);
        if (m.find()) {
            String placeholder = m.group(1);
            Object v = pairs.get(placeholder);
            if (v == null && placeholder.contains(".")) {
                v = pairs.get(placeholder.substring(placeholder.lastIndexOf('.') + 1));
            }
            return v;
        }
        System.err.println("[resolveSetValue] column=" + column + " not matched. sqlSet=[" + sqlSet
                + "] pairs=" + pairs);
        return null;
    }

    private LambdaUpdateWrapper<ErpProductDO> captureUpdateWrapper() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaUpdateWrapper<ErpProductDO>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(erpProductMapper).update(any(), captor.capture());
        return captor.getValue();
    }

    // ========== 变更阶段一 ==========

    @Test
    void completeChangeRequest_approved_shouldSetEditing() {
        service.completeChangeRequest(ID, PI, true, "通过");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.EDITING.getStatus(),
                resolveSetValue(wrapper, "audit_status"));
    }

    @Test
    void completeChangeRequest_rejected_shouldSetApprove() {
        service.completeChangeRequest(ID, PI, false, "驳回");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.APPROVE.getStatus(),
                resolveSetValue(wrapper, "audit_status"));
    }

    // ========== 变更阶段二 ==========

    @Test
    void completeChangeConfirm_approved_shouldSetApprove() {
        current.set(product(ErpAuditStatus.CONFIRM_PENDING.getStatus()));
        // 阶段二通过先落暂存（先业务后终态），再流转 APPROVE
        when(pendingChangeService.applyPendingChange(ID, PI, "确认通过")).thenReturn(true);
        service.completeChangeConfirm(ID, PI, true, "确认通过");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.APPROVE.getStatus(),
                resolveSetValue(wrapper, "audit_status"));
    }

    @Test
    void completeChangeConfirm_rejected_shouldSetEditing() {
        current.set(product(ErpAuditStatus.CONFIRM_PENDING.getStatus()));
        service.completeChangeConfirm(ID, PI, false, "驳回");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.EDITING.getStatus(),
                resolveSetValue(wrapper, "audit_status"));
    }

    // ========== 废除阶段一 ==========

    @Test
    void completeObsoleteRequest_approved_shouldSetObsoletedAndTrace() {
        // 废除一段式：审批通过直接落终态（销号留痕+status 停用）
        current.set(product(ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus()));
        service.completeObsoleteRequest(ID, PI, true, "通过");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.OBSOLETED.getStatus(), resolveSetValue(wrapper, "audit_status"));
        assertEquals(Boolean.TRUE, resolveSetValue(wrapper, "abolish_flag"));
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), resolveSetValue(wrapper, "status"));
    }

    @Test
    void completeObsoleteRequest_rejected_shouldSetApprove() {
        current.set(product(ErpAuditStatus.OBSOLETE_CR_PENDING.getStatus()));
        service.completeObsoleteRequest(ID, PI, false, "驳回");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.APPROVE.getStatus(),
                resolveSetValue(wrapper, "audit_status"));
    }

    // ========== 废除阶段二（废除留痕） ==========

    @Test
    void completeStatusChange_approved_shouldApplyPendingThenBackToApprove() {
        // 启停一段式：先业务（暂存落主表），后终态（回 APPROVE）
        current.set(product(ErpAuditStatus.STOP_PENDING.getStatus()));
        when(pendingChangeService.applyPendingChange(ID, PI, "启停通过")).thenReturn(true);
        service.completeStatusChange(ID, PI, true, "启停通过");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), resolveSetValue(wrapper, "audit_status"));
    }

    @Test
    void completeStatusChange_rejected_shouldBackToApproveWithoutChange() {
        current.set(product(ErpAuditStatus.STOP_PENDING.getStatus()));
        service.completeStatusChange(ID, PI, false, "驳回");
        LambdaUpdateWrapper<ErpProductDO> wrapper = captureUpdateWrapper();
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), resolveSetValue(wrapper, "audit_status"));
    }

    @Test
    void completeStatusChange_approved_whenApplyFailed_shouldThrow() {
        // 先业务后终态：暂存落库失败即终止，不落 APPROVE
        current.set(product(ErpAuditStatus.STOP_PENDING.getStatus()));
        when(pendingChangeService.applyPendingChange(ID, PI, null)).thenReturn(false);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.completeStatusChange(ID, PI, true, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    // ========== 守卫：流程实例不匹配 / 状态不符 / CAS 失败 ==========

    @Test
    void completeChangeRequest_whenPiMismatch_shouldThrow() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.completeChangeRequest(ID, "wrong-pi", true, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void completeChangeRequest_whenStatusMismatch_shouldIgnoreWithoutUpdate() {
        // 当前 APPROVE，期望 CR_PENDING → 静默忽略，不抛异常、不更新
        current.set(product(ErpAuditStatus.APPROVE.getStatus()));
        service.completeChangeRequest(ID, PI, true, null); // 不应抛异常
        verify(erpProductMapper, never()).update(any(), any());
    }

    @Test
    void completeChangeConfirm_whenCasFailed_shouldThrow() {
        current.set(product(ErpAuditStatus.CONFIRM_PENDING.getStatus()));
        when(pendingChangeService.applyPendingChange(ID, PI, null)).thenReturn(true);
        when(erpProductMapper.update(any(), any())).thenReturn(0);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.completeChangeConfirm(ID, PI, true, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void completeChangeConfirm_whenApplyPendingChangeFailed_shouldThrow() {
        // 暂存落库失败（先业务后终态）：业务失败即终止，不落 APPROVE 终态
        current.set(product(ErpAuditStatus.CONFIRM_PENDING.getStatus()));
        when(pendingChangeService.applyPendingChange(ID, PI, null)).thenReturn(false);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.completeChangeConfirm(ID, PI, true, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void completeStatusChange_whenPiMismatch_shouldThrow() {
        current.set(product(ErpAuditStatus.STOP_PENDING.getStatus()));
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.completeStatusChange(ID, "wrong-pi", true, null));
        assertEquals(PRODUCT_AUDIT_STATUS_ILLEGAL.getCode(), ex.getCode());
    }
}