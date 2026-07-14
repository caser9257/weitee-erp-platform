package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockAssembleItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockAssembleMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockAssembleActionTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductQuantityPrecisionService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockAssembleServiceImplTest {

    @Mock
    private ErpProductQuantityPrecisionService productQuantityPrecisionService;
    @Mock
    private ErpStockAssembleMapper assembleMapper;
    @Mock
    private ErpStockAssembleItemMapper assembleItemMapper;
    @Mock
    private ErpStockRecordService stockRecordService;
    @Mock
    private ErpStockBatchAllocationService stockBatchAllocationService;

    @InjectMocks
    private ErpStockAssembleServiceImpl service;

    @Test
    void approveStockAssemble_shouldConsumeMaterialsAndInboundFinishedProduct() {
        ErpStockAssembleDO assemble = new ErpStockAssembleDO()
                .setId(1L).setNo("ZZ-0001")
                .setActionType(ErpStockAssembleActionTypeEnum.ASSEMBLE.getType())
                .setWarehouseId(10L).setProductId(100L)
                .setCount(new BigDecimal("2"))
                .setStatus(ErpAuditStatus.DRAFT.getStatus());
        when(assembleMapper.selectById(1L)).thenReturn(assemble);
        when(assembleMapper.updateStatusIfMatch(1L, ErpAuditStatus.DRAFT.getStatus(),
                ErpAuditStatus.APPROVE.getStatus())).thenReturn(1);
        when(assembleItemMapper.selectListByAssembleId(1L)).thenReturn(List.of(
                new ErpStockAssembleItemDO().setId(11L).setAssembleId(1L)
                        .setProductId(200L).setCount(new BigDecimal("4"))
                        .setUnitCost(new BigDecimal("5")).setStockDirection(-1),
                new ErpStockAssembleItemDO().setId(12L).setAssembleId(1L)
                        .setProductId(100L).setCount(new BigDecimal("2"))
                        .setUnitCost(new BigDecimal("10")).setStockDirection(1)));

        service.updateStockAssembleStatus(1L, ErpAuditStatus.APPROVE.getStatus());

        ArgumentCaptor<ErpStockRecordCreateReqBO> captor = ArgumentCaptor.forClass(ErpStockRecordCreateReqBO.class);
        verify(stockRecordService, org.mockito.Mockito.times(2)).createStockRecord(captor.capture());
        assertEquals(List.of(new BigDecimal("-4"), new BigDecimal("2")),
                captor.getAllValues().stream().map(ErpStockRecordCreateReqBO::getCount).toList());
        verify(stockBatchAllocationService).allocateOutbound(any());
    }

    @Test
    void approveStockAssemble_shouldRejectRepeatedApproval() {
        ErpStockAssembleDO assemble = new ErpStockAssembleDO()
                .setId(1L).setNo("ZZ-0001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus());
        when(assembleMapper.selectById(1L)).thenReturn(assemble);

        assertThrows(ServiceException.class,
                () -> service.updateStockAssembleStatus(1L, ErpAuditStatus.APPROVE.getStatus()));
        verify(assembleMapper, never()).updateStatusIfMatch(any(), any(), any());
        verify(stockRecordService, never()).createStockRecord(any());
    }
}
