package cn.weitee.erp.server.erp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentService;
import cn.weitee.erp.server.WeiteeServerApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P2 预备：付款 BPM 回调重投/乱序集成测试（真实 MySQL + Redis/Redisson）。
 *
 * 默认不执行；手动触发：
 *   mvn -pl weitee-server test -Dtest=ErpFinancePaymentBpmCallbackIntegrationTest -Dp2.ap.it=true
 *
 * 断言目标（完成标准）：付款、撤回、作废、重复执行行为可追踪，且不产生重复核销事实。
 */
@SpringBootTest(
        classes = WeiteeServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "server.port=0",
                "spring.quartz.auto-startup=false",
                "spring.boot.admin.client.enabled=false"
        }
)
@ActiveProfiles("local")
@EnabledIfSystemProperty(named = "p2.ap.it", matches = "true")
class ErpFinancePaymentBpmCallbackIntegrationTest {

    private static final Long TEST_SUPPLIER_ID = 999_999_902L;

    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Resource
    private ErpFinancePaymentItemMapper erpFinancePaymentItemMapper;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    private Long statementId;
    private Long paymentId;

    @BeforeEach
    void mockLoginUser() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setUserType(1);
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

    @AfterEach
    void cleanUpTestData() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        if (paymentId != null) {
            jdbcTemplate.update("DELETE FROM erp_finance_payment_allocate WHERE payment_id = ?", paymentId);
            jdbcTemplate.update("DELETE FROM erp_finance_payment_item WHERE payment_id = ?", paymentId);
            jdbcTemplate.update("DELETE FROM erp_finance_payment WHERE id = ?", paymentId);
        }
        if (statementId != null) {
            jdbcTemplate.update("DELETE FROM erp_ap_statement_item WHERE statement_id = ?", statementId);
            jdbcTemplate.update("DELETE FROM erp_ap_statement WHERE id = ?", statementId);
        }
    }

    private Long prepareStatementAndProcessPayment(String amount) {
        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo("AP-IT-" + UUID.randomUUID())
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(System.nanoTime())
                .setBizNo("EXP-IT-" + UUID.randomUUID())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAmount(new BigDecimal(amount))
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal(amount))
                .setCurrencyCode("CNY")
                .setBizDate(LocalDateTime.now())
                .setDueDate(LocalDateTime.now())
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark("P2 回调集成测试数据");
        erpApStatementMapper.insert(statement);
        statementId = statement.getId();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setNo("FP-IT-" + suffix)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-IT-" + suffix)
                .setPaymentTime(LocalDateTime.now())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAccountId(1L)
                .setTotalPrice(new BigDecimal(amount))
                .setDiscountPrice(BigDecimal.ZERO)
                .setPaymentPrice(new BigDecimal(amount))
                .setRemark("P2 回调集成测试数据");
        erpFinancePaymentMapper.insert(payment);
        paymentId = payment.getId();
        ErpFinancePaymentItemDO item = new ErpFinancePaymentItemDO()
                .setPaymentId(paymentId)
                .setApStatementId(statementId)
                .setBizType(statement.getBizType())
                .setBizId(statement.getBizId())
                .setBizNo(statement.getBizNo())
                .setTotalPrice(statement.getAmount())
                .setPaidPrice(statement.getPaidAmount())
                .setPaymentPrice(new BigDecimal(amount));
        erpFinancePaymentItemMapper.insert(item);
        return paymentId;
    }

    private String processInstanceId(Long paymentId) {
        return erpFinancePaymentMapper.selectById(paymentId).getProcessInstanceId();
    }

    @Test
    void duplicateApproveCallback_shouldCreateExactlyOneAllocateFact() {
        Long id = prepareStatementAndProcessPayment("60.00");
        String pid = processInstanceId(id);

        financePaymentService.updateFinancePaymentStatusByBpm(id, pid, ErpAuditStatus.APPROVE.getStatus(), "ok");
        // 模拟 Flowable 事件重投：第二次同 pid 的 APPROVE 回调必须被忽略
        financePaymentService.updateFinancePaymentStatusByBpm(id, pid, ErpAuditStatus.APPROVE.getStatus(), "ok");

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), erpFinancePaymentMapper.selectById(id).getStatus());
        List<ErpFinancePaymentAllocateDO> approved =
                erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId));
        assertEquals(1, approved.size(), "重复回调不得产生第二条生效核销");
        assertEquals(0, new BigDecimal("60.00").compareTo(erpApStatementMapper.selectById(statementId).getPaidAmount()));
    }

    @Test
    void cancelThenLateApproveCallback_shouldBeRejectedWithoutAllocateFact() {
        Long id = prepareStatementAndProcessPayment("60.00");
        String pid = processInstanceId(id);

        financePaymentService.rollbackFinancePaymentStatusToDraftByBpm(id, pid, "撤回");
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), erpFinancePaymentMapper.selectById(id).getStatus());

        // 撤回后流程实例已解绑，迟到的 APPROVE 回调必须以非法状态拒绝，且不得写入核销事实
        ServiceException ex = assertThrows(ServiceException.class, () ->
                financePaymentService.updateFinancePaymentStatusByBpm(id, pid, ErpAuditStatus.APPROVE.getStatus(), "late"));
        assertEquals(FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
        assertEquals(0, erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size());
        assertEquals(0, BigDecimal.ZERO.compareTo(erpApStatementMapper.selectById(statementId).getPaidAmount()));
    }

    @Test
    void approveThenLateCancelCallback_shouldBeIgnoredAndKeepAllocateFact() {
        Long id = prepareStatementAndProcessPayment("60.00");
        String pid = processInstanceId(id);

        financePaymentService.updateFinancePaymentStatusByBpm(id, pid, ErpAuditStatus.APPROVE.getStatus(), "ok");
        // 审批通过后收到迟到的撤回回退：状态已非 PROCESS，必须忽略，核销事实保留
        financePaymentService.rollbackFinancePaymentStatusToDraftByBpm(id, pid, "late cancel");

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), erpFinancePaymentMapper.selectById(id).getStatus());
        assertEquals(1, erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size());
        ErpApStatementDO statement = erpApStatementMapper.selectById(statementId);
        assertNotNull(statement);
        assertTrue(statement.getPaidAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void voidAfterApprove_shouldReleaseAllocateAndRestoreRemain() {
        Long id = prepareStatementAndProcessPayment("60.00");
        String pid = processInstanceId(id);
        financePaymentService.updateFinancePaymentStatusByBpm(id, pid, ErpAuditStatus.APPROVE.getStatus(), "ok");

        financePaymentService.voidFinancePayment(id, "作废");
        // 重复作废必须幂等
        financePaymentService.voidFinancePayment(id, "重复作废");

        assertEquals(ErpAuditStatus.VOID.getStatus(), erpFinancePaymentMapper.selectById(id).getStatus());
        assertEquals(0, erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size());
        ErpApStatementDO statement = erpApStatementMapper.selectById(statementId);
        assertEquals(0, BigDecimal.ZERO.compareTo(statement.getPaidAmount()));
        assertEquals(0, new BigDecimal("60.00").compareTo(statement.getRemainAmount()));
        assertEquals(ErpApStatementStatusEnum.UNPAID.getStatus(), statement.getStatus());
    }
}
