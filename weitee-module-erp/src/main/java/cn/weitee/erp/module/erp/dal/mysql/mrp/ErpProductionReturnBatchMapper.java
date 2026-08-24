package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReturnBatchDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpProductionReturnBatchMapper extends BaseMapperX<ErpProductionReturnBatchDO> {

    default List<ErpProductionReturnBatchDO> selectListByReturnItemIds(Collection<Long> returnItemIds) {
        if (CollUtil.isEmpty(returnItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionReturnBatchDO>()
                .in(ErpProductionReturnBatchDO::getReturnItemId, returnItemIds));
    }

    default List<ErpProductionReturnBatchDO> selectListByIssueBatchIds(Collection<Long> issueBatchIds) {
        if (CollUtil.isEmpty(issueBatchIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionReturnBatchDO>()
                .in(ErpProductionReturnBatchDO::getIssueBatchId, issueBatchIds));
    }

    default List<ErpProductionReturnBatchDO> selectListByIssueBatchIdsForUpdate(Collection<Long> issueBatchIds) {
        if (CollUtil.isEmpty(issueBatchIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionReturnBatchDO>()
                .in(ErpProductionReturnBatchDO::getIssueBatchId, issueBatchIds)
                .last("FOR UPDATE"));
    }

}
