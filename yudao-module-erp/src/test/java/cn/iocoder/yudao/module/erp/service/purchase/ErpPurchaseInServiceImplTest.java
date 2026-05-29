package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInConfirmStockInReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInQualityCheckReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInStockExecuteMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockExecuteStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpApStatementService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceBizHookService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockRecordService;
import cn.iocoder.yudao.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BATCH_UPDATE_FIELD_NOT_SUPPORT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BATCH_UPDATE_FIELD_VALUE_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_UPDATE_FAIL_APPROVE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseInServiceImplTest {

    private static final int PURCHASE_IN_ITEM_COUNT_EXCEED_REMAINING = 1_030_102_018;
    private static final int PURCHASE_IN_ITEM_ORDER_MISMATCH = 1_030_102_019;

    private final AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final AtomicReference<ErpPurchaseInDO> lastUpdateObjRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInItemDO>> purchaseInItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInDO>> approvedPurchaseInsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInItemDO>> approvedPurchaseInItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInItemDO>> updatedItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInStockExecuteDO>> purchaseInStockExecutesRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInStockExecuteItemDO>> purchaseInStockExecuteItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<ErpPurchaseInStockExecuteDO> insertedStockExecuteRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInStockExecuteItemDO>> insertedStockExecuteItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<Map<Long, BigDecimal>> orderInCountMapRef = new AtomicReference<>();
    private final AtomicReference<Boolean> rawCountQueryCalledRef = new AtomicReference<>(false);
    private final AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseOrderItemDO>> purchaseOrderItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpProductDO>> productListRef = new AtomicReference<>(List.of());
    private final AtomicReference<String> generatedNoRef = new AtomicReference<>("CGRK20260409000001");
    private final AtomicReference<Long> validatedAccountIdRef = new AtomicReference<>();
    private final AtomicReference<ErpPurchaseInDO> insertedPurchaseInRef = new AtomicReference<>();
    private final AtomicReference<List<ErpPurchaseInItemDO>> insertedPurchaseInItemsRef = new AtomicReference<>(List.of());
    private final AtomicReference<Long> orderIdRef = new AtomicReference<>();
    private final AtomicReference<Long> createdQualityPurchaseInIdRef = new AtomicReference<>();
    private final AtomicReference<Long> voidedQualityPurchaseInIdRef = new AtomicReference<>();
    private final AtomicReference<ErpPurchaseInQualityCheckReqVO> delegatedQualityCheckReqRef = new AtomicReference<>();
    private final AtomicReference<ServiceException> delegatedQualityCheckExceptionRef = new AtomicReference<>();
    private final AtomicReference<Long> createdApStatementPurchaseInIdRef = new AtomicReference<>();
    private final AtomicReference<Integer> autoGenerateVoucherBizTypeRef = new AtomicReference<>();
    private final AtomicReference<Long> autoGenerateVoucherBizIdRef = new AtomicReference<>();
    private final AtomicReference<LocalDate> financeHookBizDateRef = new AtomicReference<>();
    private final AtomicReference<Integer> closedStatementBizTypeRef = new AtomicReference<>();
    private final AtomicReference<Long> closedStatementBizIdRef = new AtomicReference<>();
    private final AtomicReference<Long> approvedAllocateCountRef = new AtomicReference<>(0L);
    private final List<ErpStockRecordCreateReqBO> stockRecords = new ArrayList<>();

    private ErpPurchaseInServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpPurchaseInServiceImpl();
        purchaseInRef.set(null);
        updateCountRef.set(1);
        lastUpdateObjRef.set(null);
        purchaseInItemsRef.set(List.of());
        approvedPurchaseInsRef.set(List.of());
        approvedPurchaseInItemsRef.set(List.of());
        updatedItemsRef.set(List.of());
        purchaseInStockExecutesRef.set(List.of());
        purchaseInStockExecuteItemsRef.set(List.of());
        insertedStockExecuteRef.set(null);
        insertedStockExecuteItemsRef.set(List.of());
        orderInCountMapRef.set(null);
        rawCountQueryCalledRef.set(false);
        purchaseOrderRef.set(null);
        purchaseOrderItemsRef.set(List.of());
        productListRef.set(List.of());
        generatedNoRef.set("CGRK20260409000001");
        validatedAccountIdRef.set(null);
        insertedPurchaseInRef.set(null);
        insertedPurchaseInItemsRef.set(List.of());
        orderIdRef.set(null);
        createdQualityPurchaseInIdRef.set(null);
        voidedQualityPurchaseInIdRef.set(null);
        delegatedQualityCheckReqRef.set(null);
        delegatedQualityCheckExceptionRef.set(null);
        createdApStatementPurchaseInIdRef.set(null);
        autoGenerateVoucherBizTypeRef.set(null);
        autoGenerateVoucherBizIdRef.set(null);
        financeHookBizDateRef.set(null);
        closedStatementBizTypeRef.set(null);
        closedStatementBizIdRef.set(null);
        approvedAllocateCountRef.set(0L);
        stockRecords.clear();
        setField(service, "purchaseInMapper", createPurchaseInMapperProxy());
        setField(service, "purchaseInItemMapper", createPurchaseInItemMapperProxy());
        setField(service, "purchaseInStockExecuteMapper", createPurchaseInStockExecuteMapperProxy());
        setField(service, "purchaseInStockExecuteItemMapper", createPurchaseInStockExecuteItemMapperProxy());
        setField(service, "purchaseOrderService", createPurchaseOrderServiceProxy());
        setField(service, "productService", createProductServiceProxy());
        setField(service, "accountService", createAccountServiceProxy());
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return generatedNoRef.get();
            }
        });
        setField(service, "stockRecordService", createStockRecordServiceProxy());
        setField(service, "purchaseInQualityService", createPurchaseInQualityServiceProxy());
        setField(service, "purchaseSourceBatchService", createPurchaseSourceBatchServiceProxy());
        setField(service, "apStatementService", createApStatementServiceProxy());
        setField(service, "financeBizHookService", createFinanceBizHookServiceProxy());
        setField(service, "paymentAllocateMapper", createPaymentAllocateMapperProxy());
    }

    @Test
    void createPurchaseIn_shouldRejectWhenInboundCountExceedsRemaining() {
        purchaseOrderRef.set(purchaseOrder(9L, "CGDD-001", 201L, 301L));
        purchaseOrderItemsRef.set(List.of(purchaseOrderItem(101L, 9L, 1001L, "10", "7")));
        productListRef.set(List.of(product(1001L, 2001L)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createPurchaseIn(
                saveReq(null, 9L, saveItem(null, 101L, 9001L, 1001L, 2001L, "4"))));

        assertEquals(PURCHASE_IN_ITEM_COUNT_EXCEED_REMAINING, ex.getCode());
        assertEquals(List.of(), insertedPurchaseInItemsRef.get());
    }

    @Test
    void createPurchaseIn_shouldRejectWhenOrderItemDoesNotBelongToOrder() {
        purchaseOrderRef.set(purchaseOrder(9L, "CGDD-001", 201L, 301L));
        purchaseOrderItemsRef.set(List.of(purchaseOrderItem(101L, 9L, 1001L, "10", "7")));
        productListRef.set(List.of(product(1001L, 2001L)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createPurchaseIn(
                saveReq(null, 9L, saveItem(null, 999L, 9001L, 1001L, 2001L, "1"))));

        assertEquals(PURCHASE_IN_ITEM_ORDER_MISMATCH, ex.getCode());
    }

    @Test
    void createPurchaseIn_shouldCreateWhenInboundCountEqualsRemaining() {
        purchaseOrderRef.set(purchaseOrder(9L, "CGDD-001", 201L, 301L));
        purchaseOrderItemsRef.set(List.of(purchaseOrderItem(101L, 9L, 1001L, "10", "7")));
        productListRef.set(List.of(product(1001L, 2001L)));

        Long id = service.createPurchaseIn(saveReq(null, 9L,
                saveItem(null, 101L, 9001L, 1001L, 2001L, "3")));

        assertEquals(88L, id);
        assertEquals("CGRK20260409000001", insertedPurchaseInRef.get().getNo());
        assertEquals("CGDD-001", insertedPurchaseInRef.get().getOrderNo());
        assertEquals(201L, insertedPurchaseInRef.get().getSupplierId());
        assertEquals(1, insertedPurchaseInItemsRef.get().size());
        assertEquals(new BigDecimal("3"), insertedPurchaseInItemsRef.get().get(0).getCount());
    }

    @Test
    void updatePurchaseIn_shouldRejectWhenInboundCountExceedsRemaining() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.REJECT.getStatus(), null));
        purchaseOrderRef.set(purchaseOrder(9L, "CGDD-001", 201L, 301L));
        purchaseOrderItemsRef.set(List.of(purchaseOrderItem(101L, 9L, 1001L, "10", "7")));
        productListRef.set(List.of(product(1001L, 2001L)));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updatePurchaseIn(
                saveReq(1L, 9L, saveItem(11L, 101L, 9001L, 1001L, 2001L, "4"))));

        assertEquals(PURCHASE_IN_ITEM_COUNT_EXCEED_REMAINING, ex.getCode());
    }

    @Test
    void updatePurchaseInBatch_shouldUpdateRemarkForEditableOrders() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.REJECT.getStatus(), null));

        ErpPurchaseInBatchUpdateResultVO result = service.updatePurchaseInBatch(batchReq("remark", "补充备注"));

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(List.of(1L), result.getUpdatedIds());
        assertEquals("补充备注", lastUpdateObjRef.get().getRemark());
    }

    @Test
    void updatePurchaseInBatch_shouldUpdateAccountAndValidateAccount() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.REJECT.getStatus(), null));

        ErpPurchaseInBatchUpdateResultVO result = service.updatePurchaseInBatch(batchReq("accountId", "301"));

        assertEquals(1, result.getSuccessCount());
        assertEquals(301L, lastUpdateObjRef.get().getAccountId());
        assertEquals(301L, validatedAccountIdRef.get());
    }

    @Test
    void updatePurchaseInBatch_shouldRejectApprovedOrder() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.APPROVE.getStatus(), null));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updatePurchaseInBatch(batchReq("remark", "补充备注")));

        assertEquals(PURCHASE_IN_UPDATE_FAIL_APPROVE.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInBatch_shouldRejectProcessingOrder() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.PROCESS.getStatus(), null)
                .setProcessInstanceId("PI-001"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updatePurchaseInBatch(batchReq("remark", "补充备注")));

        assertEquals(PURCHASE_IN_UPDATE_FAIL_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInBatch_shouldRejectUnsupportedField() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.REJECT.getStatus(), null));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updatePurchaseInBatch(batchReq("status", "20")));

        assertEquals(PURCHASE_IN_BATCH_UPDATE_FIELD_NOT_SUPPORT.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInBatch_shouldRejectInvalidDatetime() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.REJECT.getStatus(), null));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updatePurchaseInBatch(batchReq("inTime", "2026-04-31 09:00:00")));

        assertEquals(PURCHASE_IN_BATCH_UPDATE_FIELD_VALUE_INVALID.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInStatus_shouldApproveWithoutWritingStockAndKeepInboundCountAtZeroBeforeQualityCheck() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.PROCESS.getStatus(), null)
                .setInTime(LocalDateTime.of(2026, 4, 29, 8, 30)));
        purchaseInItemsRef.set(List.of(purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")));
        approvedPurchaseInsRef.set(List.of(
                new ErpPurchaseInDO().setId(1L).setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())));
        approvedPurchaseInItemsRef.set(List.of(purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")));

        service.updatePurchaseInStatus(1L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(ErpQaStatusEnum.TO_INSPECT.getStatus(), lastUpdateObjRef.get().getQaStatus());
        assertTrue(stockRecords.isEmpty());
        assertEquals(9L, orderIdRef.get());
        assertEquals(BigDecimal.ZERO, orderInCountMapRef.get().getOrDefault(101L, BigDecimal.ZERO));
        assertEquals(Boolean.FALSE, rawCountQueryCalledRef.get());
        assertEquals(1L, createdQualityPurchaseInIdRef.get());
        assertEquals(1L, createdApStatementPurchaseInIdRef.get());
        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), autoGenerateVoucherBizTypeRef.get());
        assertEquals(1L, autoGenerateVoucherBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 29), financeHookBizDateRef.get());
    }

    @Test
    void updatePurchaseInStatusByBpm_shouldApproveWithoutWritingStockAndKeepInboundCountAtZeroBeforeQualityCheck() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.PROCESS.getStatus(), null)
                .setInTime(LocalDateTime.of(2026, 4, 30, 9, 15)));
        purchaseInItemsRef.set(List.of(purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")));
        approvedPurchaseInsRef.set(List.of(
                new ErpPurchaseInDO().setId(1L).setQaStatus(ErpQaStatusEnum.TO_INSPECT.getStatus())));
        approvedPurchaseInItemsRef.set(List.of(purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")));

        service.updatePurchaseInStatusByBpm(1L, "PI-APPROVE", ErpAuditStatus.APPROVE.getStatus(), null);

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals("PI-APPROVE", lastUpdateObjRef.get().getProcessInstanceId());
        assertEquals(ErpQaStatusEnum.TO_INSPECT.getStatus(), lastUpdateObjRef.get().getQaStatus());
        assertTrue(stockRecords.isEmpty());
        assertEquals(9L, orderIdRef.get());
        assertEquals(BigDecimal.ZERO, orderInCountMapRef.get().getOrDefault(101L, BigDecimal.ZERO));
        assertEquals(Boolean.FALSE, rawCountQueryCalledRef.get());
        assertEquals(1L, createdQualityPurchaseInIdRef.get());
        assertEquals(1L, createdApStatementPurchaseInIdRef.get());
        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), autoGenerateVoucherBizTypeRef.get());
        assertEquals(1L, autoGenerateVoucherBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 30), financeHookBizDateRef.get());
    }

    @Test
    void updatePurchaseInStatus_shouldRejectWhenApprovedAllocateExists() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.APPROVE.getStatus(), null)
                .setPaymentPrice(BigDecimal.ZERO));
        approvedAllocateCountRef.set(1L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updatePurchaseInStatus(1L, ErpAuditStatus.PROCESS.getStatus()));

        assertEquals(PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInStatus_shouldCloseApStatementWhenProcessWithoutApprovedAllocate() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.APPROVE.getStatus(), null)
                .setPaymentPrice(BigDecimal.ZERO));

        service.updatePurchaseInStatus(1L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), closedStatementBizTypeRef.get());
        assertEquals(1L, closedStatementBizIdRef.get());
    }

    @Test
    void qualityCheckPurchaseIn_shouldDelegateToQualityOrderService() {
        service.qualityCheckPurchaseIn(99L, qualityCheckReq(1L, "来料质检完成",
                qualityCheckItem(11L, "5", "1", "1件外观瑕疵"),
                qualityCheckItem(12L, "0", "4", "整批不合格")));

        assertEquals(1L, delegatedQualityCheckReqRef.get().getId());
        assertEquals("来料质检完成", delegatedQualityCheckReqRef.get().getRemark());
        assertEquals(2, delegatedQualityCheckReqRef.get().getItems().size());
    }

    @Test
    void confirmPurchaseInStockIn_shouldWritePassedQtyAndRefreshInboundCount() {
        purchaseInRef.set(purchaseIn(1L, 9L, "PI-001", ErpAuditStatus.APPROVE.getStatus(),
                ErpQaStatusEnum.PARTIAL.getStatus())
                .setQaPassCount(new BigDecimal("5"))
                .setStockInCount(BigDecimal.ZERO)
                .setStockInStatus(ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus()));
        purchaseInItemsRef.set(List.of(
                purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")
                        .setQaPassCount(new BigDecimal("5")).setQaRejectCount(BigDecimal.ONE).setStockInCount(BigDecimal.ZERO),
                purchaseInItem(12L, 1L, 102L, 1002L, 2002L, "4")
                        .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(new BigDecimal("4")).setStockInCount(BigDecimal.ZERO)));
        approvedPurchaseInsRef.set(List.of(
                new ErpPurchaseInDO().setId(1L)
                        .setQaStatus(ErpQaStatusEnum.PARTIAL.getStatus())
                        .setQaPassCount(new BigDecimal("5"))
                        .setStockInCount(new BigDecimal("5"))
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus()),
                new ErpPurchaseInDO().setId(2L)
                        .setQaStatus(ErpQaStatusEnum.PASSED.getStatus())
                        .setQaPassCount(new BigDecimal("2"))
                        .setStockInCount(new BigDecimal("2"))
                        .setStockInStatus(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus())));
        approvedPurchaseInItemsRef.set(List.of(
                purchaseInItem(11L, 1L, 101L, 1001L, 2001L, "6")
                        .setQaPassCount(new BigDecimal("5")).setQaRejectCount(BigDecimal.ONE).setStockInCount(new BigDecimal("5")),
                purchaseInItem(12L, 1L, 102L, 1002L, 2002L, "4")
                        .setQaPassCount(BigDecimal.ZERO).setQaRejectCount(new BigDecimal("4")).setStockInCount(BigDecimal.ZERO),
                purchaseInItem(21L, 2L, 101L, 1001L, 2001L, "2")
                        .setQaPassCount(new BigDecimal("2")).setQaRejectCount(BigDecimal.ZERO).setStockInCount(new BigDecimal("2"))));

        service.confirmPurchaseInStockIn(99L, confirmStockInReq(1L));

        assertEquals(ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus(), lastUpdateObjRef.get().getStockInStatus());
        assertEquals(new BigDecimal("5"), lastUpdateObjRef.get().getStockInCount());
        assertEquals(99L, lastUpdateObjRef.get().getStockInUserId());
        assertNotNull(lastUpdateObjRef.get().getStockInTime());
        assertNotNull(insertedStockExecuteRef.get());
        assertEquals(1, insertedStockExecuteItemsRef.get().size());
        assertEquals(1, stockRecords.size());
        assertEquals(1001L, stockRecords.get(0).getProductId());
        assertEquals(new BigDecimal("5"), stockRecords.get(0).getCount());
        assertEquals(9L, orderIdRef.get());
        assertEquals(new BigDecimal("7"), orderInCountMapRef.get().get(101L));
        assertEquals(BigDecimal.ZERO, orderInCountMapRef.get().getOrDefault(102L, BigDecimal.ZERO));
    }

    @Test
    void qualityCheckPurchaseIn_shouldRejectWhenInboundNotApproved() {
        delegatedQualityCheckExceptionRef.set(new ServiceException(PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.qualityCheckPurchaseIn(99L,
                qualityCheckReq(1L, "待质检", qualityCheckItem(11L, "6", "0", null))));

        assertEquals(PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseInStatusByBpm_shouldDeclareTransactionalBoundary() throws Exception {
        Method method = ErpPurchaseInServiceImpl.class.getMethod("updatePurchaseInStatusByBpm",
                Long.class, String.class, Integer.class, String.class);
        assertTrue(method.isAnnotationPresent(Transactional.class));
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

    private ErpPurchaseInMapper createPurchaseInMapperProxy() {
        return createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("selectByNo".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpPurchaseInDO purchaseIn = (ErpPurchaseInDO) args[0];
                if (purchaseIn.getId() == null) {
                    purchaseIn.setId(88L);
                }
                insertedPurchaseInRef.set(purchaseIn);
                return 1;
            }
            if ("updateById".equals(methodName) || "updateByIdAndStatus".equals(methodName)) {
                lastUpdateObjRef.set((ErpPurchaseInDO) ("updateById".equals(methodName) ? args[0] : args[2]));
                return updateCountRef.get();
            }
            if ("selectApprovedListByOrderId".equals(methodName)) {
                return approvedPurchaseInsRef.get();
            }
            return null;
        });
    }

    private ErpPurchaseInItemMapper createPurchaseInItemMapperProxy() {
        return createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByInId".equals(methodName)) {
                return purchaseInItemsRef.get();
            }
            if ("insertBatch".equals(methodName)) {
                insertedPurchaseInItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInItemDO>) args[0]));
                return true;
            }
            if ("selectListByInIds".equals(methodName)) {
                return approvedPurchaseInItemsRef.get();
            }
            if ("updateBatch".equals(methodName)) {
                updatedItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInItemDO>) args[0]));
                return true;
            }
            if ("selectOrderItemCountSumMapByInIds".equals(methodName)) {
                rawCountQueryCalledRef.set(true);
                return Map.of(101L, new BigDecimal("99"));
            }
            return null;
        });
    }

    private ErpPurchaseInStockExecuteMapper createPurchaseInStockExecuteMapperProxy() {
        return createProxy(ErpPurchaseInStockExecuteMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpPurchaseInStockExecuteDO executeDO = (ErpPurchaseInStockExecuteDO) args[0];
                if (executeDO.getId() == null) {
                    executeDO.setId(501L);
                }
                if (executeDO.getCreateTime() == null) {
                    executeDO.setCreateTime(LocalDateTime.of(2026, 4, 10, 10, 0));
                }
                insertedStockExecuteRef.set(executeDO);
                purchaseInStockExecutesRef.set(List.of(executeDO));
                return 1;
            }
            if ("selectListByPurchaseInId".equals(methodName)) {
                return purchaseInStockExecutesRef.get();
            }
            if ("selectLatestExecutedByPurchaseInId".equals(methodName)) {
                return purchaseInStockExecutesRef.get().stream()
                        .filter(item -> ErpPurchaseInStockExecuteStatusEnum.EXECUTED.getStatus().equals(item.getStatus()))
                        .findFirst()
                        .orElse(null);
            }
            if ("updateStatusByPurchaseInId".equals(methodName)) {
                return 1;
            }
            return null;
        });
    }

    private ErpPurchaseInStockExecuteItemMapper createPurchaseInStockExecuteItemMapperProxy() {
        return createProxy(ErpPurchaseInStockExecuteItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpPurchaseInStockExecuteItemDO item = (ErpPurchaseInStockExecuteItemDO) args[0];
                List<ErpPurchaseInStockExecuteItemDO> items = new ArrayList<>(purchaseInStockExecuteItemsRef.get());
                items.add(item);
                insertedStockExecuteItemsRef.set(items);
                purchaseInStockExecuteItemsRef.set(items);
                return 1;
            }
            if ("insertBatch".equals(methodName)) {
                insertedStockExecuteItemsRef.set(new ArrayList<>((Collection<ErpPurchaseInStockExecuteItemDO>) args[0]));
                purchaseInStockExecuteItemsRef.set(insertedStockExecuteItemsRef.get());
                return true;
            }
            if ("selectListByPurchaseInId".equals(methodName)) {
                return purchaseInStockExecuteItemsRef.get();
            }
            if ("selectListByExecuteIds".equals(methodName)) {
                return purchaseInStockExecuteItemsRef.get();
            }
            return null;
        });
    }

    private ErpPurchaseOrderService createPurchaseOrderServiceProxy() {
        return createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("validatePurchaseOrder".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("getPurchaseOrderItemListByOrderId".equals(methodName)) {
                return purchaseOrderItemsRef.get();
            }
            if ("updatePurchaseOrderInCount".equals(methodName)) {
                orderIdRef.set((Long) args[0]);
                orderInCountMapRef.set((Map<Long, BigDecimal>) args[1]);
            }
            return null;
        });
    }

    private cn.iocoder.yudao.module.erp.service.product.ErpProductService createProductServiceProxy() {
        return createProxy(cn.iocoder.yudao.module.erp.service.product.ErpProductService.class, (methodName, args) -> {
            if ("validProductList".equals(methodName)) {
                return productListRef.get();
            }
            if ("getProductVOMap".equals(methodName)) {
                return productListRef.get().stream().collect(java.util.stream.Collectors.toMap(
                        ErpProductDO::getId,
                        product -> {
                            ErpProductRespVO respVO = new ErpProductRespVO();
                            respVO.setId(product.getId());
                            respVO.setUnitId(product.getUnitId());
                            respVO.setBatchControlFlag(Boolean.FALSE);
                            return respVO;
                        }));
            }
            return null;
        });
    }

    private ErpPurchaseSourceBatchService createPurchaseSourceBatchServiceProxy() {
        return createProxy(ErpPurchaseSourceBatchService.class, (methodName, args) -> {
            if ("getPurchaseSourceBatchMap".equals(methodName)) {
                return Map.<Long, ErpPurchaseSourceBatchDO>of();
            }
            return null;
        });
    }

    private cn.iocoder.yudao.module.erp.service.finance.ErpAccountService createAccountServiceProxy() {
        return createProxy(cn.iocoder.yudao.module.erp.service.finance.ErpAccountService.class, (methodName, args) -> {
            if ("validateAccount".equals(methodName)) {
                validatedAccountIdRef.set((Long) args[0]);
            }
            return null;
        });
    }

    private ErpStockRecordService createStockRecordServiceProxy() {
        return createProxy(ErpStockRecordService.class, (methodName, args) -> {
            if ("createStockRecord".equals(methodName)) {
                stockRecords.add((ErpStockRecordCreateReqBO) args[0]);
            }
            return null;
        });
    }

    private ErpPurchaseInQualityService createPurchaseInQualityServiceProxy() {
        return createProxy(ErpPurchaseInQualityService.class, (methodName, args) -> {
            if ("createQualityOrderIfAbsent".equals(methodName)) {
                createdQualityPurchaseInIdRef.set((Long) args[0]);
                return 66L;
            }
            if ("voidQualityOrderByPurchaseIn".equals(methodName)) {
                voidedQualityPurchaseInIdRef.set((Long) args[0]);
                return null;
            }
            if ("submitPurchaseInQualityByPurchaseIn".equals(methodName)) {
                if (delegatedQualityCheckExceptionRef.get() != null) {
                    throw delegatedQualityCheckExceptionRef.get();
                }
                delegatedQualityCheckReqRef.set((ErpPurchaseInQualityCheckReqVO) args[1]);
                return null;
            }
            return null;
        });
    }

    private ErpApStatementService createApStatementServiceProxy() {
        return createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("createStatementForPurchaseIn".equals(methodName)) {
                createdApStatementPurchaseInIdRef.set(((ErpPurchaseInDO) args[0]).getId());
                return null;
            }
            if ("closeStatementByBiz".equals(methodName)) {
                closedStatementBizTypeRef.set((Integer) args[0]);
                closedStatementBizIdRef.set((Long) args[1]);
                return null;
            }
            return null;
        });
    }

    private ErpFinanceBizHookService createFinanceBizHookServiceProxy() {
        return createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                autoGenerateVoucherBizTypeRef.set((Integer) args[0]);
                autoGenerateVoucherBizIdRef.set((Long) args[1]);
                financeHookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        });
    }

    private ErpFinancePaymentAllocateMapper createPaymentAllocateMapperProxy() {
        return createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectCountByBizTypeAndBizIdAndStatus".equals(methodName)) {
                assertEquals(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus(), args[2]);
                return approvedAllocateCountRef.get();
            }
            return null;
        });
    }

    private ErpPurchaseInDO purchaseIn(Long id, Long orderId, String no, Integer status, Integer qaStatus) {
        return new ErpPurchaseInDO()
                .setId(id)
                .setOrderId(orderId)
                .setNo(no)
                .setStatus(status)
                .setQaStatus(qaStatus);
    }

    private ErpPurchaseInItemDO purchaseInItem(Long id, Long inId, Long orderItemId,
                                               Long productId, Long warehouseId, String count) {
        return new ErpPurchaseInItemDO()
                .setId(id)
                .setInId(inId)
                .setOrderItemId(orderItemId)
                .setProductId(productId)
                .setWarehouseId(warehouseId)
                .setCount(new BigDecimal(count));
    }

    private ErpPurchaseOrderDO purchaseOrder(Long id, String no, Long supplierId, Long accountId) {
        return new ErpPurchaseOrderDO()
                .setId(id)
                .setNo(no)
                .setSupplierId(supplierId)
                .setAccountId(accountId)
                .setStatus(ErpAuditStatus.APPROVE.getStatus());
    }

    private ErpPurchaseOrderItemDO purchaseOrderItem(Long id, Long orderId, Long productId,
                                                     String count, String inCount) {
        return new ErpPurchaseOrderItemDO()
                .setId(id)
                .setOrderId(orderId)
                .setProductId(productId)
                .setCount(new BigDecimal(count))
                .setInCount(new BigDecimal(inCount));
    }

    private ErpProductDO product(Long id, Long unitId) {
        return new ErpProductDO().setId(id).setUnitId(unitId);
    }

    private ErpPurchaseInSaveReqVO saveReq(Long id, Long orderId, ErpPurchaseInSaveReqVO.Item... items) {
        ErpPurchaseInSaveReqVO reqVO = new ErpPurchaseInSaveReqVO();
        reqVO.setId(id);
        reqVO.setOrderId(orderId);
        reqVO.setAccountId(301L);
        reqVO.setInTime(LocalDateTime.of(2026, 4, 9, 10, 0));
        reqVO.setDiscountPercent(BigDecimal.ZERO);
        reqVO.setOtherPrice(BigDecimal.ZERO);
        reqVO.setItems(List.of(items));
        return reqVO;
    }

    private ErpPurchaseInSaveReqVO.Item saveItem(Long id, Long orderItemId, Long warehouseId,
                                                 Long productId, Long productUnitId, String count) {
        ErpPurchaseInSaveReqVO.Item item = new ErpPurchaseInSaveReqVO.Item();
        item.setId(id);
        item.setOrderItemId(orderItemId);
        item.setWarehouseId(warehouseId);
        item.setProductId(productId);
        item.setProductUnitId(productUnitId);
        item.setProductPrice(new BigDecimal("10"));
        item.setCount(new BigDecimal(count));
        item.setTaxPercent(BigDecimal.ZERO);
        return item;
    }

    private ErpPurchaseInQualityCheckReqVO qualityCheckReq(Long id, String remark,
                                                           ErpPurchaseInQualityCheckReqVO.Item... items) {
        ErpPurchaseInQualityCheckReqVO reqVO = new ErpPurchaseInQualityCheckReqVO();
        reqVO.setId(id);
        reqVO.setRemark(remark);
        reqVO.setItems(List.of(items));
        return reqVO;
    }

    private ErpPurchaseInQualityCheckReqVO.Item qualityCheckItem(Long id, String qaPassCount,
                                                                 String qaRejectCount, String qaRemark) {
        ErpPurchaseInQualityCheckReqVO.Item item = new ErpPurchaseInQualityCheckReqVO.Item();
        item.setId(id);
        item.setQaPassCount(new BigDecimal(qaPassCount));
        item.setQaRejectCount(new BigDecimal(qaRejectCount));
        item.setQaRemark(qaRemark);
        return item;
    }

    private ErpPurchaseInBatchUpdateReqVO batchReq(String fieldKey, String value) {
        ErpPurchaseInBatchUpdateReqVO reqVO = new ErpPurchaseInBatchUpdateReqVO();
        reqVO.setIds(List.of(1L));
        reqVO.setFieldKey(fieldKey);
        reqVO.setMode("overwrite");
        reqVO.setValue(value);
        return reqVO;
    }

    private ErpPurchaseInConfirmStockInReqVO confirmStockInReq(Long id) {
        ErpPurchaseInConfirmStockInReqVO reqVO = new ErpPurchaseInConfirmStockInReqVO();
        reqVO.setId(id);
        return reqVO;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
