package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionInboundMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductionInboundBatchTraceTest {

    @Mock
    private ErpProductionInboundMapper inboundMapper;
    @Mock
    private ErpStockBatchService stockBatchService;
    @Mock
    private ErpStockRecordService stockRecordService;
    @Mock
    private ErpFinanceBizHookService financeBizHookService;

    @InjectMocks
    private ErpProductionInboundServiceImpl service;

    @Test
    void executeProductionInbound_shouldCreateFinishedBatchBeforeStockRecord() {
        ErpProductionInboundDO inbound = new ErpProductionInboundDO()
                .setId(61L)
                .setNo("ZZRK20260818000001")
                .setProductId(31L)
                .setWarehouseId(41L)
                .setInboundQty(new BigDecimal("6"))
                .setUnitCost(new BigDecimal("12.500000"))
                .setTotalCost(new BigDecimal("75.000000"))
                .setStatus(10);
        when(inboundMapper.selectById(61L)).thenReturn(inbound);

        service.executeProductionInbound(99L, 61L);

        ArgumentCaptor<ErpStockBatchInboundReqBO> batchCaptor = ArgumentCaptor.forClass(ErpStockBatchInboundReqBO.class);
        verify(stockBatchService).createOrIncreaseBatch(batchCaptor.capture());
        assertThat(batchCaptor.getValue())
                .extracting(ErpStockBatchInboundReqBO::getProductId,
                        ErpStockBatchInboundReqBO::getWarehouseId,
                        ErpStockBatchInboundReqBO::getBatchNo,
                        ErpStockBatchInboundReqBO::getCount,
                        ErpStockBatchInboundReqBO::getBizType,
                        ErpStockBatchInboundReqBO::getBizId,
                        ErpStockBatchInboundReqBO::getSourceBizType)
                .containsExactly(31L, 41L, "ZZRK20260818000001", new BigDecimal("6"),
                        ErpStockRecordBizTypeEnum.PRODUCTION_IN.getType(), 61L, "PRODUCTION_INBOUND");
        verify(stockRecordService).createStockRecord(any(ErpStockRecordCreateReqBO.class));
        verify(inboundMapper).updateById(any(ErpProductionInboundDO.class));
    }

    @Test
    void revertProductionInbound_shouldDecreaseFinishedBatchBeforeRollbackRecord() {
        ErpProductionInboundDO inbound = new ErpProductionInboundDO()
                .setId(63L)
                .setNo("ZZRK20260818000002")
                .setProductId(31L)
                .setWarehouseId(41L)
                .setInboundQty(new BigDecimal("7"))
                .setStatus(20);
        when(inboundMapper.selectById(63L)).thenReturn(inbound);
        when(stockBatchService.getStockBatchByProductWarehouseAndBatchNo(31L, 41L, "ZZRK20260818000002"))
                .thenReturn(new ErpStockBatchDO().setId(701L));

        service.revertProductionInbound(100L, 63L);

        ArgumentCaptor<ErpStockBatchChangeReqBO> batchCaptor = ArgumentCaptor.forClass(ErpStockBatchChangeReqBO.class);
        verify(stockBatchService).decreaseBatch(batchCaptor.capture());
        assertThat(batchCaptor.getValue())
                .extracting(ErpStockBatchChangeReqBO::getStockBatchId,
                        ErpStockBatchChangeReqBO::getCount,
                        ErpStockBatchChangeReqBO::getBizType,
                        ErpStockBatchChangeReqBO::getBizId,
                        ErpStockBatchChangeReqBO::getBizNo)
                .containsExactly(701L, new BigDecimal("7"),
                        ErpStockRecordBizTypeEnum.PRODUCTION_IN_CANCEL.getType(), 63L, "ZZRK20260818000002");
        verify(stockRecordService).createStockRecord(any(ErpStockRecordCreateReqBO.class));
        verify(inboundMapper).resetExecutionInfoById(63L);
    }

    @Test
    void revertProductionInbound_shouldRejectWhenFinishedBatchIsMissing() {
        ErpProductionInboundDO inbound = new ErpProductionInboundDO()
                .setId(64L)
                .setNo("ZZRK20260818000003")
                .setProductId(31L)
                .setWarehouseId(41L)
                .setInboundQty(new BigDecimal("1"))
                .setStatus(20);
        when(inboundMapper.selectById(64L)).thenReturn(inbound);
        when(stockBatchService.getStockBatchByProductWarehouseAndBatchNo(31L, 41L, "ZZRK20260818000003"))
                .thenReturn(null);

        assertThatThrownBy(() -> service.revertProductionInbound(100L, 64L))
                .isInstanceOf(RuntimeException.class);

        verify(stockBatchService, never()).decreaseBatch(any(ErpStockBatchChangeReqBO.class));
        verify(stockRecordService, never()).createStockRecord(any(ErpStockRecordCreateReqBO.class));
    }
}
