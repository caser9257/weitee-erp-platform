package cn.weitee.erp.server.erp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePrepaymentService;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P2 预备：AP 核销并发一致性集成测试（真实 MySQL + Redis/Redisson）。
 *
 * 默认不执行；手动触发：
 *   mvn -pl weitee-server test -Dtest=ErpApStatementAllocateConcurrencyIntegrationTest -Dp2.ap.it=true
 * 前置：本地 profile 数据库与 Redis 可用，且已执行 P2 相关迁移。
 *
 * 断言目标（完成标准）：
 * 1. 核销不得超过可核销余额（并发下 remain 不为负、生效核销合计不超过台账金额）。
 * 2. 付款与预付款共用台账锁后，跨子系统并发互斥真实生效。
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
class ErpApStatementAllocateConcurrencyIntegrationTest {

    private static final Long TEST_SUPPLIER_ID = 999_999_901L;

    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpFinancePrepaymentService financePrepaymentService;
    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Resource
    private ErpFinancePaymentItemMapper erpFinancePaymentItemMapper;
    @Resource
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Resource
    private ErpFinancePrepaymentMapper erpFinancePrepaymentMapper;
    @Resource
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    private Long statementId;
    private final java.util.List<Long> paymentIds = new java.util.ArrayList<>();
    private final java.util.List<Long> prepaymentIds = new java.util.ArrayList<>();

    @BeforeEach
    void mockLoginUser() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setUserType(1);
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

    private static void bindLoginUserToCurrentThread() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setUserType(1);
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

