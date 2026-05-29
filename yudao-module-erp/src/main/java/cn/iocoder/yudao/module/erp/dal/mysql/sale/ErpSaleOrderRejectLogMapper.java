package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
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
