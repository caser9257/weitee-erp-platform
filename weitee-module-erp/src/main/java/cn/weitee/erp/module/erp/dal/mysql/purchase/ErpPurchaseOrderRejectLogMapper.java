package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpPurchaseOrderRejectLogMapper extends BaseMapperX<ErpPurchaseOrderRejectLogDO> {

    default List<ErpPurchaseOrderRejectLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<ErpPurchaseOrderRejectLogDO>()
                .eq(ErpPurchaseOrderRejectLogDO::getOrderId, orderId)
                .orderByDesc(ErpPurchaseOrderRejectLogDO::getId));
    }

}
