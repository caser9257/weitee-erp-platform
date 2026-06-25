package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceReturnBatchDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpOutsourceReturnBatchMapper extends BaseMapperX<ErpOutsourceReturnBatchDO> {
    default List<ErpOutsourceReturnBatchDO> selectListByReturnItemIds(Collection<Long> returnItemIds) {
        if (CollUtil.isEmpty(returnItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceReturnBatchDO>()
                .in(ErpOutsourceReturnBatchDO::getReturnItemId, returnItemIds)
                .orderByAsc(ErpOutsourceReturnBatchDO::getId));
    }
    default List<ErpOutsourceReturnBatchDO> selectListByIssueBatchIds(Collection<Long> issueBatchIds) {
        if (CollUtil.isEmpty(issueBatchIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceReturnBatchDO>()
                .in(ErpOutsourceReturnBatchDO::getIssueBatchId, issueBatchIds)
                .orderByAsc(ErpOutsourceReturnBatchDO::getId));
    }
}
