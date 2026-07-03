package cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns;

import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpPurchaseReturnRespVOTest {

    @Test
    void shouldMapProcessInstanceIdFromPurchaseReturnDO() {
        ErpPurchaseReturnDO purchaseReturn = new ErpPurchaseReturnDO();
        purchaseReturn.setId(1L);
        purchaseReturn.setStatus(ErpAuditStatus.PROCESS.getStatus());
        purchaseReturn.setProcessInstanceId("PI-20260703-001");

        ErpPurchaseReturnRespVO respVO = BeanUtils.toBean(purchaseReturn, ErpPurchaseReturnRespVO.class);

        assertEquals("PI-20260703-001", respVO.getProcessInstanceId());
    }
}
