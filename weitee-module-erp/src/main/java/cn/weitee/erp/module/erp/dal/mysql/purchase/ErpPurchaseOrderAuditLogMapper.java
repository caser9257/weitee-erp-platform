package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseOrderAuditLogMapper extends BaseMapperX<ErpPurchaseOrderAuditLogDO> {

    default List<ErpPurchaseOrderAuditLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaseOrderAuditLogDO>()
                .eq(ErpPurchaseOrderAuditLogDO::getOrderId, orderId)
                .orderByDesc(ErpPurchaseOrderAuditLogDO::getId));
    }

}
