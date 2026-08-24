package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockTaskFailureLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 库存任务失败日志 Mapper
 */
@Mapper
public interface ErpStockTaskFailureLogMapper extends BaseMapperX<ErpStockTaskFailureLogDO> {

    default List<ErpStockTaskFailureLogDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ErpStockTaskFailureLogDO>()
                .eqIfPresent(ErpStockTaskFailureLogDO::getStatus, status)
                .orderByDesc(ErpStockTaskFailureLogDO::getId));
    }

    default List<ErpStockTaskFailureLogDO> selectPendingRetry() {
        return selectList(new LambdaQueryWrapperX<ErpStockTaskFailureLogDO>()
                .eq(ErpStockTaskFailureLogDO::getStatus, ErpStockTaskFailureLogDO.STATUS_PENDING_RETRY)
                .orderByAsc(ErpStockTaskFailureLogDO::getId));
    }

}
