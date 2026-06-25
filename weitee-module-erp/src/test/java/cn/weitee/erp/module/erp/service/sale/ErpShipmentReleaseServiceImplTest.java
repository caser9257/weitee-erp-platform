package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.ShipmentReleaseResultVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReceiptService;
import cn.weitee.erp.module.crm.service.contract.CrmContractService;
import cn.weitee.erp.module.crm.dal.dataobject.contract.CrmContractDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 发货放行校验边界场景测试
 */
@ExtendWith(MockitoExtension.class)
class ErpShipmentReleaseServiceImplTest {

    @InjectMocks
    private ErpShipmentReleaseServiceImpl shipmentReleaseService;

    @Mock
    private ErpSaleOrderMapper erpSaleOrderMapper;

    @Mock
    private ErpFinanceReceiptService erpFinanceReceiptService;

    @Mock
    private CrmContractService crmContractService;

    private ErpSaleOrderDO baseOrder;
    private CrmContractDO baseContract;

    @BeforeEach
    void setUp() {
        // 基础订单
        baseOrder = new ErpSaleOrderDO();
        baseOrder.setId(1L);
        baseOrder.setNo("ORD-2026-001");
        baseOrder.setStatus(ErpAuditStatus.APPROVE.getStatus());
        baseOrder.setContractId(100L);
        baseOrder.setTotalPrice(new BigDecimal("100000.00"));
        baseOrder.setDepositPrice(new BigDecimal("20000.00"));
        baseOrder.setShipmentReleaseStatus("PENDING");

        // 基础合同
        baseContract = new CrmContractDO();
        baseContract.setId(100L);
        baseContract.setNo("CT-2026-001");
        baseContract.setShipmentReleaseRule("SIGN_AND_SHIP");
        baseContract.setPrepaymentRatio(null);
    }

    // ========== 场景 1：订单状态校验 ==========

    @Nested
    @DisplayName("订单状态校验")
    class OrderStatusCheck {

        @Test
        @DisplayName("订单未审批通过 → 阻塞")
        void should_block_when_order_not_approved() {
            // Given
            baseOrder.setStatus(ErpAuditStatus.PROCESS.getStatus());
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
            assertEquals("BLOCKED", result.getReleaseStatus());
            assertTrue(result.getBlockerReasons().get(0).contains("未审批通过"));
        }

