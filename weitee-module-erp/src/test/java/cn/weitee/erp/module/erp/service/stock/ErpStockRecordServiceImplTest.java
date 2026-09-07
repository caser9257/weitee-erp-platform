package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockRecordMapper;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockRecordServiceImplTest {

    @Test
    void createStockRecord_shouldPassPriceToStockIncrementAndRecordTotalCount() throws Exception {
        ErpStockRecordServiceImpl service = new ErpStockRecordServiceImpl();
        ErpStockService stockService = mock(ErpStockService.class);
        ErpStockRecordMapper recordMapper = mock(ErpStockRecordMapper.class);
        when(stockService.updateStockCountIncrement(10L, 20L, new BigDecimal("8"), new BigDecimal("12.5")))
                .thenReturn(new BigDecimal("108"));

        setField(service, "stockService", stockService);
        setField(service, "erpStockRecordMapper", recordMapper);

        service.createStockRecord(new ErpStockRecordCreateReqBO(10L, 20L, new BigDecimal("8"),
                70, 300L, 400L, "CGRK20260903001", new BigDecimal("12.5"), new BigDecimal("100")));

        verify(stockService).updateStockCountIncrement(10L, 20L, new BigDecimal("8"), new BigDecimal("12.5"));
        ArgumentCaptor<ErpStockRecordDO> captor = ArgumentCaptor.forClass(ErpStockRecordDO.class);
        verify(recordMapper).insert(captor.capture());
        ErpStockRecordDO record = captor.getValue();
        assertEquals(new BigDecimal("108"), record.getTotalCount());
        assertEquals(new BigDecimal("12.5"), record.getPrice());
        assertEquals(new BigDecimal("100"), record.getAmount());
        assertEquals("CGRK20260903001", record.getBizNo());
    }

    @Test
    void createStockRecord_shouldNotSwallowStockIncrementFailure() throws Exception {
        ErpStockRecordServiceImpl service = new ErpStockRecordServiceImpl();
        ErpStockService stockService = mock(ErpStockService.class);
        ErpStockRecordMapper recordMapper = mock(ErpStockRecordMapper.class);
        when(stockService.updateStockCountIncrement(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("库存不足"));

        setField(service, "stockService", stockService);
        setField(service, "erpStockRecordMapper", recordMapper);

        ErpStockRecordCreateReqBO reqBO = new ErpStockRecordCreateReqBO(10L, 20L, new BigDecimal("-8"),
                50, 300L, 400L, "XSCK20260903001", BigDecimal.TEN, new BigDecimal("80"));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> service.createStockRecord(reqBO));
        verify(recordMapper, never()).insert(any(ErpStockRecordDO.class));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
