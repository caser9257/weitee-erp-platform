package cn.weitee.erp.module.erp.dal.mysql.sale;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpSaleOrderAuditLogMapper extends BaseMapperX<ErpSaleOrderAuditLogDO> {

    default List<ErpSaleOrderAuditLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<ErpSaleOrderAuditLogDO>()
                .eq(ErpSaleOrderAuditLogDO::getOrderId, orderId)
                .orderByDesc(ErpSaleOrderAuditLogDO::getId));
    }

}
