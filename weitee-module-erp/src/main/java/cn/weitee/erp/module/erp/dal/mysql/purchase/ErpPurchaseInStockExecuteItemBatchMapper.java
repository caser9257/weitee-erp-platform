package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInStockExecuteItemBatchDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpPurchaseInStockExecuteItemBatchMapper extends BaseMapperX<ErpPurchaseInStockExecuteItemBatchDO> {

    default List<ErpPurchaseInStockExecuteItemBatchDO> selectListByExecuteItemIds(Collection<Long> executeItemIds) {
        if (CollUtil.isEmpty(executeItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpPurchaseInStockExecuteItemBatchDO>()
                .in(ErpPurchaseInStockExecuteItemBatchDO::getExecuteItemId, executeItemIds)
                .orderByAsc(ErpPurchaseInStockExecuteItemBatchDO::getId));
    }

    default List<ErpPurchaseInStockExecuteItemBatchDO> selectListByPurchaseSourceBatchId(Long purchaseSourceBatchId) {
        if (purchaseSourceBatchId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpPurchaseInStockExecuteItemBatchDO>()
                .eq(ErpPurchaseInStockExecuteItemBatchDO::getPurchaseSourceBatchId, purchaseSourceBatchId)
                .orderByAsc(ErpPurchaseInStockExecuteItemBatchDO::getId));
    }

}
