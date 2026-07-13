package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpFinancePrepaymentServiceImplTest {

    @Test
    void allocateFinancePrepayment_shouldPersistPrepaymentIdOnAllocation() throws Exception {
        ErpFinancePrepaymentServiceImpl service = new ErpFinancePrepaymentServiceImpl();
        ErpFinancePrepaymentMapper prepaymentMapper = mock(ErpFinancePrepaymentMapper.class);
        ErpFinancePrepaymentAllocateMapper allocateMapper = mock(ErpFinancePrepaymentAllocateMapper.class);
        ErpApStatementItemMapper itemMapper = mock(ErpApStatementItemMapper.class);
        ErpApStatementService statementService = mock(ErpApStatementService.class);
        ErpFinancePrepaymentDO prepayment = new ErpFinancePrepaymentDO()
                .setId(7L).setNo("YF-7").setSupplierId(9L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setPrepaymentPrice(new BigDecimal("100.00"));
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(11L).setStatementNo("YF-11").setSupplierId(9L)
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setAmount(new BigDecimal("80.00"))
                .setRemainAmount(new BigDecimal("80.00"));

        when(prepaymentMapper.selectById(7L)).thenReturn(prepayment);
        when(statementService.validateApStatement(11L)).thenReturn(statement);
        when(allocateMapper.selectApprovedListByPrepaymentId(7L)).thenReturn(List.of());
        when(statementService.getApStatementListByIds(any())).thenReturn(List.of(statement));
        when(allocateMapper.insertBatch(any())).thenReturn(true);

        setField(service, "erpFinancePrepaymentMapper", prepaymentMapper);
        setField(service, "erpFinancePrepaymentAllocateMapper", allocateMapper);
        setField(service, "erpApStatementItemMapper", itemMapper);
        setField(service, "apStatementService", statementService);

        service.allocateFinancePrepayment(new ErpFinancePrepaymentAllocateReqVO()
                .setPrepaymentId(7L)
                .setItems(List.of(new ErpFinancePrepaymentAllocateReqVO.Item()
                        .setApStatementId(11L)
                        .setAllocateAmount(new BigDecimal("20.00"))
                        .setRemark("核销"))));

        ArgumentCaptor<List<ErpFinancePrepaymentAllocateDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(allocateMapper).insertBatch(captor.capture());
        assertEquals(7L, captor.getValue().get(0).getPrepaymentId());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
