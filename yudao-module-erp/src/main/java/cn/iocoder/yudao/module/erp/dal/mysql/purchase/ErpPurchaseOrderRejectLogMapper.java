package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
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
