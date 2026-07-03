package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErrorCodeConstants;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinancePaymentServiceImplTest {

    @Test
    void updateFinancePaymentStatus_shouldCreateAllocateFactsAndRefreshStatements() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(3L)
                .setNo("FP-001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setSupplierId(201L);
        ErpFinancePaymentItemDO paymentItem = new ErpFinancePaymentItemDO()
                .setId(31L)
                .setPaymentId(3L)
                .setApStatementId(10L)
                .setBizId(10L)
                .setBizNo("PI-001")
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setPaymentPrice(new BigDecimal("88.50"));
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(10L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(10L)
                .setBizNo("PI-001")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("120.00"))
                .setRemainAmount(new BigDecimal("120.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        List<ErpFinancePaymentAllocateDO> insertedAllocates = new ArrayList<>();
        List<ErpApStatementItemDO> insertedStatementItems = new ArrayList<>();
        AtomicReference<List<Long>> refreshedStatementIdsRef = new AtomicReference<>();
        AtomicReference<List<Long>> refreshedBizStatementIdsRef = new AtomicReference<>();

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(paymentItem);
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("validateApStatement".equals(methodName)) {
                return statement;
            }
            if ("refreshStatementAmountByIds".equals(methodName)) {
                refreshedStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("refreshBizSummaryByStatementIds".equals(methodName)) {
                refreshedBizStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("getApStatementListByIds".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedAllocates.add((ErpFinancePaymentAllocateDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItems.add((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpPurchaseInDO().setId((Long) args[0]).setOrderId(110L);
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));
        setField(service, "redissonClient", createRedissonClientProxy());

        service.updateFinancePaymentStatus(3L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(1, insertedAllocates.size());
        assertEquals(10L, insertedAllocates.get(0).getApStatementId());
        assertEquals(new BigDecimal("88.50"), insertedAllocates.get(0).getAllocateAmount());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus(), insertedAllocates.get(0).getStatus());
        assertEquals(1, insertedStatementItems.size());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATED.getStatus(), insertedStatementItems.get(0).getItemType());
        assertEquals(List.of(10L), refreshedStatementIdsRef.get());
        assertEquals(List.of(10L), refreshedBizStatementIdsRef.get());
    }

    @Test
    void updateFinancePaymentStatus_shouldCreateNegativeAllocateForReturnStatement() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(4L)
                .setNo("FP-002")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setSupplierId(202L);
        ErpFinancePaymentItemDO paymentItem = new ErpFinancePaymentItemDO()
                .setId(41L)
                .setPaymentId(4L)
                .setApStatementId(12L)
                .setBizId(12L)
                .setBizNo("PR-001")
                .setBizType(ErpBizTypeEnum.PURCHASE_RETURN.getType())
                .setPaymentPrice(new BigDecimal("35.20"));
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(12L)
                .setBizType(ErpBizTypeEnum.PURCHASE_RETURN.getType())
                .setBizId(12L)
                .setBizNo("PR-001")
                .setSupplierId(202L)
                .setAmount(new BigDecimal("-88.00"))
                .setRemainAmount(new BigDecimal("-88.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        List<ErpFinancePaymentAllocateDO> insertedAllocates = new ArrayList<>();
        List<ErpApStatementItemDO> insertedStatementItems = new ArrayList<>();

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(paymentItem);
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("validateApStatement".equals(methodName)) {
                return statement;
            }
            if ("getApStatementListByIds".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedAllocates.add((ErpFinancePaymentAllocateDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItems.add((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpPurchaseInDO().setId((Long) args[0]).setOrderId(120L);
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));
        setField(service, "redissonClient", createRedissonClientProxy());

        service.updateFinancePaymentStatus(4L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(1, insertedAllocates.size());
        assertEquals(12L, insertedAllocates.get(0).getApStatementId());
        assertEquals(new BigDecimal("-35.20"), insertedAllocates.get(0).getAllocateAmount());
        assertEquals(1, insertedStatementItems.size());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATED.getStatus(), insertedStatementItems.get(0).getItemType());
    }

    @Test
    void updateFinancePaymentStatus_shouldCancelApprovedAllocateFactsWhenRollback() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.APPROVE.getStatus());
        ErpFinancePaymentItemDO paymentItem = new ErpFinancePaymentItemDO()
                .setId(51L)
                .setPaymentId(5L)
                .setApStatementId(15L)
                .setBizId(15L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setPaymentPrice(new BigDecimal("66.00"));
        ErpFinancePaymentAllocateDO approvedAllocate = new ErpFinancePaymentAllocateDO()
                .setId(501L)
                .setPaymentId(5L)
                .setApStatementId(15L)
                .setAllocateAmount(new BigDecimal("66.00"))
                .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus());
        List<ErpFinancePaymentAllocateDO> updatedAllocates = new ArrayList<>();
        List<ErpApStatementItemDO> insertedStatementItems = new ArrayList<>();
        AtomicReference<List<Long>> refreshedStatementIdsRef = new AtomicReference<>();
        AtomicReference<List<Long>> refreshedBizStatementIdsRef = new AtomicReference<>();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(15L)
                .setPaidAmount(new BigDecimal("66.00"))
                .setRemainAmount(new BigDecimal("22.00"));

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(paymentItem);
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(approvedAllocate);
            }
            if ("updateById".equals(methodName)) {
                updatedAllocates.add((ErpFinancePaymentAllocateDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("refreshStatementAmountByIds".equals(methodName)) {
                refreshedStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("refreshBizSummaryByStatementIds".equals(methodName)) {
                refreshedBizStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("getApStatementListByIds".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItems.add((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpPurchaseInDO().setId((Long) args[0]).setOrderId(130L);
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));

        service.updateFinancePaymentStatus(5L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(1, updatedAllocates.size());
        assertEquals(501L, updatedAllocates.get(0).getId());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.CANCELED.getStatus(), updatedAllocates.get(0).getStatus());
        assertEquals(1, insertedStatementItems.size());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus(), insertedStatementItems.get(0).getItemType());
        assertEquals(new BigDecimal("-66.00"), insertedStatementItems.get(0).getAmount());
        assertEquals(List.of(15L), refreshedStatementIdsRef.get());
        assertEquals(List.of(15L), refreshedBizStatementIdsRef.get());
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldRejectMismatchedProcessInstanceId() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-BOUND")
                .setSupplierId(201L);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> null));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> null));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> null));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> null));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));
        setField(service, "redissonClient", createRedissonClientProxy());

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateFinancePaymentStatusByBpm(5L, "PI-OTHER", ErpAuditStatus.APPROVE.getStatus(), "approved"));

        assertEquals(FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldIgnoreLateCallbackWhenOrderAlreadyHandled() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setProcessInstanceId("PI-BOUND")
                .setSupplierId(201L);
        AtomicReference<Boolean> updateCalledRef = new AtomicReference<>(false);
        AtomicReference<Boolean> itemQueryCalledRef = new AtomicReference<>(false);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updateCalledRef.set(true);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                itemQueryCalledRef.set(true);
                return List.of();
            }
            return null;
        }));

        service.updateFinancePaymentStatusByBpm(5L, "PI-BOUND", ErpAuditStatus.REJECT.getStatus(), "approved");

        assertEquals(Boolean.FALSE, updateCalledRef.get());
        assertEquals(Boolean.FALSE, itemQueryCalledRef.get());
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldResetStatusToDraftAndClearBinding() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-ROLLBACK")
                .setSupplierId(201L);
        AtomicReference<ErpFinancePaymentDO> updatedPaymentRef = new AtomicReference<>();
        AtomicReference<Boolean> itemQueryCalledRef = new AtomicReference<>(false);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updatedPaymentRef.set((ErpFinancePaymentDO) args[2]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                itemQueryCalledRef.set(true);
            }
            return List.of();
        }));

        service.rollbackFinancePaymentStatusToDraftByBpm(5L, "PI-ROLLBACK", "cancel");

        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedPaymentRef.get().getStatus());
        assertEquals(null, updatedPaymentRef.get().getProcessInstanceId());
        assertEquals(Boolean.FALSE, itemQueryCalledRef.get());
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldRejectMismatchedProcessInstanceId() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-BOUND")
                .setSupplierId(201L);
        AtomicReference<Boolean> itemQueryCalledRef = new AtomicReference<>(false);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                itemQueryCalledRef.set(true);
            }
            return List.of();
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.rollbackFinancePaymentStatusToDraftByBpm(5L, "PI-OTHER", "cancel"));

        assertEquals(FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
        assertEquals(Boolean.FALSE, itemQueryCalledRef.get());
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldIgnoreLateCallbackWhenOrderAlreadyHandled() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(5L)
                .setNo("FP-003")
                .setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId("PI-LATE")
                .setSupplierId(201L);
        AtomicReference<Boolean> updateCalledRef = new AtomicReference<>(false);
        AtomicReference<Boolean> itemQueryCalledRef = new AtomicReference<>(false);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updateCalledRef.set(true);
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                itemQueryCalledRef.set(true);
            }
            return List.of();
        }));

        service.rollbackFinancePaymentStatusToDraftByBpm(5L, "PI-LATE", "cancel");

        assertEquals(Boolean.FALSE, updateCalledRef.get());
        assertEquals(Boolean.FALSE, itemQueryCalledRef.get());
    }

    @Test
    void voidFinancePayment_shouldVoidApprovedPaymentAndReleaseAllocate() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(6L)
                .setNo("FP-004")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setSupplierId(203L);
        ErpFinancePaymentItemDO paymentItem = new ErpFinancePaymentItemDO()
                .setId(61L)
                .setPaymentId(6L)
                .setApStatementId(20L)
                .setBizId(20L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setPaymentPrice(new BigDecimal("100.00"));
        ErpFinancePaymentAllocateDO approvedAllocate = new ErpFinancePaymentAllocateDO()
                .setId(601L)
                .setPaymentId(6L)
                .setApStatementId(20L)
                .setAllocateAmount(new BigDecimal("100.00"))
                .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus());
        List<ErpFinancePaymentAllocateDO> updatedAllocates = new ArrayList<>();
        List<ErpApStatementItemDO> insertedStatementItems = new ArrayList<>();
        AtomicReference<List<Long>> refreshedStatementIdsRef = new AtomicReference<>();
        AtomicReference<List<Long>> refreshedBizStatementIdsRef = new AtomicReference<>();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(20L)
                .setPaidAmount(new BigDecimal("100.00"))
                .setRemainAmount(new BigDecimal("50.00"));

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentItemMapper", createProxy(ErpFinancePaymentItemMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(paymentItem);
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectListByPaymentId".equals(methodName)) {
                return List.of(approvedAllocate);
            }
            if ("updateById".equals(methodName)) {
                updatedAllocates.add((ErpFinancePaymentAllocateDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("refreshStatementAmountByIds".equals(methodName)) {
                refreshedStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("refreshBizSummaryByStatementIds".equals(methodName)) {
                refreshedBizStatementIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
            }
            if ("getApStatementListByIds".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItems.add((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpPurchaseInDO().setId((Long) args[0]).setOrderId(140L);
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));

        service.voidFinancePayment(6L, "业务取消，需要作废");

        assertEquals(1, updatedAllocates.size());
        assertEquals(601L, updatedAllocates.get(0).getId());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.CANCELED.getStatus(), updatedAllocates.get(0).getStatus());
        assertEquals(1, insertedStatementItems.size());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus(), insertedStatementItems.get(0).getItemType());
        assertEquals(new BigDecimal("-100.00"), insertedStatementItems.get(0).getAmount());
        assertEquals(List.of(20L), refreshedStatementIdsRef.get());
        assertEquals(List.of(20L), refreshedBizStatementIdsRef.get());
    }

    @Test
    void voidFinancePayment_shouldRejectWhenPaymentNotApproved() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(7L)
                .setNo("FP-005")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setSupplierId(204L);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.voidFinancePayment(7L, "测试作废"));

        assertEquals(1_030_601_010, ex.getCode());
    }

    @Test
    void voidFinancePayment_shouldBeIdempotentWhenAlreadyVoided() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpFinancePaymentDO payment = new ErpFinancePaymentDO()
                .setId(8L)
                .setNo("FP-006")
                .setStatus(ErpAuditStatus.VOID.getStatus())
                .setSupplierId(205L);

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return payment;
            }
            return null;
        }));

        // 应该直接返回，不抛出异常
        service.voidFinancePayment(8L, "重复作废测试");
    }

    @Test
    void createFinancePayment_shouldRejectWhenAllocateAmountExceedStatementRemain() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(20L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(100L)
                .setBizNo("PI-100")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("100.00"))
                .setRemainAmount(new BigDecimal("20.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("validateApStatement".equals(methodName)) {
                return statement;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.createFinancePayment(buildSaveReqVO(20L, ErpBizTypeEnum.PURCHASE_IN.getType(), 100L,
                        new BigDecimal("30.00"))));

        assertEquals(AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED.getCode(), ex.getCode());
    }

    @Test
    void createFinancePayment_shouldRejectWhenPaymentPriceIsNotPositive() throws Exception {
        ErpFinancePaymentServiceImpl service = new ErpFinancePaymentServiceImpl();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(21L)
                .setBizType(ErpBizTypeEnum.PURCHASE_RETURN.getType())
                .setBizId(101L)
                .setBizNo("PR-101")
                .setStatementNo("AP-12-PR-101")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("-100.00"))
                .setRemainAmount(new BigDecimal("-100.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("validateApStatement".equals(methodName)) {
                return statement;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.createFinancePayment(buildSaveReqVO(21L, ErpBizTypeEnum.PURCHASE_RETURN.getType(), 101L,
                        new BigDecimal("-1.00"))));

        assertEquals(AP_STATEMENT_ALLOCATE_AMOUNT_INVALID.getCode(), ex.getCode());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private RedissonClient createRedissonClientProxy() {
        return createProxy(RedissonClient.class, (methodName, args) -> {
            if ("getLock".equals(methodName)) {
                return createProxy(RLock.class, (lockMethodName, lockArgs) -> switch (lockMethodName) {
                    case "tryLock" -> true;
                    case "isHeldByCurrentThread" -> true;
                    case "unlock" -> null;
                    default -> null;
                });
            }
            return null;
        });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private ErpFinancePaymentSaveReqVO buildSaveReqVO(Long apStatementId, Integer bizType, Long bizId,
                                                      BigDecimal paymentPrice) {
        ErpFinancePaymentSaveReqVO reqVO = new ErpFinancePaymentSaveReqVO();
        reqVO.setPaymentTime(LocalDateTime.of(2026, 4, 23, 12, 0));
        reqVO.setSupplierId(201L);
        reqVO.setAccountId(301L);
        reqVO.setDiscountPrice(BigDecimal.ZERO);
        ErpFinancePaymentSaveReqVO.Item item = new ErpFinancePaymentSaveReqVO.Item();
        item.setApStatementId(apStatementId);
        item.setBizType(bizType);
        item.setBizId(bizId);
        item.setPaidPrice(BigDecimal.ZERO);
        item.setPaymentPrice(paymentPrice);
        reqVO.setItems(List.of(item));
        return reqVO;
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
