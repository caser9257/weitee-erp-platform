package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRecordPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchRecordMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchRecordServiceImplTest {

    @Test
    void getStockBatchRecordPage_shouldDelegatePageQuery() throws Exception {
        ErpStockBatchRecordServiceImpl service = new ErpStockBatchRecordServiceImpl();
        AtomicReference<ErpStockBatchRecordPageReqVO> reqRef = new AtomicReference<>();
        PageResult<ErpStockBatchRecordDO> expectedPage = new PageResult<>(List.of(
                new ErpStockBatchRecordDO().setId(1L)
                        .setStockBatchId(11L)
                        .setProductId(1L)
                        .setWarehouseId(2L)
                        .setBatchNo("B001")
                        .setCount(new BigDecimal("3.000"))
                        .setAfterAvailableQty(new BigDecimal("8.000"))
                        .setBizType(ErpStockRecordBizTypeEnum.BATCH_ADJUST_IN.getType())
                        .setBizId(11L)
                        .setBizNo("TZ202604290001")
        ), 1L);

        setField(service, "stockBatchRecordMapper", createProxy(ErpStockBatchRecordMapper.class, (methodName, args) -> {
            if ("selectPage".equals(methodName)) {
                reqRef.set((ErpStockBatchRecordPageReqVO) args[0]);
                return expectedPage;
            }
            return null;
        }));

        ErpStockBatchRecordPageReqVO reqVO = new ErpStockBatchRecordPageReqVO();
        reqVO.setStockBatchId(11L);
        reqVO.setProductId(1L);
        reqVO.setWarehouseId(2L);
        reqVO.setBizType(ErpStockRecordBizTypeEnum.BATCH_ADJUST_IN.getType());
        reqVO.setBizNo("TZ202604290001");

        PageResult<ErpStockBatchRecordDO> result = service.getStockBatchRecordPage(reqVO);

        assertThat(reqRef.get()).isSameAs(reqVO);
        assertThat(result).isSameAs(expectedPage);
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
