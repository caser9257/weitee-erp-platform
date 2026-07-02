package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceCostDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceIssueCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceInboundCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceFeeSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceLossDetailRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceLossEntryCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceOrderCloseReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceReconciliationRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.ErpOutsourceReturnCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceIssueBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceIssueDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceIssueItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceLossDetailDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceReturnBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceFeeMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceInboundMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceIssueBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceIssueItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceIssueMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceLossDetailMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceReturnBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpOutsourceReturnMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpOutsourceIssueTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpOutsourceOrderTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpOutsourceOrderServiceImplTest {

    @Test
    void createOutsourceOrder_shouldRequireBomForBomType() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "supplierService", createProxyByName(
                "cn.weitee.erp.module.erp.service.purchase.ErpSupplierService", (methodName, args) -> null));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "bomService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpBomService", (methodName, args) -> null));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceOrder(
                new ErpOutsourceOrderSaveReqVO()
                        .setOrderType(ErpOutsourceOrderTypeEnum.BOM.getType())
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("10"))));

        assertEquals(1_030_700_000, ex.getCode());
    }

    @Test
    void createOutsourceOrder_shouldRejectBomIdForSimpleType() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "supplierService", createProxyByName(
                "cn.weitee.erp.module.erp.service.purchase.ErpSupplierService", (methodName, args) -> null));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceOrder(
                new ErpOutsourceOrderSaveReqVO()
                        .setOrderType(ErpOutsourceOrderTypeEnum.SIMPLE.getType())
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setBomId(9L)
                        .setPlannedQty(new BigDecimal("10"))));

        assertEquals(1_030_700_037, ex.getCode());
    }

    @Test
    void createOutsourceOrder_shouldRejectBomOfDifferentProduct() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "supplierService", createProxyByName(
                "cn.weitee.erp.module.erp.service.purchase.ErpSupplierService", (methodName, args) -> null));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "bomService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpBomService", (methodName, args) -> {
                    if ("getBom".equals(methodName)) {
                        return new ErpBomDO().setId(9L).setProductId(200L);
                    }
                    return null;
                }));
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpOutsourceOrderDO) args[0]).setId(1L);
                return 1;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWDD202605210009";
            }
        });

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceOrder(
                new ErpOutsourceOrderSaveReqVO()
                        .setOrderType(ErpOutsourceOrderTypeEnum.BOM.getType())
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setBomId(9L)
                        .setPlannedQty(new BigDecimal("10"))));

        assertEquals(1_030_700_039, ex.getCode());
    }

    @Test
    void createOutsourceOrder_shouldPersistBomTypeWithBomId() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceOrderDO> insertedOrderRef = new AtomicReference<>();
        setField(service, "supplierService", createProxyByName(
                "cn.weitee.erp.module.erp.service.purchase.ErpSupplierService", (methodName, args) -> null));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "bomService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpBomService", (methodName, args) -> {
                    if ("getBom".equals(methodName)) {
                        return new ErpBomDO().setId(9L).setProductId(100L);
                    }
                    return null;
                }));
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpOutsourceOrderDO order = (ErpOutsourceOrderDO) args[0];
                order.setId(1L);
                insertedOrderRef.set(order);
                return 1;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWDD202605210001";
            }
        });

        Long orderId = service.createOutsourceOrder(new ErpOutsourceOrderSaveReqVO()
                .setOrderType(ErpOutsourceOrderTypeEnum.BOM.getType())
                .setSupplierId(10L)
                .setProductId(100L)
                .setBomId(9L)
                .setPlannedQty(new BigDecimal("10"))
                .setProcessName("喷涂"));

        assertEquals(1L, orderId);
        assertNotNull(insertedOrderRef.get());
        assertEquals(ErpOutsourceOrderTypeEnum.BOM.getType(), insertedOrderRef.get().getOrderType());
        assertEquals(9L, insertedOrderRef.get().getBomId());
        assertEquals("WWDD202605210001", insertedOrderRef.get().getNo());
    }

    @Test
    void createOutsourceIssue_shouldRejectSimpleOrderMaterialTracking() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setOrderType(ErpOutsourceOrderTypeEnum.SIMPLE.getType())
                        .setStatus(10);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceIssue(
                new ErpOutsourceIssueCreateReqVO().setOrderId(1L)));

        assertEquals(1_030_700_038, ex.getCode());
    }

    @Test
    void createOutsourceReturn_shouldRejectSimpleOrderMaterialTracking() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setOrderType(ErpOutsourceOrderTypeEnum.SIMPLE.getType())
                        .setStatus(10);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceReturn(
                new ErpOutsourceReturnCreateReqVO().setOrderId(1L)));

        assertEquals(1_030_700_038, ex.getCode());
    }

    @Test
    void closeOutsourceOrder_shouldRejectLossItemsForSimpleOrder() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setOrderType(ErpOutsourceOrderTypeEnum.SIMPLE.getType())
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setStatus(20);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.closeOutsourceOrder(
                new ErpOutsourceOrderCloseReqVO()
                        .setOrderId(1L)
                        .setLossQty(new BigDecimal("2"))
                        .setLossItems(List.of(new ErpOutsourceOrderCloseReqVO.LossItem()
                                .setIssueBatchId(31L)
                                .setLossQty(new BigDecimal("2"))))));

        assertEquals(1_030_700_038, ex.getCode());
    }

    @Test
    void createOutsourceLossEntry_shouldRejectSimpleOrderMaterialTracking() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setOrderType(ErpOutsourceOrderTypeEnum.SIMPLE.getType())
                        .setStatus(40);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceLossEntry(
                new ErpOutsourceLossEntryCreateReqVO()
                        .setOrderId(1L)
                        .setEntries(List.of(new ErpOutsourceLossEntryCreateReqVO.Entry()
                                .setIssueBatchId(31L)
                                .setLossQty(new BigDecimal("1"))))));

        assertEquals(1_030_700_038, ex.getCode());
    }

    @Test
    void createOutsourceInbound_shouldCalculateInboundCostFromNetMaterialAndFee() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceInboundDO> insertedInboundRef = new AtomicReference<>();
        AtomicReference<Object> inboundBatchReqRef = new AtomicReference<>();
        AtomicReference<Object> stockRecordReqRef = new AtomicReference<>();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(BigDecimal.ZERO)
                        .setStatus(10);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(
                        new ErpOutsourceIssueDO().setId(11L).setOrderId(1L).setIssueAmount(new BigDecimal("100.00")),
                        new ErpOutsourceIssueDO().setId(12L).setOrderId(1L).setIssueAmount(new BigDecimal("20.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnMapper", createProxy(ErpOutsourceReturnMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceReturnDO().setId(21L).setOrderId(1L).setReturnAmount(new BigDecimal("15.00")));
            }
            return null;
        }));
        setField(service, "outsourceFeeMapper", createProxy(ErpOutsourceFeeMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(
                        new ErpOutsourceFeeDO().setId(31L).setOrderId(1L).setFeeAmount(new BigDecimal("30.00")),
                        new ErpOutsourceFeeDO().setId(32L).setOrderId(1L).setFeeAmount(new BigDecimal("5.00")));
            }
            return null;
        }));
        setField(service, "outsourceInboundMapper", createProxy(ErpOutsourceInboundMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpOutsourceInboundDO inbound = (ErpOutsourceInboundDO) args[0];
                inbound.setId(41L);
                insertedInboundRef.set(inbound);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("createOrIncreaseBatch".equals(methodName)) {
                inboundBatchReqRef.set(args[0]);
                return new ErpStockBatchDO().setId(501L).setBatchNo("CP202604280001");
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> {
            if ("createStockRecord".equals(methodName)) {
                stockRecordReqRef.set(args[0]);
            }
            return null;
        }));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "supplierService", createProxyByName(
                "cn.weitee.erp.module.erp.service.purchase.ErpSupplierService", (methodName, args) -> null));
        setField(service, "warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> null));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWRK202604280001";
            }
        });

        Long inboundId = service.createOutsourceInbound(new ErpOutsourceInboundCreateReqVO()
                .setOrderId(1L)
                .setWarehouseId(1000L)
                .setInboundQty(new BigDecimal("10"))
                .setBatchNo("CP202604280001")
                .setProduceDate(LocalDate.of(2026, 4, 28))
                .setRemark("委外完工入库"));

        assertEquals(41L, inboundId);
        assertNotNull(insertedInboundRef.get());
        assertEquals(new BigDecimal("105.00"), insertedInboundRef.get().getMaterialCost().setScale(2));
        assertEquals(new BigDecimal("35.00"), insertedInboundRef.get().getProcessFee().setScale(2));
        assertEquals(new BigDecimal("140.00"), insertedInboundRef.get().getTotalCost().setScale(2));
        assertEquals(new BigDecimal("14.000000"), insertedInboundRef.get().getUnitCost());
        assertNotNull(inboundBatchReqRef.get());
        assertNotNull(stockRecordReqRef.get());
        assertEquals(new BigDecimal("10"), updatedOrderRef.get().getFinishedQty());
    }

    @Test
    void getOutsourceCostDetail_shouldAggregateNetMaterialAndProcessFee() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("15"))
                        .setStatus(20);
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(
                        new ErpOutsourceIssueDO().setId(11L).setOrderId(1L).setIssueNo("WWFL001").setIssueAmount(new BigDecimal("80.00")),
                        new ErpOutsourceIssueDO().setId(12L).setOrderId(1L).setIssueNo("WWFL002").setIssueAmount(new BigDecimal("20.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnMapper", createProxy(ErpOutsourceReturnMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceReturnDO().setId(21L).setOrderId(1L).setReturnNo("WWTL001").setReturnAmount(new BigDecimal("10.00")));
            }
            return null;
        }));
        setField(service, "outsourceFeeMapper", createProxy(ErpOutsourceFeeMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(
                        new ErpOutsourceFeeDO().setId(31L).setOrderId(1L).setFeeAmount(new BigDecimal("30.00")),
                        new ErpOutsourceFeeDO().setId(32L).setOrderId(1L).setFeeAmount(new BigDecimal("5.00")));
            }
            return null;
        }));
        setField(service, "outsourceInboundMapper", createProxy(ErpOutsourceInboundMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceInboundDO().setId(41L).setOrderId(1L).setInboundNo("WWRK001")
                        .setInboundQty(new BigDecimal("15")).setTotalCost(new BigDecimal("125.00")));
            }
            return null;
        }));

        ErpOutsourceCostDetailRespVO detail = service.getOutsourceCostDetail(1L);

        assertEquals(new BigDecimal("90.00"), detail.getNetMaterialCost().setScale(2));
        assertEquals(new BigDecimal("35.00"), detail.getProcessFee().setScale(2));
        assertEquals(new BigDecimal("125.00"), detail.getTotalCost().setScale(2));
        assertEquals(new BigDecimal("8.333333"), detail.getUnitCost());
        assertEquals(2, detail.getIssueDetails().size());
        assertEquals(1, detail.getReturnDetails().size());
        assertEquals(2, detail.getFeeDetails().size());
        assertEquals(1, detail.getInboundDetails().size());
    }

    @Test
    void createOutsourceFee_shouldCreateApStatement() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceFeeDO> insertedFeeRef = new AtomicReference<>();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();
        AtomicReference<Object[]> apStatementArgsRef = new AtomicReference<>();
        AtomicReference<Integer> financeHookBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> financeHookBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> financeHookBizDateRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(BigDecimal.ZERO)
                        .setStatus(10);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceFeeMapper", createProxy(ErpOutsourceFeeMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpOutsourceFeeDO fee = (ErpOutsourceFeeDO) args[0];
                fee.setId(51L);
                insertedFeeRef.set(fee);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("createStatementForOutsourceFee".equals(methodName)) {
                apStatementArgsRef.set(args);
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                financeHookBizTypeRef.set((Integer) args[0]);
                financeHookBizIdRef.set((Long) args[1]);
                financeHookBizDateRef.set((LocalDate) args[2]);
                return 71L;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWFY202604280001";
            }
        });

        Long feeId = service.createOutsourceFee(new ErpOutsourceFeeSaveReqVO()
                .setOrderId(1L)
                .setFeeTime(LocalDateTime.of(2026, 4, 28, 16, 30))
                .setFeeAmount(new BigDecimal("66.00"))
                .setRemark("委外加工费"));

        assertEquals(51L, feeId);
        assertNotNull(insertedFeeRef.get());
        assertEquals("WWFY202604280001", insertedFeeRef.get().getFeeNo());
        assertEquals(new BigDecimal("66.00"), insertedFeeRef.get().getFeeAmount());
        assertNotNull(apStatementArgsRef.get());
        assertSame(insertedFeeRef.get(), apStatementArgsRef.get()[0]);
        assertEquals(1L, ((ErpOutsourceOrderDO) apStatementArgsRef.get()[1]).getId());
        assertEquals(20, updatedOrderRef.get().getStatus());
        assertEquals(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), financeHookBizTypeRef.get());
        assertEquals(51L, financeHookBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 28), financeHookBizDateRef.get());
    }

    @Test
    void createOutsourceIssue_shouldPersistSupplementIssueType() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceIssueDO> insertedIssueRef = new AtomicReference<>();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("20"))
                        .setIssuedQty(BigDecimal.ZERO)
                        .setFinishedQty(BigDecimal.ZERO)
                        .setStatus(10);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpOutsourceIssueDO issue = (ErpOutsourceIssueDO) args[0];
                issue.setId(101L);
                insertedIssueRef.set(issue);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpOutsourceIssueItemDO) args[0]).setId(201L);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpOutsourceIssueBatchDO) args[0]).setId(301L);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInItemMapper", createProxyByName(
                "cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper", (methodName, args) -> {
                    if ("selectListByIds".equals(methodName)) {
                        return List.of(new ErpPurchaseInItemDO()
                                .setId(401L)
                                .setCount(new BigDecimal("10"))
                                .setTotalPrice(new BigDecimal("40.00")));
                    }
                    return null;
                }));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> null));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getStockBatchMap".equals(methodName)) {
                return Map.of(501L, new ErpStockBatchDO()
                        .setId(501L)
                        .setProductId(1001L)
                        .setWarehouseId(11L)
                        .setBatchNo("BATCH-001")
                        .setSourceBizType("PURCHASE_IN")
                        .setSourceBizItemId(401L));
            }
            return null;
        }));
        setDefaultStockService(service);
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWFL202604280001";
            }
        });

        Long issueId = service.createOutsourceIssue(new ErpOutsourceIssueCreateReqVO()
                .setOrderId(1L)
                .setIssueType(ErpOutsourceIssueTypeEnum.SUPPLEMENT.getType())
                .setRemark("补料发料")
                .setItems(List.of(new ErpOutsourceIssueCreateReqVO.Item()
                        .setMaterialId(1001L)
                        .setWarehouseId(11L)
                        .setIssueQty(new BigDecimal("10"))
                        .setRemark("补料")
                        .setBatches(List.of(new ErpOutsourceIssueCreateReqVO.Batch()
                                .setStockBatchId(501L)
                                .setBatchNo("BATCH-001")
                                .setIssueQty(new BigDecimal("10")))))));

        assertEquals(101L, issueId);
        assertNotNull(insertedIssueRef.get());
        assertEquals(ErpOutsourceIssueTypeEnum.SUPPLEMENT.getType(), insertedIssueRef.get().getIssueType());
        assertEquals(new BigDecimal("10"), updatedOrderRef.get().getIssuedQty());
    }

    @Test
    void getOutsourceReconciliationDetail_shouldSplitNormalAndSupplementIssueAmount() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setStatus(20);
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(
                        new ErpOutsourceIssueDO().setId(11L).setIssueNo("WWFL001")
                                .setIssueType(ErpOutsourceIssueTypeEnum.NORMAL.getType())
                                .setIssueAmount(new BigDecimal("80.00")),
                        new ErpOutsourceIssueDO().setId(12L).setIssueNo("WWFL002")
                                .setIssueType(ErpOutsourceIssueTypeEnum.SUPPLEMENT.getType())
                                .setIssueAmount(new BigDecimal("15.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnMapper", createProxy(ErpOutsourceReturnMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceReturnDO().setId(21L).setReturnNo("WWTL001").setReturnAmount(new BigDecimal("5.00")));
            }
            return null;
        }));
        setField(service, "outsourceFeeMapper", createProxy(ErpOutsourceFeeMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceFeeDO().setId(31L).setFeeNo("WWFY001").setFeeAmount(new BigDecimal("20.00")));
            }
            return null;
        }));
        setField(service, "outsourceInboundMapper", createProxy(ErpOutsourceInboundMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceInboundDO().setId(41L).setInboundNo("WWRK001")
                        .setInboundQty(new BigDecimal("18")).setTotalCost(new BigDecimal("110.00")));
            }
            return null;
        }));

        ErpOutsourceReconciliationRespVO detail = service.getOutsourceReconciliationDetail(1L);

        assertEquals(new BigDecimal("80.00"), detail.getNormalIssueAmount().setScale(2));
        assertEquals(new BigDecimal("15.00"), detail.getSupplementIssueAmount().setScale(2));
        assertEquals(new BigDecimal("95.00"), detail.getTotalIssueAmount().setScale(2));
        assertEquals(new BigDecimal("5.00"), detail.getReturnAmount().setScale(2));
        assertEquals(new BigDecimal("90.00"), detail.getNetMaterialCost().setScale(2));
        assertEquals(new BigDecimal("20.00"), detail.getProcessFee().setScale(2));
        assertEquals(new BigDecimal("110.00"), detail.getTotalCost().setScale(2));
        assertEquals(new BigDecimal("2"), detail.getPendingInboundQty());
        assertEquals(BigDecimal.ZERO, detail.getOverInboundQty());
        assertEquals(Boolean.TRUE, detail.getHasSupplementIssue());
        assertEquals(2, detail.getIssueDetails().size());
        assertEquals("补料发料", detail.getIssueDetails().get(1).getIssueTypeName());
    }

    @Test
    void createOutsourceFee_shouldAllowCompletedOrderAndKeepCompletedStatus() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();
        AtomicReference<Integer> autoGenerateBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> autoGenerateBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> financeHookBizDateRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setSupplierId(10L)
                        .setProductId(100L)
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("20"))
                        .setStatus(30);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceFeeMapper", createProxy(ErpOutsourceFeeMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ((ErpOutsourceFeeDO) args[0]).setId(61L);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> null));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                autoGenerateBizTypeRef.set((Integer) args[0]);
                autoGenerateBizIdRef.set((Long) args[1]);
                financeHookBizDateRef.set((LocalDate) args[2]);
                return 88L;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "WWFY202604280002";
            }
        });

        Long feeId = service.createOutsourceFee(new ErpOutsourceFeeSaveReqVO()
                .setOrderId(1L)
                .setFeeTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                .setFeeAmount(new BigDecimal("88.00"))
                .setRemark("完工后补记加工费"));

        assertEquals(61L, feeId);
        assertNotNull(updatedOrderRef.get());
        assertEquals(30, updatedOrderRef.get().getStatus());
        assertEquals(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), autoGenerateBizTypeRef.get());
        assertEquals(61L, autoGenerateBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 29), financeHookBizDateRef.get());
    }

    @Test
    void closeOutsourceOrder_shouldSetLossQtyAndClosedStatus() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setStatus(20);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.closeOutsourceOrder(new ErpOutsourceOrderCloseReqVO()
                .setOrderId(1L)
                .setLossQty(new BigDecimal("2"))
                .setCloseRemark("确认损耗结案"));

        assertNotNull(updatedOrderRef.get());
        assertEquals(new BigDecimal("2"), updatedOrderRef.get().getLossQty());
        assertEquals(40, updatedOrderRef.get().getStatus());
        assertEquals("确认损耗结案", updatedOrderRef.get().getCloseRemark());
        assertNotNull(updatedOrderRef.get().getCloseTime());
    }

    @Test
    void closeOutsourceOrder_shouldRejectWhenLossQtyStillNotEnough() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setStatus(20);
            }
            return null;
        }));

        assertThrows(RuntimeException.class, () -> service.closeOutsourceOrder(new ErpOutsourceOrderCloseReqVO()
                .setOrderId(1L)
                .setLossQty(new BigDecimal("1"))
                .setCloseRemark("损耗不足")));
    }

    @Test
    void closeOutsourceOrder_shouldPersistLossDetailWhenProvided() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceOrderDO> updatedOrderRef = new AtomicReference<>();
        AtomicReference<ErpOutsourceLossDetailDO> insertedLossDetailRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setStatus(20);
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpOutsourceOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceIssueDO().setId(11L).setOrderId(1L));
            }
            return null;
        }));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> {
            if ("selectListByIssueIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueItemDO().setId(21L).setIssueId(11L).setMaterialId(1001L).setWarehouseId(11L));
            }
            return null;
        }));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueItemIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueBatchDO()
                        .setId(31L)
                        .setIssueItemId(21L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setIssueQty(new BigDecimal("2"))
                        .setIssueAmount(new BigDecimal("10.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnBatchMapper", createProxy(ErpOutsourceReturnBatchMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceLossDetailMapper", createProxy(ErpOutsourceLossDetailMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName) || "selectListByIssueBatchIds".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                ErpOutsourceLossDetailDO detail = (ErpOutsourceLossDetailDO) args[0];
                detail.setId(41L);
                insertedLossDetailRef.set(detail);
                return 1;
            }
            return null;
        }));

        service.closeOutsourceOrder(new ErpOutsourceOrderCloseReqVO()
                .setOrderId(1L)
                .setLossQty(new BigDecimal("2"))
                .setCloseRemark("按批次登记损耗")
                .setLossItems(List.of(new ErpOutsourceOrderCloseReqVO.LossItem()
                        .setIssueBatchId(31L)
                        .setLossQty(new BigDecimal("2"))
                        .setRemark("整批损耗"))));

        assertNotNull(updatedOrderRef.get());
        assertEquals(40, updatedOrderRef.get().getStatus());
        assertNotNull(insertedLossDetailRef.get());
        assertEquals(1L, insertedLossDetailRef.get().getOrderId());
        assertEquals(31L, insertedLossDetailRef.get().getIssueBatchId());
        assertEquals(new BigDecimal("2"), insertedLossDetailRef.get().getLossQty());
        assertEquals(new BigDecimal("10.00"), insertedLossDetailRef.get().getLossAmount().setScale(2));
    }

    @Test
    void getOutsourceLossDetail_shouldBuildBatchLevelLossView() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setLossQty(new BigDecimal("2"))
                        .setCloseRemark("已确认损耗");
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceIssueDO().setId(11L).setOrderId(1L));
            }
            return null;
        }));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> {
            if ("selectListByIssueIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueItemDO().setId(21L).setIssueId(11L).setMaterialId(1001L).setWarehouseId(11L));
            }
            return null;
        }));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueItemIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueBatchDO()
                        .setId(31L)
                        .setIssueItemId(21L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setIssueQty(new BigDecimal("5"))
                        .setIssueAmount(new BigDecimal("50.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnBatchMapper", createProxy(ErpOutsourceReturnBatchMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName) || "selectListByIssueBatchIds".equals(methodName)) {
                return List.of(new ErpOutsourceReturnBatchDO()
                        .setIssueBatchId(31L)
                        .setReturnQty(new BigDecimal("1"))
                        .setReturnAmount(new BigDecimal("10.00")));
            }
            return null;
        }));
        setField(service, "outsourceLossDetailMapper", createProxy(ErpOutsourceLossDetailMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceLossDetailDO()
                        .setOrderId(1L)
                        .setIssueBatchId(31L)
                        .setMaterialId(1001L)
                        .setWarehouseId(11L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setLossQty(new BigDecimal("2"))
                        .setLossAmount(new BigDecimal("20.00"))
                        .setRemark("existing"));
            }
            if ("selectListByIssueBatchIds".equals(methodName)) {
                return List.of(new ErpOutsourceLossDetailDO()
                        .setOrderId(1L)
                        .setIssueBatchId(31L)
                        .setMaterialId(1001L)
                        .setWarehouseId(11L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setLossQty(new BigDecimal("2"))
                        .setLossAmount(new BigDecimal("20.00"))
                        .setRemark("登记损耗"));
            }
            return List.of();
        }));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> {
                    if ("getProductVOMap".equals(methodName)) {
                        return Map.of(1001L, new ErpProductRespVO()
                                .setId(1001L)
                                .setName("喷涂件")
                                .setMaterialCode("MAT-001")
                                .setBarCode("BAR-001")
                                .setUnitName("PCS"));
                    }
                    return null;
                }));
        setField(service, "warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> {
                    if ("getWarehouseMap".equals(methodName)) {
                        return Map.of(11L, new ErpWarehouseDO().setId(11L).setName("委外仓"));
                    }
                    return null;
                }));

        ErpOutsourceLossDetailRespVO detail = service.getOutsourceLossDetail(1L);

        assertEquals(new BigDecimal("2"), detail.getOrderLossQty());
        assertEquals(new BigDecimal("2"), detail.getRecordedLossQty());
        assertEquals(BigDecimal.ZERO, detail.getPendingBackfillQty());
        assertEquals(Boolean.FALSE, detail.getHasPendingBackfill());
        assertEquals(BigDecimal.ZERO, detail.getUnresolvedQty());
        assertEquals(1, detail.getDetails().size());
        assertEquals("喷涂件", detail.getDetails().get(0).getMaterialName());
        assertEquals("委外仓", detail.getDetails().get(0).getWarehouseName());
        assertEquals(new BigDecimal("2"), detail.getDetails().get(0).getLossQty());
        assertEquals(new BigDecimal("2"), detail.getDetails().get(0).getAvailableLossQty());
        assertEquals(new BigDecimal("10.000000"), detail.getDetails().get(0).getUnitPrice());
        assertEquals(1, detail.getEntries().size());
    }

    @Test
    void createOutsourceLossEntry_shouldRejectWhenLossSourceMissing() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setLossQty(new BigDecimal("5"))
                        .setStatus(40);
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceReturnBatchMapper", createProxy(ErpOutsourceReturnBatchMapper.class, (methodName, args) -> List.of()));
        setField(service, "outsourceLossDetailMapper", createProxy(ErpOutsourceLossDetailMapper.class, (methodName, args) -> List.of()));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createOutsourceLossEntry(
                new ErpOutsourceLossEntryCreateReqVO()
                        .setOrderId(1L)
                        .setEntries(List.of(new ErpOutsourceLossEntryCreateReqVO.Entry()
                                .setIssueBatchId(31L)
                                .setLossQty(new BigDecimal("1"))
                                .setRemark("历史补录")))));

        assertEquals(1_030_700_036, ex.getCode());
    }

    @Test
    void createOutsourceLossEntry_shouldAllowClosedOrderBatchSupplement() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();
        AtomicReference<ErpOutsourceLossDetailDO> insertedLossDetailRef = new AtomicReference<>();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setLossQty(new BigDecimal("5"))
                        .setStatus(40);
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceIssueDO().setId(11L).setOrderId(1L));
            }
            return null;
        }));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> {
            if ("selectListByIssueIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueItemDO().setId(21L).setIssueId(11L).setMaterialId(1001L).setWarehouseId(11L));
            }
            return null;
        }));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueItemIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueBatchDO()
                        .setId(31L)
                        .setIssueItemId(21L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setIssueQty(new BigDecimal("5"))
                        .setIssueAmount(new BigDecimal("50.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnBatchMapper", createProxy(ErpOutsourceReturnBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueBatchIds".equals(methodName)) {
                return List.of(new ErpOutsourceReturnBatchDO()
                        .setIssueBatchId(31L)
                        .setReturnQty(new BigDecimal("1"))
                        .setReturnAmount(new BigDecimal("10.00")));
            }
            return List.of();
        }));
        setField(service, "outsourceLossDetailMapper", createProxy(ErpOutsourceLossDetailMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceLossDetailDO()
                        .setId(41L)
                        .setOrderId(1L)
                        .setIssueBatchId(31L)
                        .setMaterialId(1001L)
                        .setWarehouseId(11L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setLossQty(new BigDecimal("1"))
                        .setLossAmount(new BigDecimal("10.00"))
                        .setRemark("first"));
            }
            if ("selectListByIssueBatchIds".equals(methodName)) {
                return List.of(new ErpOutsourceLossDetailDO()
                        .setId(41L)
                        .setOrderId(1L)
                        .setIssueBatchId(31L)
                        .setMaterialId(1001L)
                        .setWarehouseId(11L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setLossQty(new BigDecimal("1"))
                        .setLossAmount(new BigDecimal("10.00"))
                        .setRemark("first"));
            }
            if ("insert".equals(methodName)) {
                ErpOutsourceLossDetailDO detail = (ErpOutsourceLossDetailDO) args[0];
                detail.setId(51L);
                insertedLossDetailRef.set(detail);
                return 1;
            }
            return null;
        }));

        service.createOutsourceLossEntry(new ErpOutsourceLossEntryCreateReqVO()
                .setOrderId(1L)
                .setEntries(List.of(new ErpOutsourceLossEntryCreateReqVO.Entry()
                        .setIssueBatchId(31L)
                        .setLossQty(new BigDecimal("2"))
                        .setRemark("second"))));

        assertNotNull(insertedLossDetailRef.get());
        assertEquals(31L, insertedLossDetailRef.get().getIssueBatchId());
        assertEquals(new BigDecimal("2"), insertedLossDetailRef.get().getLossQty());
        assertEquals(new BigDecimal("20.00"), insertedLossDetailRef.get().getLossAmount().setScale(2));
        assertEquals("second", insertedLossDetailRef.get().getRemark());
    }

    @Test
    void getOutsourceLossDetail_shouldAggregateMultiEntriesAndExposePendingBackfill() throws Exception {
        ErpOutsourceOrderServiceImpl service = new ErpOutsourceOrderServiceImpl();

        setField(service, "outsourceOrderMapper", createProxy(ErpOutsourceOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpOutsourceOrderDO()
                        .setId(1L)
                        .setNo("WWDD202604280001")
                        .setPlannedQty(new BigDecimal("20"))
                        .setFinishedQty(new BigDecimal("18"))
                        .setLossQty(new BigDecimal("5"))
                        .setCloseRemark("closed");
            }
            return null;
        }));
        setField(service, "outsourceIssueMapper", createProxy(ErpOutsourceIssueMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName)) {
                return List.of(new ErpOutsourceIssueDO().setId(11L).setOrderId(1L));
            }
            return null;
        }));
        setField(service, "outsourceIssueItemMapper", createProxy(ErpOutsourceIssueItemMapper.class, (methodName, args) -> {
            if ("selectListByIssueIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueItemDO().setId(21L).setIssueId(11L).setMaterialId(1001L).setWarehouseId(11L));
            }
            return null;
        }));
        setField(service, "outsourceIssueBatchMapper", createProxy(ErpOutsourceIssueBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueItemIds".equals(methodName)) {
                return List.of(new ErpOutsourceIssueBatchDO()
                        .setId(31L)
                        .setIssueItemId(21L)
                        .setStockBatchId(501L)
                        .setBatchNo("BATCH-001")
                        .setIssueQty(new BigDecimal("5"))
                        .setIssueAmount(new BigDecimal("50.00")));
            }
            return null;
        }));
        setField(service, "outsourceReturnBatchMapper", createProxy(ErpOutsourceReturnBatchMapper.class, (methodName, args) -> {
            if ("selectListByIssueBatchIds".equals(methodName)) {
                return List.of(new ErpOutsourceReturnBatchDO()
                        .setIssueBatchId(31L)
                        .setReturnQty(new BigDecimal("1"))
                        .setReturnAmount(new BigDecimal("10.00")));
            }
            return List.of();
        }));
        setField(service, "outsourceLossDetailMapper", createProxy(ErpOutsourceLossDetailMapper.class, (methodName, args) -> {
            if ("selectListByOrderId".equals(methodName) || "selectListByIssueBatchIds".equals(methodName)) {
                return List.of(
                        new ErpOutsourceLossDetailDO()
                                .setId(41L)
                                .setOrderId(1L)
                                .setIssueBatchId(31L)
                                .setMaterialId(1001L)
                                .setWarehouseId(11L)
                                .setStockBatchId(501L)
                                .setBatchNo("BATCH-001")
                                .setLossQty(new BigDecimal("2"))
                                .setLossAmount(new BigDecimal("20.00"))
                                .setRemark("first"),
                        new ErpOutsourceLossDetailDO()
                                .setId(42L)
                                .setOrderId(1L)
                                .setIssueBatchId(31L)
                                .setMaterialId(1001L)
                                .setWarehouseId(11L)
                                .setStockBatchId(501L)
                                .setBatchNo("BATCH-001")
                                .setLossQty(new BigDecimal("1"))
                                .setLossAmount(new BigDecimal("10.00"))
                                .setRemark("second"));
            }
            return List.of();
        }));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> {
                    if ("getProductVOMap".equals(methodName)) {
                        return Map.of(1001L, new ErpProductRespVO()
                                .setId(1001L)
                                .setName("Spray Part")
                                .setMaterialCode("MAT-001")
                                .setBarCode("BAR-001")
                                .setUnitName("PCS"));
                    }
                    return null;
                }));
        setField(service, "warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> {
                    if ("getWarehouseMap".equals(methodName)) {
                        return Map.of(11L, new ErpWarehouseDO().setId(11L).setName("Outsource WH"));
                    }
                    return null;
                }));

        ErpOutsourceLossDetailRespVO detail = service.getOutsourceLossDetail(1L);

        assertEquals(new BigDecimal("5"), detail.getOrderLossQty());
        assertEquals(new BigDecimal("3"), detail.getRecordedLossQty());
        assertEquals(new BigDecimal("2"), detail.getPendingBackfillQty());
        assertEquals(Boolean.TRUE, detail.getHasPendingBackfill());
        assertEquals(1, detail.getDetails().size());
        assertEquals(new BigDecimal("3"), detail.getDetails().get(0).getLossQty());
        assertEquals(new BigDecimal("1"), detail.getDetails().get(0).getAvailableLossQty());
        assertEquals(new BigDecimal("30.00"), detail.getDetails().get(0).getLossAmount().setScale(2));
        assertEquals("second", detail.getDetails().get(0).getRemark());
        assertEquals(2, detail.getEntries().size());
        assertEquals(new BigDecimal("10.000000"), detail.getEntries().get(0).getUnitPrice());
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

    @SuppressWarnings("unchecked")
    private <T> T createProxyByName(String className, MethodHandler handler) {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return createProxy(type, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = target.getClass().getDeclaredField(resolveFieldName(fieldName));
        }
        field.setAccessible(true);
        field.set(target, value);
    }

    private String resolveFieldName(String fieldName) {
        if (fieldName.endsWith("Mapper")) {
            return "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return fieldName;
    }

    private void setDefaultStockService(ErpOutsourceOrderServiceImpl service) throws Exception {
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStock".equals(methodName)) {
                return new ErpStockDO().setAverageCost(new BigDecimal("10.00"));
            }
            return null;
        }));
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
