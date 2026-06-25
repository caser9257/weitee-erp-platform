package cn.weitee.erp.module.erp.dal.mysql.sale;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpSaleOrderRejectLogMapper extends BaseMapperX<ErpSaleOrderRejectLogDO> {

    default List<ErpSaleOrderRejectLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<ErpSaleOrderRejectLogDO>()
                .eq(ErpSaleOrderRejectLogDO::getOrderId, orderId)
                .orderByDesc(ErpSaleOrderRejectLogDO::getId));
    }

}