    @AfterEach
    void cleanUpTestData() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        if (statementId != null) {
            jdbcTemplate.update("DELETE FROM erp_ap_statement WHERE id = ?", statementId);
            jdbcTemplate.update("DELETE FROM erp_ap_statement_item WHERE statement_id = ?", statementId);
        }
        if (!paymentIds.isEmpty()) {
            jdbcTemplate.update("DELETE FROM erp_finance_payment_allocate WHERE payment_id IN ("
                    + placeholders(paymentIds.size()) + ")", paymentIds.toArray());
            jdbcTemplate.update("DELETE FROM erp_finance_payment_item WHERE payment_id IN ("
                    + placeholders(paymentIds.size()) + ")", paymentIds.toArray());
            jdbcTemplate.update("DELETE FROM erp_finance_payment WHERE id IN ("
                    + placeholders(paymentIds.size()) + ")", paymentIds.toArray());
        }
        if (!prepaymentIds.isEmpty()) {
            jdbcTemplate.update("DELETE FROM erp_finance_prepayment_allocate WHERE prepayment_id IN ("
                    + placeholders(prepaymentIds.size()) + ")", prepaymentIds.toArray());
            jdbcTemplate.update("DELETE FROM erp_finance_prepayment WHERE id IN ("
                    + placeholders(prepaymentIds.size()) + ")", prepaymentIds.toArray());
        }
    }

    private static String placeholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }

    private Long insertStatement(String amount) {
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
                .setRemark("P2 并发集成测试数据");
        erpApStatementMapper.insert(statement);
        return statement.getId();
    }

    private Long insertApprovedPayment(Long apStatementId, String amount) {
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
                .setRemark("P2 并发集成测试数据");
        erpFinancePaymentMapper.insert(payment);
        paymentIds.add(payment.getId());
        ErpApStatementDO statement = erpApStatementMapper.selectById(apStatementId);
        ErpFinancePaymentItemDO item = new ErpFinancePaymentItemDO()
                .setPaymentId(payment.getId())
                .setApStatementId(apStatementId)
                .setBizType(statement.getBizType())
                .setBizId(statement.getBizId())
                .setBizNo(statement.getBizNo())
                .setTotalPrice(statement.getAmount())
                .setPaidPrice(statement.getPaidAmount())
                .setPaymentPrice(new BigDecimal(amount));
        erpFinancePaymentItemMapper.insert(item);
        return payment.getId();
    }

    private Long insertApprovedPrepayment(String amount) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ErpFinancePrepaymentDO prepayment = new ErpFinancePrepaymentDO()
                .setNo("YF-IT-" + suffix)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setPrepaymentTime(LocalDateTime.now())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAccountId(1L)
                .setPrepaymentPrice(new BigDecimal(amount))
                .setAllocatedPrice(BigDecimal.ZERO)
                .setRemainPrice(new BigDecimal(amount))
                .setRemark("P2 并发集成测试数据");
        erpFinancePrepaymentMapper.insert(prepayment);
        prepaymentIds.add(prepayment.getId());
        return prepayment.getId();
    }

    private <T> java.util.concurrent.atomic.AtomicInteger runConcurrently(Callable<T> taskA, Callable<T> taskB) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger();
        try {
            Callable<T> gatedA = () -> { bindLoginUserToCurrentThread(); startGate.await(); return taskA.call(); };
            Callable<T> gatedB = () -> { bindLoginUserToCurrentThread(); startGate.await(); return taskB.call(); };
            Future<T> futureA = executor.submit(gatedA);
            Future<T> futureB = executor.submit(gatedB);
            startGate.countDown();
            for (Future<T> future : List.of(futureA, futureB)) {
                try {
                    future.get();
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    if (!(ex.getCause() instanceof ServiceException)) {
                        throw ex;
                    }
                }
            }
        } finally {
            executor.shutdownNow();
        }
        return successCount;
    }

    @Test
    void concurrentPaymentApprovals_shouldNotExceedStatementRemain() throws Exception {
        statementId = insertStatement("100.00");
        Long paymentA = insertApprovedPayment(statementId, "80.00");
        Long paymentB = insertApprovedPayment(statementId, "80.00");
        String pidA = erpFinancePaymentMapper.selectById(paymentA).getProcessInstanceId();
        String pidB = erpFinancePaymentMapper.selectById(paymentB).getProcessInstanceId();

        java.util.concurrent.atomic.AtomicInteger successes = runConcurrently(
                () -> {
                    financePaymentService.updateFinancePaymentStatusByBpm(paymentA, pidA,
                            ErpAuditStatus.APPROVE.getStatus(), "it");
                    return paymentA;
                },
                () -> {
                    financePaymentService.updateFinancePaymentStatusByBpm(paymentB, pidB,
                            ErpAuditStatus.APPROVE.getStatus(), "it");
                    return paymentB;
                });

        // 80+80 > 100：台账锁串行后必须恰好一个成功，另一个被超额校验拒绝
        assertEquals(1, successes.get(), "并发审批只允许一个核销成功");
        ErpApStatementDO refreshed = erpApStatementMapper.selectById(statementId);
        assertNotNull(refreshed);
        assertTrue(refreshed.getRemainAmount().compareTo(BigDecimal.ZERO) >= 0, "台账余额不得为负");
        assertEquals(0, new BigDecimal("20.00").compareTo(refreshed.getRemainAmount()));
        assertEquals(1, erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size());
    }

    @Test
    void concurrentPaymentApprovalAndPrepaymentAllocate_shouldShareStatementLock() throws Exception {
        statementId = insertStatement("100.00");
        Long payment = insertApprovedPayment(statementId, "80.00");
        String pid = erpFinancePaymentMapper.selectById(payment).getProcessInstanceId();
        Long prepayment = insertApprovedPrepayment("80.00");

        java.util.concurrent.atomic.AtomicInteger successes = runConcurrently(
                () -> {
                    financePaymentService.updateFinancePaymentStatusByBpm(payment, pid,
                            ErpAuditStatus.APPROVE.getStatus(), "it");
                    return payment;
                },
                () -> {
                    financePrepaymentService.allocateFinancePrepayment(
                            new ErpFinancePrepaymentAllocateReqVO().setPrepaymentId(prepayment)
                                    .setItems(List.of(new ErpFinancePrepaymentAllocateReqVO.Item()
                                            .setApStatementId(statementId)
                                            .setAllocateAmount(new BigDecimal("80.00")))));
                    return prepayment;
                });

        // 跨子系统（付款审批 × 预付款核销）共用台账锁：合计 160 > 100，必须恰好一个成功
        assertEquals(1, successes.get(), "付款与预付款并发核销必须互斥且总量受台账余额约束");
        ErpApStatementDO refreshed = erpApStatementMapper.selectById(statementId);
        assertTrue(refreshed.getRemainAmount().compareTo(BigDecimal.ZERO) >= 0, "台账余额不得为负");
        int approvedAllocates = erpFinancePaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size()
                + erpFinancePrepaymentAllocateMapper.selectApprovedListByStatementIds(List.of(statementId)).size();
        assertEquals(1, approvedAllocates);
    }
}