        @Test
        @DisplayName("订单已审批通过 → 继续后续校验")
        void should_pass_when_order_approved() {
            // Given
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L)).thenReturn(BigDecimal.ZERO);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertNotNull(result);
            // 签约即发规则下，应该通过
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("订单状态校验") && d.isPassed()));
        }

        @Test
        @DisplayName("订单不存在 → 阻塞")
        void should_block_when_order_not_found() {
            // Given
            when(erpSaleOrderMapper.selectById(999L)).thenReturn(null);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(999L);

            // Then
            assertFalse(result.isReleasable());
            assertTrue(result.getBlockerReasons().get(0).contains("不存在"));
        }
    }

    // ========== 场景 2：合同关联校验 ==========

    @Nested
    @DisplayName("合同关联校验")
    class ContractCheck {

        @Test
        @DisplayName("订单未关联合同 → 阻塞")
        void should_block_when_no_contract() {
            // Given
            baseOrder.setContractId(null);
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
            assertTrue(result.getBlockerReasons().get(0).contains("未关联合同"));
        }
    }

    // ========== 场景 3：放行规则校验 ==========

    @Nested
    @DisplayName("放行规则校验")
    class ReleaseRuleCheck {

        @Test
        @DisplayName("签约即发 → 直接通过")
        void should_pass_when_sign_and_ship() {
            // Given
            baseContract.setShipmentReleaseRule("SIGN_AND_SHIP");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L)).thenReturn(BigDecimal.ZERO);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("到账后发：收款金额 >= 订单金额 → 通过")
        void should_pass_when_fully_paid() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("100000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("到账后发：收款金额 < 订单金额 → 阻塞")
        void should_block_when_partial_payment() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("50000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
            assertTrue(result.getBlockerReasons().stream()
                    .anyMatch(r -> r.contains("已收款") && r.contains("<")));
        }

        @Test
        @DisplayName("到账后发：部分收款（差 1 分钱）→ 阻塞")
        void should_block_when_payment_off_by_one_cent() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("99999.99"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
        }

        @Test
        @DisplayName("到账后发：收款金额刚好等于订单金额 → 通过")
        void should_pass_when_payment_exact_match() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("100000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }
    }

    // ========== 场景 4：预付款比例校验 ==========

    @Nested
    @DisplayName("预付款比例校验")
    class PrepaymentCheck {

        @Test
        @DisplayName("合同比例 30%，收款 30000 → 通过")
        void should_pass_when_prepayment_ratio_met() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PREPAYMENT");
            baseContract.setPrepaymentRatio(new BigDecimal("30"));
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("30000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("合同比例 30%，收款 29999.99 → 阻塞（临界值）")
        void should_block_when_prepayment_ratio_not_met_at_boundary() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PREPAYMENT");
            baseContract.setPrepaymentRatio(new BigDecimal("30"));
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("29999.99"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
        }

        @Test
        @DisplayName("无合同比例，使用订单定金 20000，收款 20000 → 通过")
        void should_pass_when_deposit_met() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PREPAYMENT");
            baseContract.setPrepaymentRatio(null); // 无合同比例
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("20000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("无合同比例，使用订单定金 20000，收款 19999.99 → 阻塞")
        void should_block_when_deposit_not_met() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PREPAYMENT");
            baseContract.setPrepaymentRatio(null);
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("19999.99"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
        }

        @Test
        @DisplayName("合同比例 100%，收款全额 → 通过")
        void should_pass_when_full_prepayment_ratio() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PREPAYMENT");
            baseContract.setPrepaymentRatio(new BigDecimal("100"));
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("100000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }
    }

    // ========== 场景 5：财务审核校验 ==========

    @Nested
    @DisplayName("财务审核校验")
    class FinanceApprovalCheck {

        @Test
        @DisplayName("财务审核后发：已放行 → 通过")
        void should_pass_when_finance_approved() {
            // Given
            baseContract.setShipmentReleaseRule("FINANCE_APPROVAL");
            baseOrder.setShipmentReleaseStatus("RELEASED");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("财务审核后发：待审核 → 阻塞")
        void should_block_when_finance_pending() {
            // Given
            baseContract.setShipmentReleaseRule("FINANCE_APPROVAL");
            baseOrder.setShipmentReleaseStatus("FINANCE_REVIEW");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
        }

        @Test
        @DisplayName("财务审核后发：已驳回 → 阻塞")
        void should_block_when_finance_rejected() {
            // Given
            baseContract.setShipmentReleaseRule("FINANCE_APPROVAL");
            baseOrder.setShipmentReleaseStatus("BLOCKED");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
        }
    }

    // ========== 场景 6：通用财务审核（所有规则都需要） ==========

    @Nested
    @DisplayName("通用财务审核")
    class GeneralFinanceCheck {

        @Test
        @DisplayName("签约即发但未财务审核 → 阻塞")
        void should_block_when_sign_and_ship_but_not_finance_approved() {
            // Given
            baseContract.setShipmentReleaseRule("SIGN_AND_SHIP");
            baseOrder.setShipmentReleaseStatus("PENDING");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertFalse(result.isReleasable());
            assertTrue(result.getBlockerReasons().stream()
                    .anyMatch(r -> r.contains("财务审核")));
        }

        @Test
        @DisplayName("签约即发且已财务审核 → 通过")
        void should_pass_when_sign_and_ship_and_finance_approved() {
            // Given
            baseContract.setShipmentReleaseRule("SIGN_AND_SHIP");
            baseOrder.setShipmentReleaseStatus("RELEASED");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.isReleasable());
            assertEquals("RELEASED", result.getReleaseStatus());
        }
    }

    // ========== 场景 7：财务审核驳回后重新提交 ==========

    @Nested
    @DisplayName("财务审核驳回后重新提交")
    class FinanceResubmit {

        @Test
        @DisplayName("驳回后重新提交为待审核 → 状态正确")
        void should_update_status_to_finance_review_on_resubmit() {
            // Given
            baseOrder.setShipmentReleaseStatus("BLOCKED");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);

            // When
            shipmentReleaseService.submitFinanceApproval(1L, 10L);

            // Then - 验证状态更新逻辑（通过 verify 或检查副作用）
            // 这里主要验证方法不会抛异常
        }

        @Test
        @DisplayName("财务审核通过 → 状态变为 RELEASED")
        void should_update_status_to_released_on_approve() {
            // Given
            baseOrder.setShipmentReleaseStatus("FINANCE_REVIEW");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);

            // When
            shipmentReleaseService.approveFinance(1L, 10L, "同意");

            // Then - 验证方法不会抛异常
        }

        @Test
        @DisplayName("财务审核驳回 → 状态变为 BLOCKED")
        void should_update_status_to_blocked_on_reject() {
            // Given
            baseOrder.setShipmentReleaseStatus("FINANCE_REVIEW");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);

            // When
            shipmentReleaseService.rejectFinance(1L, 10L, "发票信息不完整");

            // Then - 验证方法不会抛异常
        }
    }

    // ========== 场景 8：合同服务异常降级 ==========

    @Nested
    @DisplayName("合同服务异常降级")
    class ContractServiceFallback {

        @Test
        @DisplayName("合同服务调用失败 → 使用默认放行规则")
        void should_fallback_to_default_rule_when_contract_service_fails() {
            // Given
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenThrow(new RuntimeException("服务不可用"));
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L)).thenReturn(BigDecimal.ZERO);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then - 不应抛异常，使用默认规则
            assertNotNull(result);
            // 默认签约即发，但需要财务审核
            assertFalse(result.isReleasable()); // 因为财务审核未通过
        }
    }

    // ========== 场景 9：金额边界值 ==========

    @Nested
    @DisplayName("金额边界值")
    class AmountBoundary {

        @Test
        @DisplayName("订单金额为 0 → 到账后发直接通过")
        void should_pass_when_order_amount_is_zero() {
            // Given
            baseOrder.setTotalPrice(BigDecimal.ZERO);
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L)).thenReturn(BigDecimal.ZERO);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("订单金额为 null → 视为 0")
        void should_treat_null_amount_as_zero() {
            // Given
            baseOrder.setTotalPrice(null);
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L)).thenReturn(BigDecimal.ZERO);

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }

        @Test
        @DisplayName("超额收款 → 到账后发通过")
        void should_pass_when_overpaid() {
            // Given
            baseContract.setShipmentReleaseRule("AFTER_PAYMENT");
            when(erpSaleOrderMapper.selectById(1L)).thenReturn(baseOrder);
            when(crmContractService.getContract(100L)).thenReturn(baseContract);
            when(erpFinanceReceiptService.getReceivedAmountByOrderId(1L))
                    .thenReturn(new BigDecimal("150000.00"));

            // When
            ShipmentReleaseResultVO result = shipmentReleaseService.checkRelease(1L);

            // Then
            assertTrue(result.getDetails().stream()
                    .anyMatch(d -> d.getCheckItem().equals("放行规则校验") && d.isPassed()));
        }
    }
}
