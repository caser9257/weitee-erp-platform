package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpSaleOrderRespVOTest {

    @Test
    void shouldCopyProcessInstanceIdFromSaleOrderDO() {
        ErpSaleOrderDO saleOrder = new ErpSaleOrderDO()
                .setId(1L)
                .setProcessInstanceId("PI-20260407-001");

        ErpSaleOrderRespVO respVO = BeanUtils.toBean(saleOrder, ErpSaleOrderRespVO.class);

        assertEquals("PI-20260407-001", respVO.getProcessInstanceId());
    }

}
