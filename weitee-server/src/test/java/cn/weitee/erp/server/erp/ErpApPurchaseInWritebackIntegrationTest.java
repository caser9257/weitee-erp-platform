package cn.weitee.erp.server.erp;

import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.purchase.ErpPaymentStatusEnum;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * P2 预备：采购入库 AP 端到端回写集成测试（真实 MySQL + Redis/Redisson）。
 *
 * 默认不执行；手动触发：
 *   mvn -pl weitee-server test -Dtest=ErpApPurchaseInWritebackIntegrationTest -Dp2.ap.it=true
 *
 * 验证完成标准第 1 条的真实库证据链：
 * 付款审批 → AP 主表(paid/remain/status) + 分配明细 + 入库单 paymentPrice + 采购订单 paymentPrice/paymentStatus
 * 四处一致；作废后全部复原。
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
class ErpApPurchaseInWritebackIntegrationTest {

    private static final Long TEST_SUPPLIER_ID = 999_999_903L;
    private static final BigDecimal ORDER_TOTAL_PRICE = new BigDecimal("100.00");
    private static final BigDecimal PAYMENT_PRICE = new BigDecimal("60.00");

    @Resource
    private ErpFinancePaymentService financePaymentService;
    @Resource
    private ErpApStatementMapper erpApStatementMapper;
    @Resource
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Resource
    private ErpFinancePaymentItemMapper erpFinancePaymentItemMapper;
    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    private Long statementId;
    private Long paymentId;
    private Long purchaseInId;
    private Long orderId;

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
        if (purchaseInId != null) {
            jdbcTemplate.update("DELETE FROM erp_purchase_in WHERE id = ?", purchaseInId);
        }
        if (orderId != null) {
            jdbcTemplate.update("DELETE FROM erp_purchase_order WHERE id = ?", orderId);
        }
    }

    private void prepareChain() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ErpPurchaseOrderDO order = new ErpPurchaseOrderDO()
                .setNo("PO-IT-" + suffix)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setOrderTime(LocalDateTime.now())
                .setTotalCount(BigDecimal.ONE)
                .setTotalPrice(ORDER_TOTAL_PRICE)
                .setTotalProductPrice(ORDER_TOTAL_PRICE)
                .setTotalTaxPrice(BigDecimal.ZERO)
                .setDiscountPercent(BigDecimal.ZERO)
                .setDiscountPrice(BigDecimal.ZERO)
                .setPaymentPrice(BigDecimal.ZERO)
                .setPaymentStatus(ErpPaymentStatusEnum.NONE.getStatus())
                .setRemark("P2 端到端集成测试数据");
        erpPurchaseOrderMapper.insert(order);
        orderId = order.getId();

        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setNo("PI-IT-" + suffix)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAccountId(1L)
                .setInTime(LocalDateTime.now())
                .setOrderId(orderId)
                .setOrderNo(order.getNo())
                .setTotalCount(BigDecimal.ONE)
                .setTotalPrice(ORDER_TOTAL_PRICE)
                .setTotalProductPrice(ORDER_TOTAL_PRICE)
                .setTotalTaxPrice(BigDecimal.ZERO)
                .setDiscountPercent(BigDecimal.ZERO)
                .setDiscountPrice(BigDecimal.ZERO)
                .setPaymentPrice(BigDecimal.ZERO)
                .setRemark("P2 端到端集成测试数据");
        erpPurchaseInMapper.insert(purchaseIn);
        purchaseInId = purchaseIn.getId();

        ErpApStatementDO statement = new ErpApStatementDO()
                .setStatementNo("AP-IT-" + suffix)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(purchaseInId)
                .setBizNo(purchaseIn.getNo())
                .setSourceOrderId(orderId)
                .setSourceOrderNo(order.getNo())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAccountId(1L)
                .setAmount(ORDER_TOTAL_PRICE)
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(ORDER_TOTAL_PRICE)
                .setCurrencyCode("CNY")
                .setBizDate(LocalDateTime.now())
                .setDueDate(LocalDateTime.now())
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setRemark("P2 端到端集成测试数据");
        erpApStatementMapper.insert(statement);
        statementId = statement.getId();

        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setNo("FP-IT-" + suffix)
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-IT-" + suffix)
                .setPaymentTime(LocalDateTime.now())
                .setSupplierId(TEST_SUPPLIER_ID)
                .setAccountId(1L)
                .setTotalPrice(PAYMENT_PRICE)
                .setDiscountPrice(BigDecimal.ZERO)
                .setPaymentPrice(PAYMENT_PRICE)
                .setRemark("P2 端到端集成测试数据");
        erpFinancePaymentMapper.insert(payment);
        paymentId = payment.getId();
        ErpFinancePaymentItemDO item = new ErpFinancePaymentItemDO()
                .setPaymentId(paymentId)
                .setApStatementId(statementId)
                .setBizType(statement.getBizType())
                .setBizId(purchaseInId)
                .setBizNo(purchaseIn.getNo())
                .setTotalPrice(ORDER_TOTAL_PRICE)
                .setPaidPrice(BigDecimal.ZERO)
                .setPaymentPrice(PAYMENT_PRICE);
        erpFinancePaymentItemMapper.insert(item);
    }

    @Test
    void approveThenVoid_shouldKeepApStatementAllocatePurchaseInAndOrderConsistent() {
        prepareChain();
        String pid = erpFinancePaymentMapper.selectById(paymentId).getProcessInstanceId();

        financePaymentService.updateFinancePaymentStatusByBpm(paymentId, pid,
                ErpAuditStatus.APPROVE.getStatus(), "端到端验证");

        // 1. AP 主表：paid/remain/status 与分配明细一致
        ErpApStatementDO statement = erpApStatementMapper.selectById(statementId);
        assertNotNull(statement);
        assertEquals(0, PAYMENT_PRICE.compareTo(statement.getPaidAmount()));
        assertEquals(0, new BigDecimal("40.00").compareTo(statement.getRemainAmount()));
        assertEquals(ErpApStatementStatusEnum.PARTIAL_PAID.getStatus(), statement.getStatus());
        // 2. 分配明细：一条生效核销 60
        assertEquals(1, erpFinancePaymentAllocateCount());
        // 3. 入库单回写
        assertEquals(0, PAYMENT_PRICE.compareTo(erpPurchaseInMapper.selectById(purchaseInId).getPaymentPrice()));
        // 4. 采购订单回写（汇总入库单已付 60 < 订单总价 100 → 部分付款）
        ErpPurchaseOrderDO order = erpPurchaseOrderMapper.selectById(orderId);
        assertEquals(0, PAYMENT_PRICE.compareTo(order.getPaymentPrice()));
        assertEquals(ErpPaymentStatusEnum.PARTIAL.getStatus(), order.getPaymentStatus());

        financePaymentService.voidFinancePayment(paymentId, "端到端作废验证");

        // 作废后四处全部复原
        statement = erpApStatementMapper.selectById(statementId);
        assertEquals(0, BigDecimal.ZERO.compareTo(statement.getPaidAmount()));
        assertEquals(0, ORDER_TOTAL_PRICE.compareTo(statement.getRemainAmount()));
        assertEquals(ErpApStatementStatusEnum.UNPAID.getStatus(), statement.getStatus());
        assertEquals(0, erpFinancePaymentAllocateCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(erpPurchaseInMapper.selectById(purchaseInId).getPaymentPrice()));
        order = erpPurchaseOrderMapper.selectById(orderId);
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getPaymentPrice()));
        assertEquals(ErpPaymentStatusEnum.NONE.getStatus(), order.getPaymentStatus());
    }

    private int erpFinancePaymentAllocateCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM erp_finance_payment_allocate WHERE payment_id = ? AND status = 20 AND deleted = 0",
                Integer.class, paymentId);
        return count == null ? 0 : count;
    }
}
